package GUI.Lighting;

import Helper.GameSettings;
import Helper.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds every light in a scene plus the scene's ambient light,
 * and works out what color things should be once lit.
 */
public class LightMgmt {
    public static final String LIGHT_TAG_KEY = "LightMgmt.LightTag";
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

    // Per-pixel light totals used by applyTo, reused between frames
    private float[] redBuf;
    private float[] greenBuf;
    private float[] blueBuf;

    // Shadow mask for the light currently being worked on: 0 = lit, 255 = fully in shadow
    private BufferedImage shadowMask;
    private byte[] maskPx;
    private float[] reflectBuf;

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

    /** One active blocker for this frame: where it is, whether it reflects, and how far its taper goes. */
    private static final class ActiveBlocker {
        final Rectangle bounds;
        final boolean reflect;
        final int reflectDist;

        ActiveBlocker(Rectangle bounds, boolean reflect, int reflectDist) {
            this.bounds = bounds;
            this.reflect = reflect;
            this.reflectDist = reflectDist;
        }
    }

    /**
     * Finds every active blocker in the light layer.
     * Rectangles are in the drawn layer's coordinates, which are the same as the lit image's.
     */
    private List<ActiveBlocker> collectBlockers() {
        List<ActiveBlocker> result = new ArrayList<>();
        if (lightLayer == null) {
            return result;
        }
        for (LightBlocker blocker : lightLayer.getBlockers()) {
            if (!blocker.isActive()) {
                continue;
            }
            boolean reflect = blocker.getTag() == LightBlocker.LightTag.REFLECT;
            int baseDist = blocker.getReflectDist() > 0 ? blocker.getReflectDist() : reflectSpread;
            int dist = GameSettings.scale(baseDist); // reference pixels -> screen pixels
            result.add(new ActiveBlocker(blocker.getLightBounds(), reflect, dist));
        }
        return result;
    }

    /**
     * Finds every active blocker in the light layer.
     * Rectangles are in the drawn layer's coordinates, which are the same as the lit image's.
     */
    private void collectBlockers(List<Rectangle> blockers, List<Boolean> reflects) {
        if (lightLayer == null) {
            return;
        }
        for (LightBlocker blocker : lightLayer.getBlockers()) {
            if (!blocker.isActive()) {
                continue;
            }
            blockers.add(blocker.getLightBounds());
            reflects.add(blocker.getTag() == LightBlocker.LightTag.REFLECT);
        }
    }

    // ---------------------------------------------------------------
    // Single point: give it a point and a color, get the lit color back
    // (does not account for shadows or reflections)
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

        if (redBuf == null || redBuf.length != size) {
            redBuf = new float[size];
            greenBuf = new float[size];
            blueBuf = new float[size];
            reflectBuf = new float[size];
        }

        // 1. Every pixel starts with the ambient light
        Arrays.fill(redBuf, ambientRed);
        Arrays.fill(greenBuf, ambientGreen);
        Arrays.fill(blueBuf, ambientBlue);

        // Everything in the blocker panel that stops light this frame
        List<ActiveBlocker> blockers = collectBlockers();

        if (!blockers.isEmpty() &&
                (shadowMask == null || shadowMask.getWidth() != width || shadowMask.getHeight() != height)) {
            shadowMask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            maskPx = ((DataBufferByte) shadowMask.getRaster().getDataBuffer()).getData();
        }

        // 2. Add each light, only inside the square it can reach
        for (LightPoint light : lights) {
            int cx = light.getLoc().getX();
            int cy = light.getLoc().getY();
            int dist = light.getDist();
            int x0 = Math.max(0, cx - dist);
            int y0 = Math.max(0, cy - dist);
            int x1 = Math.min(width - 1, cx + dist);
            int y1 = Math.min(height - 1, cy + dist);

            // Blockers this light can reach. A blocker the light sits inside is ignored,
            // otherwise the light would be completely smothered.
            Rectangle reach = new Rectangle(cx - dist, cy - dist, dist * 2 + 1, dist * 2 + 1);
            List<ActiveBlocker> active = new ArrayList<>();
            List<Rectangle> activeRects = new ArrayList<>();
            List<ActiveBlocker> activeReflectors = new ArrayList<>();
            for (ActiveBlocker b : blockers) {
                // A reflector's taper can reach past the light's square, so grow the check for it
                Rectangle check = b.reflect ? grow(reach, b.reflectDist) : reach;
                if (!b.bounds.intersects(check) || containsInclusive(b.bounds, cx, cy)) {
                    continue;
                }
                active.add(b);
                if (b.bounds.intersects(reach)) {
                    activeRects.add(b.bounds);
                }
                if (b.reflect) {
                    activeReflectors.add(b);
                }
            }

            boolean onScreen = x0 <= x1 && y0 <= y1;
            boolean hasShadow = onScreen && !activeRects.isEmpty() && buildShadowMask(light, activeRects, x0, y0, x1, y1);

            // Direct light
            if (onScreen) {
                for (int y = y0; y <= y1; y++) {
                    int row = y * width;
                    for (int x = x0; x <= x1; x++) {
                        float strength = light.getBrightnessAt(x, y);
                        if (strength <= 0f) {
                            continue;
                        }
                        int i = row + x;
                        if (hasShadow) {
                            int shade = maskPx[i] & 0xFF;
                            if (shade == 255) {
                                continue; // fully in shadow
                            }
                            strength *= (255 - shade) / 255f; // soft shadow edge
                        }

                        redBuf[i] += strength * light.getRed();
                        greenBuf[i] += strength * light.getGreen();
                        blueBuf[i] += strength * light.getBlue();
                    }
                }
            }

            // Light tapering into REFLECT blockers from their lit edges
            List<Rectangle> taperAreas = new ArrayList<>();
            for (ActiveBlocker r : activeReflectors) {
                addReflection(light, r, active, hasShadow, x0, y0, x1, y1, width, height, taperAreas);
            }
            for (Rectangle area : taperAreas) {
                flushReflection(light, area, width);
            }
        }

        // 3. Multiply every pixel's color by the light that reached it
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < size; i++) {
            int p = pixels[i];
            int alpha = p & 0xFF000000;
            int red = (p >> 16) & 0xFF;
            int green = (p >> 8) & 0xFF;
            int blue = p & 0xFF;

            red = lightChannel(red, redBuf[i]);
            green = lightChannel(green, greenBuf[i]);
            blue = lightChannel(blue, blueBuf[i]);

            pixels[i] = alpha | (red << 16) | (green << 8) | blue;
        }
    }

    private static Rectangle grow(Rectangle r, int amount) {
        return new Rectangle(r.x - amount, r.y - amount, r.width + amount * 2, r.height + amount * 2);
    }

    /** Like Rectangle.contains, but counts points sitting exactly on the edge. */
    private static boolean containsInclusive(Rectangle r, int x, int y) {
        return x >= r.x && x <= r.x + r.width && y >= r.y && y <= r.y + r.height;
    }

    // ---------------------------------------------------------------
    // Shadows
    // ---------------------------------------------------------------

    /**
     * Draws the shadows of every blocker for one light into the shadow mask, inside the light's square.
     * Blockers themselves are left lit (so their faces still show), unless another blocker shadows them.
     * @return true if any shadow landed inside the square.
     */
    private boolean buildShadowMask(LightPoint light, List<Rectangle> blockers, int x0, int y0, int x1, int y1) {
        // Use the middle of the light's pixel so shadows line up with pixel centers
        double lx = light.getLoc().getX() + 0.5;
        double ly = light.getLoc().getY() + 0.5;
        // Far enough that every shadow runs past the edge of the light's reach
        double far = light.getDist() * 3.0 + 10;

        Area shadow = new Area();
        for (Rectangle r : blockers) {
            Area s = shadowOf(r, lx, ly, far);
            //s.subtract(new Area(r)); // keep the blocker's own face lit
            shadow.add(s);
        }

        Rectangle square = new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
        Graphics2D g = shadowMask.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(Color.BLACK);
        g.fill(square); // clear only the part this light uses

        boolean any = shadow.intersects(square);
        if (any) {
            g.setClip(square);
            // Antialiasing gives the shadow edges a slight softness
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fill(shadow);
        }
        g.dispose();
        return any;
    }

    /**
     * The area a rectangle hides from a light: the rectangle plus everything behind it.
     * Each edge is stretched away from the light, and the pieces are joined together.
     */
    private static Area shadowOf(Rectangle r, double lx, double ly, double far) {
        double[][] corners = {
                {r.x, r.y},
                {r.x + r.width, r.y},
                {r.x + r.width, r.y + r.height},
                {r.x, r.y + r.height}
        };

        Area area = new Area();
        for (int i = 0; i < 4; i++) {
            double[] a = corners[i];
            double[] b = corners[(i + 1) % 4];

            double[] aFar = pushAway(a[0], a[1], lx, ly, far);
            double[] bFar = pushAway(b[0], b[1], lx, ly, far);

            // A point straight out along the middle direction. Without it, a light very close
            // to an edge would make the far side of the shadow cut back in toward the light.
            double[] dirA = unit(a[0] - lx, a[1] - ly);
            double[] dirB = unit(b[0] - lx, b[1] - ly);
            double[] mid = unit(dirA[0] + dirB[0], dirA[1] + dirB[1]);

            Path2D.Double piece = new Path2D.Double();
            piece.moveTo(a[0], a[1]);
            piece.lineTo(b[0], b[1]);
            piece.lineTo(bFar[0], bFar[1]);
            if (mid[0] != 0 || mid[1] != 0) {
                piece.lineTo(lx + mid[0] * far * 2, ly + mid[1] * far * 2);
            }
            piece.lineTo(aFar[0], aFar[1]);
            piece.closePath();
            area.add(new Area(piece));
        }
        return area;
    }

    /** Moves a point further away from the light by the given distance. */
    private static double[] pushAway(double px, double py, double lx, double ly, double amount) {
        double[] dir = unit(px - lx, py - ly);
        return new double[]{px + dir[0] * amount, py + dir[1] * amount};
    }

    private static double[] unit(double x, double y) {
        double len = Math.sqrt(x * x + y * y);
        if (len < 1e-9) {
            return new double[]{0, 0};
        }
        return new double[]{x / len, y / len};
    }

    // ---------------------------------------------------------------
    // Reflections
    // ---------------------------------------------------------------

    /**
     * Lights a REFLECT rectangle from the edges that face a light.
     * <p>
     * The light stops at the edge (the rectangle and everything behind it are already in shadow), but the
     * surface near a lit edge picks some of it up: for each pixel inside the rectangle, follow the ray from
     * the light to that pixel back to where it entered the rectangle. The pixel gets the light's strength at
     * that entry point, fading evenly to 0 once the ray has travelled the blocker's reflect distance inside.
     * </p>
     * Only bands along the facing edges are scanned, since nothing deeper than the reflect distance is lit.
     * Each scanned band is added to {@code outAreas} so it can be flushed afterwards.
     */
    private void addReflection(LightPoint light, ActiveBlocker ref, List<ActiveBlocker> blockers, boolean hasShadow,
                               int sx0, int sy0, int sx1, int sy1, int width, int height, List<Rectangle> outAreas) {
        Rectangle r = ref.bounds;
        int taper = ref.reflectDist;

        // Pixel centers, matching the shadow code
        double lx = light.getLoc().getX() + 0.5;
        double ly = light.getLoc().getY() + 0.5;

        int left = r.x;
        int top = r.y;
        int right = r.x + r.width;    // exclusive
        int bottom = r.y + r.height;  // exclusive

        // Only pixels the taper can reach: inside the rectangle, on screen, within the light's reach + taper
        int cx = light.getLoc().getX();
        int cy = light.getLoc().getY();
        int reachDist = light.getDist() + taper;
        Rectangle limit = r
                .intersection(new Rectangle(0, 0, width, height))
                .intersection(new Rectangle(cx - reachDist, cy - reachDist, reachDist * 2 + 1, reachDist * 2 + 1));
        if (limit.isEmpty()) {
            return;
        }

        // A band along each edge that faces the light
        List<Rectangle> bands = new ArrayList<>();
        if (lx < left) {
            bands.add(new Rectangle(left, top, taper, r.height).intersection(limit));
        }
        if (lx > right) {
            bands.add(new Rectangle(right - taper, top, taper, r.height).intersection(limit));
        }
        if (ly < top) {
            bands.add(new Rectangle(left, top, r.width, taper).intersection(limit));
        }
        if (ly > bottom) {
            bands.add(new Rectangle(left, bottom - taper, r.width, taper).intersection(limit));
        }

        for (Rectangle band : bands) {
            if (band.isEmpty()) {
                continue;
            }
            outAreas.add(band);

            // Only other blockers overlapping this band can get in the way inside it
            List<ActiveBlocker> near = new ArrayList<>();
            for (ActiveBlocker o : blockers) {
                if (o != ref && o.bounds.intersects(band)) {
                    near.add(o);
                }
            }

            for (int y = band.y; y < band.y + band.height; y++) {
                int row = y * width;
                double py = y + 0.5;
                double dy = py - ly;
                for (int x = band.x; x < band.x + band.width; x++) {
                    double px = x + 0.5;
                    double dx = px - lx;

                    // Where along the ray (0 = light, 1 = this pixel) it enters the rectangle
                    double tx = dx > 0 ? (left - lx) / dx : dx < 0 ? (right - lx) / dx : Double.NEGATIVE_INFINITY;
                    double ty = dy > 0 ? (top - ly) / dy : dy < 0 ? (bottom - ly) / dy : Double.NEGATIVE_INFINITY;
                    double tEnter = Math.max(tx, ty);
                    if (tEnter <= 0 || tEnter >= 1) {
                        continue;
                    }

                    double len = Math.sqrt(dx * dx + dy * dy);
                    double depth = (1 - tEnter) * len; // how far the light has travelled inside
                    if (depth > taper) {
                        continue;
                    }

                    // Light strength just outside the edge, where it hits the face
                    double tHit = Math.max(0, tEnter - 1.5 / len);
                    int hx = (int) Math.floor(lx + dx * tHit);
                    int hy = (int) Math.floor(ly + dy * tHit);
                    float incoming = light.getBrightnessAt(hx, hy);
                    if (incoming <= 0f) {
                        continue;
                    }
                    if (hasShadow && hx >= sx0 && hx <= sx1 && hy >= sy0 && hy <= sy1) {
                        int shade = maskPx[hy * width + hx] & 0xFF; // does something else shade the face?
                        if (shade == 255) {
                            continue;
                        }
                        incoming *= (255 - shade) / 255f;
                    }

                    double ex = lx + dx * tEnter;
                    double ey = ly + dy * tEnter;
                    if (taperBlocked(near, x, y, ex, ey, px, py)) {
                        continue;
                    }

                    // Full strength at the edge, fading evenly to 0 at the taper distance
                    float strength = incoming * (float) (1.0 - depth / taper);

                    int i = row + x;
                    if (strength > reflectBuf[i]) {
                        reflectBuf[i] = strength;
                    }
                }
            }
        }
    }

    /**
     * Whether another blocker sits between where the light entered a reflector and the pixel being lit.
     * Overlapping REFLECT blockers (like a wall and floor whose boxes overlap in a corner) don't block
     * each other's surfaces, but anything else in the way does.
     */
    private static boolean taperBlocked(List<ActiveBlocker> near, int x, int y,
                                        double ex, double ey, double px, double py) {
        for (ActiveBlocker o : near) {
            if (o.reflect && o.bounds.contains(x, y)) {
                continue;
            }
            if (o.bounds.intersectsLine(ex, ey, px, py)) {
                return true;
            }
        }
        return false;
    }

    /** Adds the collected reflection for one light to the light buffers and clears the scratch buffer. */
    private void flushReflection(LightPoint light, Rectangle area, int width) {
        float red = light.getRed();
        float green = light.getGreen();
        float blue = light.getBlue();
        for (int y = area.y; y < area.y + area.height; y++) {
            int row = y * width;
            for (int x = area.x; x < area.x + area.width; x++) {
                int i = row + x;
                float s = reflectBuf[i];
                if (s > 0f) {
                    redBuf[i] += s * red;
                    greenBuf[i] += s * green;
                    blueBuf[i] += s * blue;
                    reflectBuf[i] = 0f;
                }
            }
        }
    }
}