package GUI.CustomPanels;

import GUI.CustomPanels.QuadrilateralPanel.PointLocation;
import Helper.ImageLoader;
import Helper.NoDefaultImageLoader;
import Helper.Point;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class QuadrilateralPanelTest {

    /** A plain 100 x 50 rectangle whose top left corner is at (10, 20). */
    private static Point[] rect() {
        return new Point[]{new Point(10, 20), new Point(110, 20), new Point(110, 70), new Point(10, 70)};
    }

    /** A 100 x 100 shape with a slanted bottom edge, so its bottom right corner area is empty. */
    private static Point[] trapezoid() {
        return new Point[]{new Point(0, 0), new Point(100, 0), new Point(100, 50), new Point(0, 100)};
    }

    private static BufferedImage render(QuadrilateralPanel panel) {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        panel.print(g);
        g.dispose();
        return image;
    }

    private static boolean hasVisiblePixel(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---- PointLocation enum ----

    @Test
    void pointLocationNumbersMatchCornerOrder() {
        assertEquals(0, PointLocation.TOP_LEFT.getPointToNum());
        assertEquals(1, PointLocation.TOP_RIGHT.getPointToNum());
        assertEquals(2, PointLocation.BOTTOM_RIGHT.getPointToNum());
        assertEquals(3, PointLocation.BOTTOM_LEFT.getPointToNum());
        assertEquals(4, PointLocation.values().length);
    }

    // ---- bounds ----

    @Test
    void colorConstructorBoundsMatchThePoints() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), Color.RED);

        assertEquals(new Rectangle(10, 20, 100, 50), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void colorConstructorBoundsUseTheExtremesOfAnIrregularShape() {
        Point[] points = {new Point(30, 5), new Point(90, 15), new Point(70, 80), new Point(10, 60)};

        QuadrilateralPanel panel = new QuadrilateralPanel(points, Color.RED);

        assertEquals(new Rectangle(10, 5, 80, 75), panel.getBounds());
    }

    @Test
    void borderConstructorDefaultsToThicknessOne() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), Color.RED, Color.BLACK);

        // bounds grow by the thickness on every side so the border isn't clipped
        assertEquals(new Rectangle(9, 19, 102, 52), panel.getBounds());
    }

    @Test
    void borderConstructorPadsBoundsByThickness() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), Color.RED, Color.BLACK, 3);

        assertEquals(new Rectangle(7, 17, 106, 56), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void imageConstructorBoundsMatchThePoints() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/Crate.png");

        assertEquals(new Rectangle(10, 20, 100, 50), panel.getBounds());
        assertFalse(panel.isOpaque());
    }

    @Test
    void imageBorderConstructorDefaultsToThicknessOne() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/Crate.png", Color.BLACK);

        assertEquals(new Rectangle(9, 19, 102, 52), panel.getBounds());
    }

    @Test
    void imageBorderConstructorPadsBoundsByThickness() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/Crate.png", Color.BLACK, 5);

        assertEquals(new Rectangle(5, 15, 110, 60), panel.getBounds());
    }

    @Test
    void nullPointsAreRejected() {
        assertThrows(NullPointerException.class, () -> new QuadrilateralPanel(null, Color.RED));
    }

    // ---- getPoint ----

    @Test
    void getPointReturnsEachCorner() {
        Point[] points = rect();
        QuadrilateralPanel panel = new QuadrilateralPanel(points, Color.RED);

        assertSame(points[0], panel.getPoint(PointLocation.TOP_LEFT));
        assertSame(points[1], panel.getPoint(PointLocation.TOP_RIGHT));
        assertSame(points[2], panel.getPoint(PointLocation.BOTTOM_RIGHT));
        assertSame(points[3], panel.getPoint(PointLocation.BOTTOM_LEFT));
    }

    // ---- painting a solid color ----

    @Test
    void solidShapeIsFilledWithItsColor() {
        BufferedImage image = render(new QuadrilateralPanel(rect(), Color.RED));

        assertEquals(Color.RED.getRGB(), image.getRGB(50, 25));
        assertEquals(Color.RED.getRGB(), image.getRGB(5, 5));
    }

    @Test
    void solidShapeLeavesOutsideOfTheShapeTransparent() {
        BufferedImage image = render(new QuadrilateralPanel(trapezoid(), Color.RED));

        // inside the slanted shape
        assertEquals(Color.RED.getRGB(), image.getRGB(20, 20));
        // bottom right corner of the bounds is outside the shape
        assertEquals(0, image.getRGB(95, 95) >>> 24);
    }

    @Test
    void borderIsDrawnOnTopOfTheFill() {
        BufferedImage image = render(new QuadrilateralPanel(rect(), Color.RED, Color.BLACK, 4));

        // the top edge sits 4px into the image (the padding), and the stroke is 4px wide around it
        assertEquals(Color.BLACK.getRGB(), image.getRGB(60, 4));
        assertEquals(Color.RED.getRGB(), image.getRGB(60, 25));
    }

    @Test
    void borderHasNoEffectWhenThicknessIsZero() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), Color.RED, Color.BLACK, 0);

        BufferedImage image = render(panel);

        assertEquals(new Rectangle(10, 20, 100, 50), panel.getBounds());
        assertEquals(Color.RED.getRGB(), image.getRGB(50, 25));
    }

    // ---- painting images ----

    @Test
    void warpedImagePaintsSomething() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/walls/BrickWall.png");

        BufferedImage image = assertDoesNotThrow(() -> render(panel));

        assertTrue(hasVisiblePixel(image));
    }

    @Test
    void warpedImageOnASlantedShapeLeavesOutsideTransparent() {
        QuadrilateralPanel panel = new QuadrilateralPanel(trapezoid(), "/images/walls/BrickWall.png");

        BufferedImage image = render(panel);

        assertEquals(0, image.getRGB(95, 95) >>> 24);
        assertTrue(hasVisiblePixel(image));
    }

    @Test
    void unwarpedImagePaintsSomething() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/walls/BrickWall.png");
        panel.setImageWarp(false);

        BufferedImage image = assertDoesNotThrow(() -> render(panel));

        assertTrue(hasVisiblePixel(image));
    }

    @Test
    void unwarpedImageIsClippedToTheShape() {
        QuadrilateralPanel panel = new QuadrilateralPanel(trapezoid(), "/images/walls/BrickWall.png");
        panel.setImageWarp(false);

        BufferedImage image = render(panel);

        assertEquals(0, image.getRGB(95, 95) >>> 24);
        assertTrue(hasVisiblePixel(image));
    }

    @Test
    void warpCanBeToggledBackAndForth() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/walls/BrickWall.png");

        panel.setImageWarp(false);
        assertDoesNotThrow(() -> render(panel));

        panel.setImageWarp(true);
        assertDoesNotThrow(() -> render(panel));
    }

    @Test
    void paintingTwiceGivesTheSameResult() {
        // the warp is cached after the first paint, the cached picture must match the first one
        QuadrilateralPanel panel = new QuadrilateralPanel(trapezoid(), "/images/walls/BrickWall.png");

        BufferedImage first = render(panel);
        BufferedImage second = render(panel);

        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                assertEquals(first.getRGB(x, y), second.getRGB(x, y), "pixel " + x + "," + y);
            }
        }
    }

    @Test
    void missingImageFallsBackWithoutThrowing() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/does_not_exist.png");

        BufferedImage image = assertDoesNotThrow(() -> render(panel));

        assertTrue(hasVisiblePixel(image));
    }

    @Test
    void imageBorderIsDrawnOnTopOfTheImage() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/walls/BrickWall.png", Color.MAGENTA, 4);
        panel.setImageWarp(false);

        BufferedImage image = render(panel);

        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(60, 4));
    }

    @Test
    void warpedImagePanelFillsWithMagentaWhenNoImageCanBeLoaded() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/does_not_exist.png") {
            @Override
            ImageLoader createImageLoader(String path) {
                return new NoDefaultImageLoader(path);
            }
        };

        BufferedImage image = render(panel);

        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(50, 25));
    }

    @Test
    void unwarpedImagePanelFillsWithMagentaWhenNoImageCanBeLoaded() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), "/images/does_not_exist.png") {
            @Override
            ImageLoader createImageLoader(String path) {
                return new NoDefaultImageLoader(path);
            }
        };
        panel.setImageWarp(false);

        BufferedImage image = render(panel);

        assertEquals(Color.MAGENTA.getRGB(), image.getRGB(50, 25));
    }

    @Test
    void noBorderIsDrawnWhenThicknessIsSetButBorderColorIsNull() {
        QuadrilateralPanel panel = new QuadrilateralPanel(rect(), Color.RED, (Color) null, 4);

        BufferedImage image = render(panel);

        // with a border this pixel (on the top edge) would be black
        assertEquals(Color.RED.getRGB(), image.getRGB(60, 4));
    }
}
