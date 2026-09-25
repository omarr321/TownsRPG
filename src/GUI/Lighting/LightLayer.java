package GUI.Lighting;

import GUI.Lighting.LightBlocker.LightTag;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
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
}