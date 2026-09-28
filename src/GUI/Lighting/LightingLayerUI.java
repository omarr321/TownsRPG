package GUI.Lighting;

import Helper.GameSettings;
import RoomClasses.RoomObjects.InteractableObj;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;
import java.util.List;
import Helper.Point;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public class LightingLayerUI extends LayerUI<JComponent> {

    private LightMgmt lightMgmt;
    private BufferedImage image;
    private boolean showBlockers = false;
    private boolean showLights = false;
    private boolean showInteractable = false;
    private Supplier<List<InteractableObj>> interactables = List::of;

    public LightingLayerUI(LightMgmt lightMgmt) {
        this.lightMgmt = lightMgmt;
    }

    public LightMgmt getLightMgmt() {
        return lightMgmt;
    }

    public void setLightMgmt(LightMgmt lightMgmt) {
        this.lightMgmt = lightMgmt;
    }

    /** When on, outlines every blocker on top of the lit image (red = BLOCK, cyan = REFLECT, gray = switched off). */
    public void setShowBlockers(boolean showBlockers) {
        this.showBlockers = showBlockers;
    }

    public void setShowLights(boolean showLights) {
        this.showLights = showLights;
    }

    public boolean isShowBlockers() {
        return showBlockers;
    }

    public boolean isShowLights() {
        return showLights;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int width = c.getWidth();
        int height = c.getHeight();
        if (width <= 0 || height <= 0 || lightMgmt == null) {
            super.paint(g, c);
            return;
        }

        // Make (or remake, after a resize) the image everything is drawn into
        if (image == null || image.getWidth() != width || image.getHeight() != height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D imageG = image.createGraphics();

        // Fill with the drawn panel's background in case it isn't opaque
        JLayer<?> layer = (JLayer<?>) c;
        Component view = layer.getView();
        if (view != null) {
            imageG.setColor(view.getBackground());
            imageG.fillRect(0, 0, width, height);
        }

        // 1. Paint the drawn layer and all of its children into the image
        super.paint(imageG, c);
        imageG.dispose();

        // 2. Light the image, using the light layer for shadows and reflections
        lightMgmt.applyTo(image);

        // 3. Put the lit image on screen
        g.drawImage(image, 0, 0, null);

        if (this.showBlockers) {
            this.drawBlockerOutlines((Graphics2D) g.create());
        }

        if (this.showLights) {
            this.drawLightMarkers((Graphics2D) g.create());
        }

        if (this.showInteractable) {
            this.drawInteractableMarkers((Graphics2D) g.create());
        }

    }

    private void drawBlockerOutlines(Graphics2D g) {
        LightLayer lightLayer = lightMgmt.getLightLayer();
        if (lightLayer == null) {
            g.dispose();
            return;
        }
        g.setStroke(new BasicStroke(GameSettings.scale(2)));
        for (LightBlocker blocker : lightLayer.getBlockers()) {
            if (!blocker.isActive()) {
                g.setColor(Color.GRAY);
            } else if (blocker.getTag() == LightBlocker.LightTag.REFLECT) {
                g.setColor(Color.CYAN);
            } else if (blocker.getTag() == LightBlocker.LightTag.BLOCK){
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GREEN);
            }
            Rectangle r = blocker.getLightBounds();
            g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        }
        g.dispose();
    }

    /** Draws a small white square at each light's position. */
    private void drawLightMarkers(Graphics2D g) {
        int size = GameSettings.scale(12);
        int half = size / 2;
        for (LightPoint light : lightMgmt.getLights()) {
            int x = light.getLoc().getX() - half;
            int y = light.getLoc().getY() - half;
            g.setColor(Color.WHITE);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK); // outline so it stays visible on bright areas
            g.drawRect(x, y, size - 1, size - 1);
        }
        g.dispose();
    }

    private void drawInteractableMarkers(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int spacing = GameSettings.scale(16);                 // gap between stripes
        Stroke stripeStroke = new BasicStroke(GameSettings.scale(4));
        Color stripeColor = new Color(255, 220, 0);      // see-through yellow

        float dash = GameSettings.scale(8);
        Stroke outlineStroke = new BasicStroke(GameSettings.scale(2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
                10f, new float[]{dash, dash * 0.75f}, 0f);

        for (InteractableObj obj : interactables.get()) {
            Point[] corners = obj.getShapeCorners();
            if (corners == null || corners.length < 3) continue;

            Polygon shape = new Polygon();
            for (Point p : corners) {
                shape.addPoint(p.getX(), p.getY());
            }

            // Stripes: only draw inside the shape
            Shape oldClip = g.getClip();
            g.clip(shape);
            g.setColor(stripeColor);
            g.setStroke(stripeStroke);

            Rectangle b = shape.getBounds();
            // 45° lines, stepping far enough left and right to cover the whole box
            for (int i = -b.height; i < b.width; i += spacing) {
                g.drawLine(b.x + i, b.y + b.height, b.x + i + b.height, b.y);
            }
            g.setClip(oldClip);                               // restore so the outline isn't clipped

            // Outline on top
            g.setColor(Color.YELLOW);
            g.setStroke(outlineStroke);
            g.drawPolygon(shape);
        }
        g.dispose();
    }

    public void setShowInteractable(boolean showInteractable) {
        this.showInteractable = showInteractable;
    }

    public void setInteractables(Supplier<List<InteractableObj>> interactables) {
        this.interactables = interactables != null ? interactables : List::of;
    }
}