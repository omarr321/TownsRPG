package GUI.Lighting;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Rectangle;

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
    private int reflectDist = -1;

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

    /**
     * The area this blocker covers right now.
     * @return The followed component's bounds if there is one, otherwise this blocker's own bounds.
     */
    public Rectangle getLightBounds() {
        if (follow != null) {
            return follow.getBounds();
        }
        return getBounds();
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
        REFLECT
    }

}