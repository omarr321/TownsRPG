package gui.panels;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import helpers.ImageLoader;
import helpers.NoDefaultImageLoader;

import static org.junit.jupiter.api.Assertions.*;

public class RectPanelTest {
    private static final int TRANSPARENT = 0;

    /** Draws the panel onto an image the same size as the panel (print() avoids Swing's double buffering). */
    private static BufferedImage render(RectPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        panel.print(g);
        g.dispose();
        return image;
    }

    private static int rgb(Color color) {
        return color.getRGB();
    }

    // ---- constructors ----

    @Test
    void colorConstructorSetsBoundsAndIsTransparent() {
        RectPanel panel = new RectPanel(5, 6, 30, 40, Color.RED);

        assertEquals(new Rectangle(5, 6, 30, 40), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void colorAndBorderConstructorSetsBounds() {
        RectPanel panel = new RectPanel(1, 2, 20, 30, Color.RED, Color.BLACK);

        assertEquals(new Rectangle(1, 2, 20, 30), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void colorBorderThicknessConstructorSetsBounds() {
        RectPanel panel = new RectPanel(1, 2, 20, 30, Color.RED, Color.BLACK, 4);

        assertEquals(new Rectangle(1, 2, 20, 30), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void imageConstructorSetsBounds() {
        RectPanel panel = new RectPanel(7, 8, 50, 60, "/images/objects/Crate.png");

        assertEquals(new Rectangle(7, 8, 50, 60), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void imageBorderThicknessConstructorSetsBounds() {
        RectPanel panel = new RectPanel(7, 8, 50, 60, "/images/objects/Crate.png", Color.BLACK, 3);

        assertEquals(new Rectangle(7, 8, 50, 60), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    // ---- painting a solid color ----

    @Test
    void solidPanelFillsWholeAreaWithColor() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED));

        assertEquals(rgb(Color.RED), image.getRGB(0, 0));
        assertEquals(rgb(Color.RED), image.getRGB(10, 10));
        assertEquals(rgb(Color.RED), image.getRGB(19, 19));
    }

    @Test
    void solidPanelUsesItsOwnFillColor() {
        BufferedImage image = render(new RectPanel(0, 0, 10, 10, Color.GREEN));

        assertEquals(rgb(Color.GREEN), image.getRGB(5, 5));
    }

    @Test
    void panelPositionDoesNotMoveThePaintedArea() {
        // x/y only place the panel in its parent, painting always starts at the panel's own 0,0
        BufferedImage image = render(new RectPanel(100, 200, 10, 10, Color.BLUE));

        assertEquals(rgb(Color.BLUE), image.getRGB(0, 0));
        assertEquals(rgb(Color.BLUE), image.getRGB(9, 9));
    }

    @Test
    void panelWithoutBorderHasNoBorder() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED));

        assertEquals(rgb(Color.RED), image.getRGB(0, 0));
        assertEquals(rgb(Color.RED), image.getRGB(19, 0));
    }

    // ---- borders ----

    @Test
    void defaultBorderIsOnePixelThick() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED, Color.BLACK));

        assertEquals(rgb(Color.BLACK), image.getRGB(0, 0));
        assertEquals(rgb(Color.BLACK), image.getRGB(19, 19));
        assertEquals(rgb(Color.BLACK), image.getRGB(10, 0));
        assertEquals(rgb(Color.RED), image.getRGB(1, 1));
        assertEquals(rgb(Color.RED), image.getRGB(10, 10));
    }

    @Test
    void borderThicknessIsRespected() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED, Color.BLACK, 3));

        // pixels 0, 1 and 2 from each edge are border, pixel 3 is fill
        for (int i = 0; i < 3; i++) {
            assertEquals(rgb(Color.BLACK), image.getRGB(i, 10), "left border pixel " + i);
            assertEquals(rgb(Color.BLACK), image.getRGB(19 - i, 10), "right border pixel " + i);
            assertEquals(rgb(Color.BLACK), image.getRGB(10, i), "top border pixel " + i);
            assertEquals(rgb(Color.BLACK), image.getRGB(10, 19 - i), "bottom border pixel " + i);
        }
        assertEquals(rgb(Color.RED), image.getRGB(3, 10));
        assertEquals(rgb(Color.RED), image.getRGB(10, 3));
        assertEquals(rgb(Color.RED), image.getRGB(16, 10));
        assertEquals(rgb(Color.RED), image.getRGB(10, 16));
    }

    @Test
    void borderColorIsUsed() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED, Color.BLUE, 2));

        assertEquals(rgb(Color.BLUE), image.getRGB(0, 0));
        assertEquals(rgb(Color.BLUE), image.getRGB(1, 1));
        assertEquals(rgb(Color.RED), image.getRGB(2, 2));
    }

    @Test
    void thicknessOfZeroDrawsNoBorder() {
        BufferedImage image = render(new RectPanel(0, 0, 20, 20, Color.RED, Color.BLACK, 0));

        assertEquals(rgb(Color.RED), image.getRGB(0, 0));
    }

    @Test
    void borderThickerThanHalfThePanelStillPaints() {
        RectPanel panel = new RectPanel(0, 0, 10, 10, Color.RED, Color.BLACK, 20);

        assertDoesNotThrow(() -> render(panel));
    }

    // ---- images ----

    @Test
    void imagePanelPaintsSomething() {
        BufferedImage image = render(new RectPanel(0, 0, 40, 40, "/images/walls/BrickWall.png"));

        assertTrue(hasVisiblePixel(image), "Expected the image to draw at least one visible pixel");
    }

    @Test
    void missingImageFallsBackWithoutThrowing() {
        RectPanel panel = new RectPanel(0, 0, 40, 40, "/images/does_not_exist.png");

        BufferedImage image = assertDoesNotThrow(() -> render(panel));

        assertTrue(hasVisiblePixel(image), "Expected the fallback image to draw something");
    }

    @Test
    void imagePanelWithBorderDrawsBorderOnTop() {
        BufferedImage image = render(new RectPanel(0, 0, 40, 40, "/images/walls/BrickWall.png", Color.MAGENTA, 2));

        assertEquals(rgb(Color.MAGENTA), image.getRGB(0, 0));
        assertEquals(rgb(Color.MAGENTA), image.getRGB(1, 20));
        assertEquals(rgb(Color.MAGENTA), image.getRGB(39, 39));
    }

    @Test
    void imageIsScaledToThePanelSize() {
        RectPanel small = new RectPanel(0, 0, 10, 10, "/images/walls/BrickWall.png");
        RectPanel big = new RectPanel(0, 0, 80, 80, "/images/walls/BrickWall.png");

        assertEquals(10, render(small).getWidth());
        assertEquals(80, render(big).getWidth());
        assertTrue(hasVisiblePixelNearCorner(render(big)));
    }

    @Test
    void imagePanelFillsWithMagentaWhenNoImageCanBeLoaded() {
        RectPanel panel = new RectPanel(0, 0, 20, 20, "/images/does_not_exist.png") {
            @Override
            ImageLoader createImageLoader(String path) {
                return new NoDefaultImageLoader(path);
            }
        };

        BufferedImage image = render(panel);

        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(0, 0));
        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(10, 10));
        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(19, 19));
    }

    private static boolean hasVisiblePixel(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != TRANSPARENT && (image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasVisiblePixelNearCorner(BufferedImage image) {
        int limit = Math.min(image.getWidth(), 20);
        for (int y = image.getHeight() - limit; y < image.getHeight(); y++) {
            for (int x = image.getWidth() - limit; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
