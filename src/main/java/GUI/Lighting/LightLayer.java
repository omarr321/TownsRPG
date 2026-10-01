package GUI.Lighting;

import GUI.Lighting.LightBlocker.LightTag;
import Helper.Point;

import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The light layer: a panel holding nothing but {@link LightBlocker}s.
 * It is never put on screen. {@link LightMgmt} reads it to know what blocks or reflects light,
 * and uses the same coordinates as the drawn layer it lights.
 */
public class LightLayer extends JPanel {

    /**
     * Constructs an empty light layer with no size.
     */
    public LightLayer() {
        super(null);
        this.setOpaque(false);
    }

    /**
     * Constructs an empty light layer with a size of the provided width and height.
     * @param width - The width of the light layer.
     * @param height - The height of the light layer.
     */
    public LightLayer(int width, int height) {
        this();
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
    }

    /**
     * Adds a LightBlocker to the Light layer using the provided dimensions.
     * @param x The X of the top left corner of the LightBlocker.
     * @param y The Y of the top left corner of the LightBlocker.
     * @param width The width of the LightBlocker.
     * @param height The height of the LightBlocker.
     * @param tag The tag of the LightBLoker.
     * @return The LightBlocker that was added and created.
     */
    public LightBlocker addBlocker(int x, int y, int width, int height, LightTag tag) {
        return addBlocker(new LightBlocker(x, y, width, height, tag));
    }

    /**
     * Adds a LightBlocker to the Light layer. This LightBlocker will follow the Component provided.
     * @param follow The component to follow.
     * @param tag The tag for what type of light it is.
     * @return The Blocker that was added and created.
     */
    public LightBlocker addBlocker(Component follow, LightTag tag) {
        return addBlocker(new LightBlocker(follow, tag));
    }

    /**
     * Adds a LightBlocker to the Light layer.
     * @param blocker The blocker to add.
     * @return The Blocker that was provided.
     */
    public LightBlocker addBlocker(LightBlocker blocker) {
        this.add(blocker);
        return blocker;
    }

    /**
     * Removes a blocker from the light layer.
     * @param blocker The blocker to remove from the light layer.
     */
    public void removeBlocker(LightBlocker blocker) {
        this.remove(blocker);
    }

    /**
     * Removes all blockers from the light layer.
     */
    public void clearBlockers() {
        this.removeAll();
    }

    /**
     * Gets the blocker contained in the light layer.
     * @return A list of all LightBlockers in the light layer.
     */
    public List<LightBlocker> getBlockers() {
        List<LightBlocker> blockers = new ArrayList<>();
        for (Component c : getComponents()) {
            if (c instanceof LightBlocker) {
                blockers.add((LightBlocker) c);
            }
        }
        return blockers;
    }

    /**
     * Finds the closest spot to ({@code x}, {@code y}) that isn't inside any active blocker,
     * staying inside a {@code width} by {@code height} area.
     *
     * <p>Uses the same edge rule as LightMgmt (edges count as inside), so a light placed
     * here is always blocked properly.
     *
     * @param x      the x coordinate of the desired spot
     * @param y      the y coordinate of the desired spot
     * @param width  the width of the area to stay inside; valid x values are {@code 0} to {@code width - 1}
     * @param height the height of the area to stay inside; valid y values are {@code 0} to {@code height - 1}
     * @return the point ({@code x}, {@code y}) itself if it is already free, the nearest free
     *         point if it is not, or {@code null} if there is no free spot at all
     */
    public Point nearestFreePoint(int x, int y, int width, int height) {
        List<Rectangle> rects = new ArrayList<>();
        for (LightBlocker b : getBlockers()) {
            if (b.isActive()) {
                rects.add(b.getLightBounds());
            }
        }
        if (isFree(x, y, rects)) {
            return new Point(x, y);
        }

        // The closest free spot is always just outside some blocker edge, so only those lines need checking
        List<Integer> xs = new ArrayList<>();
        List<Integer> ys = new ArrayList<>();
        xs.add(x);
        ys.add(y);
        for (Rectangle r : rects) {
            xs.add(r.x - 1);
            xs.add(r.x + r.width + 1);
            ys.add(r.y - 1);
            ys.add(r.y + r.height + 1);
        }

        Point best = null;
        long bestDist = Long.MAX_VALUE;
        for (int cx : xs) {
            if (cx < 0 || cx >= width) continue;
            for (int cy : ys) {
                if (cy < 0 || cy >= height) continue;
                long dx = cx - x;
                long dy = cy - y;
                long dist = dx * dx + dy * dy;
                if (dist < bestDist && isFree(cx, cy, rects)) {
                    best = new Point(cx, cy);
                    bestDist = dist;
                }
            }
        }
        return best;
    }

    private static boolean isFree(int x, int y, List<Rectangle> rects) {
        for (Rectangle r : rects) {
            if (x >= r.x && x <= r.x + r.width && y >= r.y && y <= r.y + r.height) {
                return false;
            }
        }
        return true;
    }
}