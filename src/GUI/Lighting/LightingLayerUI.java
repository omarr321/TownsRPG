package GUI.Lighting;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class LightingLayerUI extends LayerUI<JComponent> {

    private LightMgmt lightMgmt;
    private BufferedImage image;

    public LightingLayerUI(LightMgmt lightMgmt) {
        this.lightMgmt = lightMgmt;
    }

    public LightMgmt getLightMgmt() {
        return lightMgmt;
    }

    public void setLightMgmt(LightMgmt lightMgmt) {
        this.lightMgmt = lightMgmt;
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

        // Fill with the wrapped panel's background in case it isn't opaque
        JLayer<?> layer = (JLayer<?>) c;
        Component view = layer.getView();
        if (view != null) {
            imageG.setColor(view.getBackground());
            imageG.fillRect(0, 0, width, height);
        }

        // 1. Paint the wrapped panel and all of its children into the image
        super.paint(imageG, c);
        imageG.dispose();

        // 2. Light the image
        lightMgmt.applyTo(image);

        // 3. Put the lit image on screen
        g.drawImage(image, 0, 0, null);
    }
}