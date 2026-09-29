package GUI.Lighting;

import Helper.GameSettings;
import Helper.Point;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Holds every light in a scene plus the scene's ambient light,
 * and works out what color things should be once lit.
 * <p>
 * Blockers affect light pixel by pixel, using the alpha of what they follow: fully transparent pixels let
 * all light through, fully opaque pixels stop it, and semi-transparent pixels let part of it through
 * (alpha 0.25 stops a quarter). Light is worked out outward from each light, one ring of pixels at a time,
 * so each pixel knows how much light made it past everything between it and the light.
 * </p>
 */
public class LightMgmt {
    private final List<LightPoint> lights = new ArrayList<>();

    private Color ambientColor;
    private float ambientIntensity;
    // ambient color * intensity as 0..1 values, stored so they aren't recalculated per pixel
    private float ambientRed;
    private float ambientGreen;
    private float ambientBlue;

    // Layer of blockers that affect light (null means nothing blocks light)
    private LightLayer lightLayer;

    // Default distance (pixels) light tapers into a REFLECT blocker that has no distance of its own
    private int reflectSpread = 50;

    // LIT blockers: how much of the averaged light they get
    private float litBlend = 0.3f;

    // Per-pixel light totals used by applyTo, reused between frames
    private float[] redBuf;
    private float[] greenBuf;
    private float[] blueBuf;

    // Blocker opacity, screen sized. Stamped once per frame (frame*), or per light into scratch (light*)
    // when a light sits inside a blocker and that blocker has to be left out. cur* point at the ones in use.
    private float[] frameOther, frameRef, frameTaper;
    private int[] frameLit;
    private float[] lightOther, lightRef, lightTaper;
    private int[] lightLit;
    private float[] opOther;   // opacity of BLOCK and LIT blockers at each pixel (0..1)
    private float[] opRef;     // opacity of REFLECT blockers at each pixel (0..1)
    private float[] taperBuf;  // reflect distance of the REFLECT blocker at each pixel
    private int[] litIdBuf;    // which LIT blocker covers each pixel (index into frameBlockers), -1 for none

    // Per-light working buffers, screen sized. Only the part around the current light is used.
    private float[] tout;      // share of the light that carries on past each pixel
    private float[] lout;      // light strength carrying on past each pixel (brightness * tout)
    private float[] entryBuf;  // REFLECT: light strength where the ray went into reflect material
    private float[] depthBuf;  // REFLECT: how far the ray has travelled inside reflect material

    // This frame's blockers; litIdBuf holds indexes into this
    private ActiveBlocker[] frameBlockers = new ActiveBlocker[0];

    // Alpha byte (0..255) -> opacity (0..1)
    private static final float[] ALPHA = new float[256];
    static {
        for (int i = 0; i < 256; i++) {
            ALPHA[i] = i / 255f;
        }
    }

    // LIT face totals are kept separately for each part of a light worked on at the same time:
    // slots 0-3 are the four quarters around the light, slot 4 is the light's own row and column.
    private static final int SLOTS = 5;

    public LightMgmt(Color ambientColor, float ambientIntensity) {
        setAmbient(ambientColor, ambientIntensity);
    }

    /** Dim white ambient light. */
    public LightMgmt() {
        this(Color.WHITE, 0.1f);
    }

    /**
     * Makes a light manager whose light is blocked by the blockers in a light layer.
     * @param lightLayer The layer of blockers.
     * @param ambientColor The ambient light color.
     * @param ambientIntensity The ambient light strength, 0 to 1.
     */
    public LightMgmt(LightLayer lightLayer, Color ambientColor, float ambientIntensity) {
        this(ambientColor, ambientIntensity);
        this.lightLayer = lightLayer;
    }

    /** Dim white ambient light, with light blocked by the blockers in a light layer. */
    public LightMgmt(LightLayer lightLayer) {
        this(lightLayer, Color.WHITE, 0.1f);
    }


    // ---------------------------------------------------------------
    // Ambient light
    // ---------------------------------------------------------------
    public void setAmbient(Color color, float intensity) {
        this.ambientColor = color;
        this.ambientIntensity = intensity;
        this.ambientRed = color.getRed() / 255f * intensity;
        this.ambientGreen = color.getGreen() / 255f * intensity;
        this.ambientBlue = color.getBlue() / 255f * intensity;
    }

    public void setAmbientColor(Color color) {
        setAmbient(color, ambientIntensity);
    }

    public void setAmbientIntensity(float intensity) {
        setAmbient(ambientColor, intensity);
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    // ---------------------------------------------------------------
    // Lights
    // ---------------------------------------------------------------
    public void addLight(LightPoint light) {
        lights.add(light);
    }

    public void removeLight(LightPoint light) {
        lights.remove(light);
    }

    public void clearLights() {
        lights.clear();
    }

    public List<LightPoint> getLights() {
        return lights;
    }

    // ---------------------------------------------------------------
    // Light blocking
    // ---------------------------------------------------------------
    public void setLightLayer(LightLayer lightLayer) {
        this.lightLayer = lightLayer;
    }

    public LightLayer getLightLayer() {
        return lightLayer;
    }

    /**
     * Default distance, in pixels, that light tapers into a REFLECT blocker (used when the blocker has no reflectDist).
     */
    public void setReflectSpread(int reflectSpread) {
        this.reflectSpread = Math.max(1, reflectSpread);
    }

    public int getReflectSpread() {
        return reflectSpread;
    }

    public void setLitBlend(float litBlend) {
        this.litBlend = Math.max(0f, Math.min(1f, litBlend));
    }

    public float getLitBlend() {
        return litBlend;
    }

    /** One active blocker for this frame: where it is, its alpha, and what it does to light. */
    private static final class ActiveBlocker {
        final int index;
        final Rectangle bounds;
        final boolean reflect;
        final boolean lit;
        final int reflectDist;
        final float litBlend;

        Rectangle area;   // the on-screen part of bounds
        int[] spritePx;   // ARGB pixels covering area, or null if the whole box is solid

        // LIT: light collected on its faces, per light (one total per slot) and overall
        final float[] faceSum = new float[SLOTS];
        final int[] faceCount = new int[SLOTS];
        float litRed, litGreen, litBlue;

        ActiveBlocker(int index, Rectangle bounds, boolean reflect, boolean lit, int reflectDist, float litBlend) {
            this.index = index;
            this.bounds = bounds;
            this.reflect = reflect;
            this.lit = lit;
            this.reflectDist = reflectDist;
            this.litBlend = litBlend;
        }

        /** How opaque this blocker is at a pixel, 0..1. Off-screen parts of its box count as fully opaque. */
        float alphaAt(int x, int y) {
            if (!bounds.contains(x, y)) {
                return 0f;
            }
            if (spritePx == null || !area.contains(x, y)) {
                return 1f;
            }
            return (spritePx[(y - area.y) * area.width + (x - area.x)] >>> 24) / 255f;
        }
    }

    /**
     * Finds every active blocker in the light layer and grabs the alpha of what it follows.
     * Rectangles are in the drawn layer's coordinates, which are the same as the lit image's.
     */
    private List<ActiveBlocker> collectBlockers(int width, int height) {
        List<ActiveBlocker> result = new ArrayList<>();
        if (lightLayer == null) {
            return result;
        }
        Rectangle screen = new Rectangle(0, 0, width, height);
        for (LightBlocker blocker : lightLayer.getBlockers()) {
            if (!blocker.isActive()) {
                continue;
            }
            boolean reflect = blocker.getTag() == LightBlocker.LightTag.REFLECT;
            boolean lit = blocker.getTag() == LightBlocker.LightTag.LIT;
            int baseDist = blocker.getReflectDist() > 0 ? blocker.getReflectDist() : reflectSpread;
            int dist = Math.max(1, GameSettings.scale(baseDist)); // reference pixels -> screen pixels
            float blend = blocker.getLitBlend() >= 0f ? blocker.getLitBlend() : litBlend;

            Rectangle bounds = blocker.getLightBounds();
            ActiveBlocker b = new ActiveBlocker(result.size(), bounds, reflect, lit, dist, blend);

            Rectangle area = bounds.intersection(screen);
            if (area.isEmpty()) {
                b.area = new Rectangle(bounds.x, bounds.y, 0, 0);
            } else {
                b.area = area;
                BufferedImage sprite = blocker.getAlphaImage(area);
                if (sprite != null) {
                    b.spritePx = ((DataBufferInt) sprite.getRaster().getDataBuffer()).getData();
                }
            }
            result.add(b);
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Single point: give it a point and a color, get the lit color back
    // (does not account for blockers)
    // ---------------------------------------------------------------
    public Color getLitColor(Point point, Color baseColor) {
        int x = point.getX();
        int y = point.getY();

        // Start with the ambient light, then add every light that reaches this point
        float red = ambientRed;
        float green = ambientGreen;
        float blue = ambientBlue;
        for (LightPoint light : lights) {
            float strength = light.getBrightnessAt(x, y);
            if (strength > 0f) {
                red += strength * light.getRed();
                green += strength * light.getGreen();
                blue += strength * light.getBlue();
            }
        }

        return new Color(
                lightChannel(baseColor.getRed(), red),
                lightChannel(baseColor.getGreen(), green),
                lightChannel(baseColor.getBlue(), blue),
                baseColor.getAlpha());
    }

    /** Multiplies one 0..255 color channel by the light reaching it (capped at 1). */
    private static int lightChannel(int channel, float light) {
        if (light > 1f) {
            light = 1f;
        }
        return (int) (channel * light);
    }

    // ---------------------------------------------------------------
    // Whole image: lights every pixel of an image at once (used by LightingLayerUI)
    // ---------------------------------------------------------------
    public void applyTo(BufferedImage image) {
        int type = image.getType();
        if (type != BufferedImage.TYPE_INT_RGB && type != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalArgumentException("Image must be TYPE_INT_RGB or TYPE_INT_ARGB");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int size = width * height;
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        ensureBuffers(size);

        // 1. Every pixel starts with the ambient light
        Arrays.fill(redBuf, ambientRed);
        Arrays.fill(greenBuf, ambientGreen);
        Arrays.fill(blueBuf, ambientBlue);

        // Everything in the blocker panel that affects light this frame, and its opacity (same for every light)
        List<ActiveBlocker> blockers = collectBlockers(width, height);
        frameBlockers = blockers.toArray(new ActiveBlocker[0]);
        stampBlockers(blockers, 0, 0, width - 1, height - 1, width,
                frameOther, frameRef, frameTaper, frameLit);

        // 2. Add each light
        for (LightPoint light : lights) {
            int cx = light.getLoc().getX();
            int cy = light.getLoc().getY();
            int dist = light.getDist();
            Rectangle reach = new Rectangle(cx - dist, cy - dist, dist * 2 + 1, dist * 2 + 1);

            // A blocker the light sits on (non-transparent pixel) is left out, otherwise the light would be smothered
            List<ActiveBlocker> excluded = new ArrayList<>();
            for (ActiveBlocker b : blockers) {
                if (b.alphaAt(cx, cy) > 0f) {
                    excluded.add(b);
                }
            }

            // A REFLECT taper can carry light past the light's own reach, so work out how far to go
            int maxTaper = 0;
            for (ActiveBlocker b : blockers) {
                if (b.reflect && !excluded.contains(b) && b.bounds.intersects(grow(reach, b.reflectDist))) {
                    maxTaper = Math.max(maxTaper, b.reflectDist);
                }
            }
            int range = dist + maxTaper;

            int x0 = Math.max(0, cx - range);
            int y0 = Math.max(0, cy - range);
            int x1 = Math.min(width - 1, cx + range);
            int y1 = Math.min(height - 1, cy + range);
            if (x0 > x1 || y0 > y1) {
                continue; // nothing of this light is on screen
            }

            if (excluded.isEmpty()) {
                // Usual case: the frame's opacity works as it is
                opOther = frameOther;
                opRef = frameRef;
                taperBuf = frameTaper;
                litIdBuf = frameLit;
            } else {
                // Redo the opacity around this light without the blockers it sits in
                List<ActiveBlocker> kept = new ArrayList<>(blockers);
                kept.removeAll(excluded);
                stampBlockers(kept, x0, y0, x1, y1, width, lightOther, lightRef, lightTaper, lightLit);
                opOther = lightOther;
                opRef = lightRef;
                taperBuf = lightTaper;
                litIdBuf = lightLit;
            }

            for (ActiveBlocker b : blockers) {
                if (b.lit) {
                    Arrays.fill(b.faceSum, 0f);
                    Arrays.fill(b.faceCount, 0);
                }
            }

            castLight(light, cx, cy, range, x0, y0, x1, y1, width);

            // LIT blockers get the average light that reached their faces
            for (ActiveBlocker b : blockers) {
                if (!b.lit) {
                    continue;
                }
                float sum = 0f;
                int count = 0;
                for (int slot = 0; slot < SLOTS; slot++) {
                    sum += b.faceSum[slot];
                    count += b.faceCount[slot];
                }
                if (count > 0) {
                    float avg = sum / count;
                    b.litRed += avg * light.getRed();
                    b.litGreen += avg * light.getGreen();
                    b.litBlue += avg * light.getBlue();
                }
            }
        }

        for (ActiveBlocker b : blockers) {
            if (b.lit) {
                fillLit(b, width);
            }
        }

        // 3. Multiply every pixel's color by the light that reached it (rows split across cores)
        IntStream.range(0, height).parallel().forEach(y -> {
            int end = (y + 1) * width;
            for (int i = y * width; i < end; i++) {
                int p = pixels[i];
                int alpha = p & 0xFF000000;
                int red = lightChannel((p >> 16) & 0xFF, redBuf[i]);
                int green = lightChannel((p >> 8) & 0xFF, greenBuf[i]);
                int blue = lightChannel(p & 0xFF, blueBuf[i]);
                pixels[i] = alpha | (red << 16) | (green << 8) | blue;
            }
        });
    }

    private void ensureBuffers(int size) {
        if (redBuf != null && redBuf.length == size) {
            return;
        }
        redBuf = new float[size];
        greenBuf = new float[size];
        blueBuf = new float[size];
        frameOther = new float[size];
        frameRef = new float[size];
        frameTaper = new float[size];
        frameLit = new int[size];
        lightOther = new float[size];
        lightRef = new float[size];
        lightTaper = new float[size];
        lightLit = new int[size];
        tout = new float[size];
        lout = new float[size];
        entryBuf = new float[size];
        depthBuf = new float[size];
    }

    private static Rectangle grow(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount, r.width + amount * 2, r.height + amount * 2);
    }

    // ---------------------------------------------------------------
    // Blocker opacity
    // ---------------------------------------------------------------

    /**
     * Writes the opacity of the given blockers into a set of opacity buffers, inside an area.
     * Overlapping blockers stack: two 50% layers let 25% through.
     */
    private static void stampBlockers(List<ActiveBlocker> list, int x0, int y0, int x1, int y1, int width,
                                      float[] other, float[] ref, float[] taper, int[] lit) {
        for (int y = y0; y <= y1; y++) {
            int from = y * width + x0;
            int to = y * width + x1 + 1;
            Arrays.fill(other, from, to, 0f);
            Arrays.fill(ref, from, to, 0f);
            Arrays.fill(taper, from, to, 0f);
            Arrays.fill(lit, from, to, -1);
        }

        Rectangle region = new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
        for (ActiveBlocker b : list) {
            Rectangle r = b.area.intersection(region);
            if (r.isEmpty()) {
                continue;
            }
            int[] px = b.spritePx;
            int aw = b.area.width;
            for (int y = r.y; y < r.y + r.height; y++) {
                int row = y * width;
                int srcRow = (y - b.area.y) * aw - b.area.x;
                for (int x = r.x; x < r.x + r.width; x++) {
                    float a = px == null ? 1f : ALPHA[px[srcRow + x] >>> 24];
                    if (a <= 0f) {
                        continue; // fully transparent: no effect on light
                    }
                    int i = row + x;
                    if (b.reflect) {
                        ref[i] = 1f - (1f - ref[i]) * (1f - a);
                        taper[i] = Math.max(taper[i], b.reflectDist);
                    } else {
                        other[i] = 1f - (1f - other[i]) * (1f - a);
                        if (b.lit) {
                            lit[i] = b.index;
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Casting light
    // ---------------------------------------------------------------

    /**
     * Spreads one light outward, one square ring of pixels at a time. Every pixel's light comes from the
     * pixels one ring closer along the line back to the light, so each ring only needs the one before it.
     * <p>
     * The light's own row and column go first. After that, the four quarters around the light never read
     * each other's pixels, so they run at the same time on separate cores.
     * </p>
     */
    private void castLight(LightPoint light, int cx, int cy, int range,
                           int x0, int y0, int x1, int y1, int width) {
        float lr = light.getRed();
        float lg = light.getGreen();
        float lb = light.getBlue();

        // The light's pixel, then its row and column outward
        float[] acc = new float[4];
        visitIfInside(light, cx, cy, cx, cy, x0, y0, x1, y1, width, lr, lg, lb, 4, acc);
        for (int k = 1; k <= range; k++) {
            visitIfInside(light, cx, cy, cx + k, cy, x0, y0, x1, y1, width, lr, lg, lb, 4, acc);
            visitIfInside(light, cx, cy, cx - k, cy, x0, y0, x1, y1, width, lr, lg, lb, 4, acc);
            visitIfInside(light, cx, cy, cx, cy + k, x0, y0, x1, y1, width, lr, lg, lb, 4, acc);
            visitIfInside(light, cx, cy, cx, cy - k, x0, y0, x1, y1, width, lr, lg, lb, 4, acc);
        }

        // The four quarters in parallel
        IntStream.range(0, 4).parallel().forEach(q ->
                castQuarter(q, light, cx, cy, range, x0, y0, x1, y1, width, lr, lg, lb));
    }

    /**
     * One quarter around the light (not counting the light's row and column), ring by ring.
     * Quarter 0 is right-down, 1 left-down, 2 left-up, 3 right-up.
     */
    private void castQuarter(int q, LightPoint light, int cx, int cy, int range,
                             int x0, int y0, int x1, int y1, int width, float lr, float lg, float lb) {
        int sx = (q == 0 || q == 3) ? 1 : -1;
        int sy = (q < 2) ? 1 : -1;
        float[] acc = new float[4];

        // How far (in steps of sx / sy from the light) the area reaches in each direction
        int iLo = sx > 0 ? x0 - cx : cx - x1;
        int iHi = sx > 0 ? x1 - cx : cx - x0;
        int jLo = sy > 0 ? y0 - cy : cy - y1;
        int jHi = sy > 0 ? y1 - cy : cy - y0;
        if (iHi < 1 || jHi < 1) {
            return; // this quarter is entirely outside the area
        }

        for (int k = 1; k <= range; k++) {
            // The ring's column on this side: dx = k, dy = 1..k
            if (k >= iLo && k <= iHi) {
                int x = cx + sx * k;
                int from = Math.max(1, jLo);
                int to = Math.min(k, jHi);
                for (int j = from; j <= to; j++) {
                    visit(light, cx, cy, x, cy + sy * j, x0, y0, x1, y1, width, lr, lg, lb, q, acc);
                }
            }
            // The ring's row on this side: dy = k, dx = 1..k-1
            if (k >= jLo && k <= jHi) {
                int y = cy + sy * k;
                int from = Math.max(1, iLo);
                int to = Math.min(k - 1, iHi);
                for (int i = from; i <= to; i++) {
                    visit(light, cx, cy, cx + sx * i, y, x0, y0, x1, y1, width, lr, lg, lb, q, acc);
                }
            }
        }
    }

    private void visitIfInside(LightPoint light, int cx, int cy, int x, int y,
                               int x0, int y0, int x1, int y1, int width,
                               float lr, float lg, float lb, int slot, float[] acc) {
        if (x >= x0 && x <= x1 && y >= y0 && y <= y1) {
            visit(light, cx, cy, x, y, x0, y0, x1, y1, width, lr, lg, lb, slot, acc);
        }
    }

    /**
     * Works out one pixel for one light: how much light reaches it, how much its own opacity stops,
     * any REFLECT glow, and whether it is a lit face of a LIT blocker.
     * {@code acc} is a 4-slot scratch array: share of light arriving, reflect entry strength,
     * reflect depth, and how much of the way back stays inside the same LIT blocker.
     */
    private void visit(LightPoint light, int cx, int cy, int x, int y,
                       int x0, int y0, int x1, int y1, int width,
                       float lr, float lg, float lb, int slot, float[] acc) {
        int i = y * width + x;
        int dx = x - cx;
        int dy = y - cy;
        int adx = Math.abs(dx);
        int ady = Math.abs(dy);
        int k = Math.max(adx, ady);

        float bright = light.getBrightnessAt(x, y);
        float aOther = opOther[i];
        float aRef = opRef[i];
        boolean inRef = aRef > 0f;
        int litId = litIdBuf[i];

        acc[0] = 0f;
        acc[1] = 0f;
        acc[2] = 0f;
        acc[3] = 0f;

        if (k == 0) {
            acc[0] = 1f; // the light's own pixel
        } else {
            // Distance between rings along this ray (only needed inside reflect material)
            float step = inRef ? (float) (Math.sqrt((double) dx * dx + (double) dy * dy) / k) : 0f;
            // The point one ring closer along the line to the light usually falls between two pixels,
            // so blend those two by how close it is to each.
            if (adx >= ady) {
                int px = x - Integer.signum(dx);
                float f = (float) (dy * (k - 1)) / k;
                int fl = (int) Math.floor(f);
                float w = f - fl;
                addParent(light, px, cy + fl, 1f - w, step, inRef, litId, x0, y0, x1, y1, width, acc);
                addParent(light, px, cy + fl + 1, w, step, inRef, litId, x0, y0, x1, y1, width, acc);
            } else {
                int py = y - Integer.signum(dy);
                float f = (float) (dx * (k - 1)) / k;
                int fl = (int) Math.floor(f);
                float w = f - fl;
                addParent(light, cx + fl, py, 1f - w, step, inRef, litId, x0, y0, x1, y1, width, acc);
                addParent(light, cx + fl + 1, py, w, step, inRef, litId, x0, y0, x1, y1, width, acc);
            }
        }

        float tin = acc[0];                             // share of the light that reached this pixel
        float t = tin * (1f - aOther) * (1f - aRef);    // share left after this pixel's own opacity
        tout[i] = t;
        float strength = bright * t;
        lout[i] = strength;

        // REFLECT: full strength where the ray went in, fading evenly to 0 at the taper distance
        if (inRef) {
            entryBuf[i] = acc[1];
            depthBuf[i] = acc[2];
            float fade = 1f - acc[2] / taperBuf[i];
            if (fade > 0f) {
                strength += acc[1] * fade * aRef;
            }
        } else {
            entryBuf[i] = 0f;
            depthBuf[i] = 0f;
        }

        if (strength > 0f) {
            redBuf[i] += strength * lr;
            greenBuf[i] += strength * lg;
            blueBuf[i] += strength * lb;
        }

        // LIT: a pixel of the blocker whose way back to the light leaves the blocker is a face facing the light
        if (litId >= 0 && acc[3] < 0.5f) {
            ActiveBlocker b = frameBlockers[litId];
            b.faceSum[slot] += bright * tin;
            b.faceCount[slot]++;
        }
    }

    /** Adds one of a pixel's two parent pixels (one ring closer to the light) to the running totals, weighted. */
    private void addParent(LightPoint light, int px, int py, float w, float step, boolean inRef, int litId,
                           int x0, int y0, int x1, int y1, int width, float[] acc) {
        if (w <= 0f) {
            return;
        }
        if (px < x0 || px > x1 || py < y0 || py > y1) {
            // Off screen: nothing there blocks light
            acc[0] += w;
            if (inRef) {
                acc[1] += w * light.getBrightnessAt(px, py);
                acc[2] += w * step * 0.5f;
            }
            return;
        }
        int j = py * width + px;
        acc[0] += w * tout[j];
        if (inRef) {
            if (opRef[j] > 0f) {
                // Already inside reflect material: carry the entry strength on, dimmed by anything else in the way
                acc[1] += w * entryBuf[j] * (1f - opOther[j]);
                acc[2] += w * (depthBuf[j] + step);
            } else {
                // Coming in from outside: the light arriving here is the entry strength
                acc[1] += w * lout[j];
                acc[2] += w * step * 0.5f;
            }
        }
        if (litId >= 0 && litIdBuf[j] == litId) {
            acc[3] += w;
        }
    }

    // ---------------------------------------------------------------
    // LIT blockers
    // ---------------------------------------------------------------

    /**
     * Lights a LIT blocker evenly with its averaged light, then blends the result toward the original color.
     * Semi-transparent pixels get a matching share of that; fully transparent ones keep their normal lighting.
     * Must run after every light has been added, since it rewrites the light buffers for this area.
     */
    private void fillLit(ActiveBlocker b, int width) {
        Rectangle area = b.area;
        if (area.isEmpty()) {
            return;
        }
        float keep = 1f - b.litBlend; // share of the original color
        for (int y = area.y; y < area.y + area.height; y++) {
            int row = y * width;
            for (int x = area.x; x < area.x + area.width; x++) {
                float a = b.spritePx == null ? 1f
                        : ALPHA[b.spritePx[(y - area.y) * area.width + (x - area.x)] >>> 24];
                if (a <= 0f) {
                    continue;
                }
                int i = row + x;
                float r = keep + b.litBlend * Math.min(1f, redBuf[i] + b.litRed);
                float g = keep + b.litBlend * Math.min(1f, greenBuf[i] + b.litGreen);
                float bl = keep + b.litBlend * Math.min(1f, blueBuf[i] + b.litBlue);
                redBuf[i] += (r - redBuf[i]) * a;
                greenBuf[i] += (g - greenBuf[i]) * a;
                blueBuf[i] += (bl - blueBuf[i]) * a;
            }
        }
    }
}