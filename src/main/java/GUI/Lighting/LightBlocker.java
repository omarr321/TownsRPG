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
 * It never draws anything directly on screen. Its bounds define its position, and its tag determines
 * how it interacts with light.
 * <p>
 * It can also dynamically track a component or a room object in the drawn layer, allowing shadows
 * and light blocking to update automatically when objects move or animate.
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
     * Constructs a blocker at a fixed position and size.
     * @param x The x position, in the same coordinates as the drawn layer.
     * @param y The y position, in the same coordinates as the drawn layer.
     * @param width The width of the blocker area.
     * @param height The height of the blocker area.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     */
    public LightBlocker(int x, int y, int width, int height, LightTag tag) {
        super(null);
        this.tag = tag;
        this.setOpaque(false);
        this.setBounds(x, y, width, height);
    }

    /**
     * Constructs a blocker that tracks a component in the drawn layer.
     * @param follow The drawn Component to follow.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     */
    public LightBlocker(Component follow, LightTag tag) {
        this(follow.getX(), follow.getY(), follow.getWidth(), follow.getHeight(), tag);
        this.follow = follow;
    }

    /**
     * Constructs a blocker that tracks a component in the drawn layer with a specified reflection distance.
     * @param follow The drawn Component to follow.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     * @param reflectDist The reflection distance configuration.
     */
    public LightBlocker(Component follow, LightTag tag, int reflectDist) {
        this(follow, tag);
        setReflectDist(reflectDist);
    }

    /**
     * Constructs a blocker that tracks a component in the drawn layer with a specified lit blend factor.
     * @param follow The drawn Component to follow.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     * @param litBlend The lighting blend factor.
     */
    public LightBlocker(Component follow, LightTag tag, float litBlend) {
        this(follow, tag);
        setLitBlend(litBlend);
    }


    /**
     * Constructs a blocker shaped like a room object's corner coordinates.
     * @param follow The {@link BasicObj} room object to follow.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     */
    public LightBlocker(BasicObj follow, LightTag tag) {
        this(0, 0, 0, 0, tag);
        this.followObj = follow;
        Rectangle r = getLightBounds();
        this.setBounds(r);
    }

    /**
     * Constructs a blocker shaped like a room object's corners with a specified reflection distance.
     * @param follow The {@link BasicObj} room object to follow.
     * @param tag The {@link LightTag} defining what this blocker does to light.
     * @param reflectDist The reflection distance configuration.
     */
    public LightBlocker(BasicObj follow, LightTag tag, int reflectDist) {
        this(follow, tag);
        setReflectDist(reflectDist);
    }

    /**
     * Gets the light tag representing this blocker's behavior.
     * @return The LightTag enum value.
     */
    public LightTag getTag() {
        return tag;
    }

    /**
     * Sets the light tag representing this blocker's behavior.
     * @param tag The new LightTag to set.
     */
    public void setTag(LightTag tag) {
        this.tag = tag;
    }

    /**
     * Gets the component being tracked by this blocker, if any.
     * @return The followed Component, or null.
     */
    public Component getFollow() {
        return follow;
    }

    /**
     * Sets the drawn component to follow, or null to revert to the blocker's own fixed bounds.
     * @param follow The Component to follow.
     */
    public void setFollow(Component follow) {
        this.follow = follow;
    }

    /**
     * Gets the reflection distance for this blocker.
     * @return The reflection distance, or -1 if not set.
     */
    public int getReflectDist() {
        return reflectDist;
    }

    /**
     * Sets the reflection distance for this blocker.
     * @param reflectDist The reflection distance value (values {@code <= 0} are normalized to -1).
     */
    public void setReflectDist(int reflectDist) {
        this.reflectDist = reflectDist > 0 ? reflectDist : -1;
    }

    /**
     * Gets the lit blend factor for this blocker.
     * @return The lit blend float value, or -1f if not set.
     */
    public float getLitBlend() {
        return this.litBlend;
    }

    /**
     * Sets the lit blend factor for this blocker.
     * @param litBlend The lit blend value (clamped between 0.0 and 1.0; values {@code < 0} normalize to -1f).
     */
    public void setLitBlend(float litBlend) {
        this.litBlend = litBlend < 0f ? -1f : Math.min(1f, litBlend);
    }

    /**
     * Calculates and returns the screen area currently covered by this blocker.
     * @return A Rectangle representing the active light bounds.
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
     * Renders the followed component onto a transparent image to extract alpha/transparency data for accurate lighting.
     * @param area The region to draw, in layer coordinates.
     * @return A BufferedImage containing the alpha shape, or null if nothing is followed.
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
     * Marks the blocker's alpha data as dirty, forcing a redraw of its shape mask on the next frame.
     */
    public void markAlphaDirty() {
        this.alphaDirty = true;
    }

    /**
     * Sets whether this blocker tracks animated content that changes every frame.
     * @param animated True to disable caching and redraw the alpha image every frame.
     */
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    /**
     * Checks if this blocker is configured for animated content.
     * @return True if animation tracking is enabled, false otherwise.
     */
    public boolean isAnimated() {
        return animated;
    }

    /**
     * Determines whether this light blocker is currently active and affecting light.
     * @return True if visible, active, and covering a non-empty area; false otherwise.
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
     * Defines how a light blocker reacts to light rays.
     */
    public enum LightTag {
        /** Stops all light, casting a shadow behind it. */
        BLOCK,
        /** Stops all light like BLOCK, but the lit edges glow outward with some of the light that hit them. */
        REFLECT,
        /** Stops all light like BLOCK, but the whole object glows the average of all lights hitting it. */
        LIT
    }

}