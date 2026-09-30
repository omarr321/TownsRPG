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

    public LightLayer() {
        super(null);
        this.setOpaque(false);
    }

    /** Makes a light layer the same size as the drawn layer. */
    public LightLayer(int width, int height) {
        this();
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
    }

    /** Adds a blocker at a fixed spot. */
    public LightBlocker addBlocker(int x, int y, int width, int height, LightTag tag) {
        return addBlocker(new LightBlocker(x, y, width, height, tag));
    }

    /** Adds a blocker that follows a component in the drawn layer. */
    public LightBlocker addBlocker(Component follow, LightTag tag) {
        return addBlocker(new LightBlocker(follow, tag));
    }

    public LightBlocker addBlocker(LightBlocker blocker) {
        this.add(blocker);
        return blocker;
    }

    public void removeBlocker(LightBlocker blocker) {
        this.remove(blocker);
    }

    public void clearBlockers() {
        this.removeAll();
    }

    /** Every blocker in this layer, whether active or not. */
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
     * The closest spot to (x, y) that isn't inside any active blocker, staying inside a width x height area.
     * Uses the same edge rule as LightMgmt (edges count as inside), so a light placed here is always blocked properly.
     * @return (x, y) itself if it's already free, or null if there is no free spot at all.
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