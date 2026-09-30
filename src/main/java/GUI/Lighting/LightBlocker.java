package GUI.Lighting;

import GUI.CustomPanels.QuadrilateralPanel;
import Helper.Point;
import RoomClasses.RoomObjects.BasicObj;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * An empty, invisible panel that lives in a {@link LightLayer} and marks an area that affects light.
 * It never draws anything. Its bounds say where it is, and its tag says what it does to light.
 * <p>
 * It can also follow a component in the drawn layer, so it moves when that component moves.
 * </p>
 */
public class LightBlocker extends JPanel {
    private LightTag tag;
    private Component follow;
    private BasicObj followObj;   // a room object whose corner shape this blocker uses
    private int reflectDist = -1;
    private float litBlend = -1f;

    // The followed component drawn on its own, reused between frames
    private BufferedImage alphaImage;

    // What alphaImage was last drawn from, so it's only redrawn when something changes
    private Object cachedSource;
    private Rectangle cachedArea;
    private Rectangle cachedSourceBounds;
    private int[] cachedShapeKey;
    private boolean alphaDirty = true;
    private boolean animated = false;

    // For a followed room object with an image: an off-screen copy of how it's drawn, used only to read
    // its alpha. It's never put on screen. Rebuilt when the object's corners, image or warp change.
    private QuadrilateralPanel objPanel;
    private int[] objPanelKey;

    /**
     * Makes a blocker at a fixed spot.
     * @param x The x position, in the same coordinates as the drawn layer.
     * @param y The y position, in the same coordinates as the drawn layer.
     * @param width The width.
     * @param height The height.
     * @param tag What this blocker does to light.
     */
    public LightBlocker(int x, int y, int width, int height, LightTag tag) {
        super(null);
        this.tag = tag;
        this.setOpaque(false);
        this.setBounds(x, y, width, height);
    }

    /**
     * Makes a blocker that follows a component in the drawn layer.
     * Every frame it uses that component's current bounds, so moving the component moves its shadow.
     * The component should be placed directly in the drawn panel so the coordinates match.
     * @param follow The drawn component to follow.
     * @param tag What this blocker does to light.
     */
    public LightBlocker(Component follow, LightTag tag) {
        this(follow.getX(), follow.getY(), follow.getWidth(), follow.getHeight(), tag);
        this.follow = follow;
    }

    public LightBlocker(Component follow, LightTag tag, int reflectDist) {
        this(follow, tag);
        setReflectDist(reflectDist);
    }
    public LightBlocker(Component follow, LightTag tag, float litBlend) {
        this(follow, tag);
        setLitBlend(litBlend);
    }


    /**
     * Makes a blocker shaped like a room object's corners. It follows the object, so if its corners
     * move, the blocker moves with them.
     */
    public LightBlocker(BasicObj follow, LightTag tag) {
        this(0, 0, 0, 0, tag);
        this.followObj = follow;
        Rectangle r = getLightBounds();
        this.setBounds(r);
    }

    public LightBlocker(BasicObj follow, LightTag tag, int reflectDist) {
        this(follow, tag);
        setReflectDist(reflectDist);
    }

    public LightTag getTag() {
        return tag;
    }

    public void setTag(LightTag tag) {
        this.tag = tag;
    }

    public Component getFollow() {
        return follow;
    }

    /** Sets the drawn component to follow, or null to go back to this blocker's own bounds. */
    public void setFollow(Component follow) {
        this.follow = follow;
    }

    public int getReflectDist() {
        return reflectDist;
    }

    public void setReflectDist(int reflectDist) {
        this.reflectDist = reflectDist > 0 ? reflectDist : -1;
    }

    public float getLitBlend() {
        return this.litBlend;
    }

    public void setLitBlend(float litBlend) {
        this.litBlend = litBlend < 0f ? -1f : Math.min(1f, litBlend);
    }

    /**
     * The area this blocker covers right now.
     * @return The followed component's bounds if there is one, otherwise this blocker's own bounds.
     */
    public Rectangle getLightBounds() {
        if (follow != null) {
            return follow.getBounds();
        }
        Polygon shape = objShape();
        if (shape != null) {
            return shape.getBounds();
        }
        return getBounds();
    }

    /**
     * An off-screen panel drawn the same way as the followed room object, so its image's transparency
     * can be read. Null if there's no room object, or it has no image.
     */
    private QuadrilateralPanel objPanel() {
        if (followObj == null) {
            return null;
        }
        String path = followObj.getImagePath();
        Point[] corners = followObj.getShapeCorners();
        if (path == null || path.isEmpty() || corners == null || corners.length != 4) {
            return null;
        }
        // Rebuild if anything that changes the drawing has changed
        int[] key = new int[9];
        for (int i = 0; i < 4; i++) {
            key[i * 2] = corners[i].getX();
            key[i * 2 + 1] = corners[i].getY();
        }
        key[8] = (followObj.getWarped() ? 1 : 0) ^ (path.hashCode() << 1);
        if (objPanel == null || !Arrays.equals(key, objPanelKey)) {
            objPanel = new QuadrilateralPanel(corners, path);
            objPanel.setImageWarp(followObj.getWarped());
            objPanelKey = key;
        }
        return objPanel;
    }

    /** The followed room object's corners as a polygon, or null if there isn't one. */
    private Polygon objShape() {
        if (followObj == null) {
            return null;
        }
        Point[] corners = followObj.getShapeCorners();
        if (corners == null || corners.length < 3) {
            return null;
        }
        Polygon shape = new Polygon();
        for (Point p : corners) {
            shape.addPoint(p.getX(), p.getY());
        }
        return shape;
    }

    /**
     * The followed component drawn on its own onto a transparent image, so the alpha shows its real shape.
     * Only the given area is drawn (normally the on-screen part), and the image's top-left is that area's
     * top-left. Redrawn on every call, so animated sprites stay up to date.
     * @param area The part to draw, in the drawn layer's coordinates.
     * @return The image, or null if this blocker follows nothing (the whole box then counts as solid).
     */
    public BufferedImage getAlphaImage(Rectangle area) {
        if (area == null || area.isEmpty()) {
            return null;
        }
        Component source = follow != null ? follow : objPanel();
        Polygon shape = source == null ? objShape() : null;
        if (source == null && shape == null) {
            return null;
        }
        int w = area.width;
        int h = area.height;

        // Reuse last frame's drawing if nothing that affects it has changed
        Object sourceKey = source != null ? source : followObj;
        Rectangle sourceBounds = source != null ? source.getBounds() : shape.getBounds();
        int[] shapeKey = shape != null ? shapeKey(shape) : null;
        if (!animated && !alphaDirty && alphaImage != null
                && sourceKey == cachedSource
                && area.equals(cachedArea)
                && sourceBounds.equals(cachedSourceBounds)
                && Arrays.equals(shapeKey, cachedShapeKey)) {
            return alphaImage;
        }

        if (alphaImage == null || alphaImage.getWidth() != w || alphaImage.getHeight() != h) {
            alphaImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g = alphaImage.createGraphics();
        g.setComposite(AlphaComposite.Clear); // wipe last frame's drawing
        g.fillRect(0, 0, w, h);
        g.setComposite(AlphaComposite.SrcOver);
        g.clip(new Rectangle(0, 0, w, h));
        if (source != null) {
            g.translate(source.getX() - area.x, source.getY() - area.y);
            // print() instead of paint(): during a repaint, paint() goes through Swing's double buffer,
            // which copies an opaque rectangle in and loses all the transparency
            source.print(g);
        } else {
            // A room object with no image (a plain color): its corner polygon is its shape
            g.translate(-area.x, -area.y);
            g.setColor(Color.WHITE);
            g.fillPolygon(shape);
        }
        g.dispose();

        cachedSource = sourceKey;
        cachedArea = new Rectangle(area);
        cachedSourceBounds = sourceBounds;
        cachedShapeKey = shapeKey;
        alphaDirty = false;
        return alphaImage;
    }

    private static int[] shapeKey(Polygon shape) {
        int[] key = new int[shape.npoints * 2];
        for (int i = 0; i < shape.npoints; i++) {
            key[i * 2] = shape.xpoints[i];
            key[i * 2 + 1] = shape.ypoints[i];
        }
        return key;
    }

    /**
     * Tells the blocker its followed component now looks different (a new image, a changed sprite), so its
     * alpha is redrawn next frame. Moving or resizing is picked up on its own; this is only for changes in
     * what's drawn.
     */
    public void markAlphaDirty() {
        this.alphaDirty = true;
    }

    /**
     * For followed components whose drawing changes every frame (animated sprites): redraws the alpha every
     * frame instead of caching it. Slower, so only switch this on for blockers that need it.
     */
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean isAnimated() {
        return animated;
    }

    /**
     * Whether this blocker should affect light this frame.
     * Call setVisible(false) to switch a blocker off; a followed component that is hidden stops blocking too.
     */
    public boolean isActive() {
        if (!isVisible()) {
            return false;
        }
        if (follow != null && !follow.isVisible()) {
            return false;
        }
        Rectangle r = getLightBounds();
        return r.width > 0 && r.height > 0;
    }

    /**
     * How a blocker reacts to light.
     */
    public enum LightTag {
        /** Stops all light, casting a shadow behind it. */
        BLOCK,
        /** Stops all light like BLOCK, but the lit edges glow outward with some of the light that hit them. */
        REFLECT,
        LIT
    }

}