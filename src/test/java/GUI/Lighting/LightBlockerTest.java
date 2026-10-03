package GUI.Lighting;

import GUI.Lighting.LightBlocker.LightTag;
import Helper.GameSettings;
import Helper.Point;
import RoomClasses.RoomObjects.BasicObj;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class LightBlockerTest {

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private static JPanel component(int x, int y, int w, int h, Color color) {
        JPanel panel = new JPanel();
        panel.setBounds(x, y, w, h);
        panel.setOpaque(true);
        panel.setBackground(color);
        return panel;
    }

    private static Point[] rectCorners() {
        return new Point[]{new Point(10, 20), new Point(110, 20), new Point(110, 70), new Point(10, 70)};
    }

    private static Point[] trapezoidCorners() {
        return new Point[]{new Point(0, 0), new Point(100, 0), new Point(100, 50), new Point(0, 100)};
    }

    private static int alpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }

    // ---- enum ----

    @Test
    void lightTagHasBlockReflectAndLit() {
        assertEquals(3, LightTag.values().length);
        assertNotNull(LightTag.valueOf("BLOCK"));
        assertNotNull(LightTag.valueOf("REFLECT"));
        assertNotNull(LightTag.valueOf("LIT"));
    }

    // ---- fixed-position constructor ----

    @Test
    void fixedConstructorSetsBoundsAndTag() {
        LightBlocker blocker = new LightBlocker(5, 6, 30, 40, LightTag.BLOCK);

        assertEquals(new Rectangle(5, 6, 30, 40), blocker.getBounds());
        assertEquals(LightTag.BLOCK, blocker.getTag());
    }

    @Test
    void fixedBlockerIsInvisibleAndFollowsNothing() {
        LightBlocker blocker = new LightBlocker(5, 6, 30, 40, LightTag.BLOCK);

        assertFalse(blocker.isOpaque());
        assertNull(blocker.getFollow());
    }

    @Test
    void defaultsForReflectDistanceLitBlendAndAnimation() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.REFLECT);

        assertEquals(-1, blocker.getReflectDist());
        assertEquals(-1f, blocker.getLitBlend());
        assertFalse(blocker.isAnimated());
    }

    @Test
    void lightBoundsOfFixedBlockerAreItsOwnBounds() {
        LightBlocker blocker = new LightBlocker(5, 6, 30, 40, LightTag.BLOCK);

        assertEquals(new Rectangle(5, 6, 30, 40), blocker.getLightBounds());
    }

    @Test
    void lightBoundsHandlesBasicObjWithInvalidCorners() {
        // BasicObj with null corners
        BasicObj objNull = new BasicObj((Point[]) null, Color.RED);
        LightBlocker blockerNull = new LightBlocker(objNull, LightTag.BLOCK);
        assertEquals(new Rectangle(0, 0, 0, 0), blockerNull.getLightBounds());

        // BasicObj with fewer than 3 corners
        BasicObj objFew = new BasicObj(new Point[]{new Point(0, 0), new Point(10, 10)}, Color.RED);
        LightBlocker blockerFew = new LightBlocker(objFew, LightTag.BLOCK);
        assertEquals(new Rectangle(0, 0, 0, 0), blockerFew.getLightBounds());
    }

    // ---- follow-a-component constructors ----

    @Test
    void followConstructorCopiesComponentBounds() {
        JPanel target = component(10, 20, 30, 40, Color.RED);

        LightBlocker blocker = new LightBlocker(target, LightTag.REFLECT);

        assertEquals(new Rectangle(10, 20, 30, 40), blocker.getBounds());
        assertSame(target, blocker.getFollow());
        assertEquals(LightTag.REFLECT, blocker.getTag());
    }

    @Test
    void followConstructorWithReflectDistance() {
        JPanel target = component(0, 0, 10, 10, Color.RED);

        LightBlocker blocker = new LightBlocker(target, LightTag.REFLECT, 25);

        assertEquals(25, blocker.getReflectDist());
        assertEquals(-1f, blocker.getLitBlend());
    }

    @Test
    void followConstructorWithLitBlend() {
        JPanel target = component(0, 0, 10, 10, Color.RED);

        LightBlocker blocker = new LightBlocker(target, LightTag.LIT, 0.35f);

        assertEquals(0.35f, blocker.getLitBlend());
        assertEquals(-1, blocker.getReflectDist());
    }

    @Test
    void lightBoundsFollowTheComponentWhenItMoves() {
        JPanel target = component(10, 20, 30, 40, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        target.setBounds(100, 200, 50, 60);

        assertEquals(new Rectangle(100, 200, 50, 60), blocker.getLightBounds());
    }

    @Test
    void setFollowSwitchesWhatIsFollowed() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);
        JPanel target = component(50, 60, 70, 80, Color.RED);

        blocker.setFollow(target);

        assertSame(target, blocker.getFollow());
        assertEquals(new Rectangle(50, 60, 70, 80), blocker.getLightBounds());
    }

    @Test
    void setFollowToNullGoesBackToOwnBounds() {
        JPanel target = component(10, 20, 30, 40, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);
        target.setBounds(500, 500, 5, 5);

        blocker.setFollow(null);

        assertNull(blocker.getFollow());
        assertEquals(new Rectangle(10, 20, 30, 40), blocker.getLightBounds());
    }

    // ---- follow-a-room-object constructors ----

    @Test
    void roomObjectConstructorUsesTheCornerBounds() {
        BasicObj obj = new BasicObj(rectCorners(), Color.RED);

        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);

        assertEquals(new Rectangle(10, 20, 100, 50), blocker.getBounds());
        assertEquals(new Rectangle(10, 20, 100, 50), blocker.getLightBounds());
        assertNull(blocker.getFollow());
    }

    @Test
    void roomObjectConstructorWithReflectDistance() {
        BasicObj obj = new BasicObj(rectCorners(), Color.RED);

        LightBlocker blocker = new LightBlocker(obj, LightTag.REFLECT, 30);

        assertEquals(30, blocker.getReflectDist());
    }

    @Test
    void lightBoundsFollowTheRoomObjectCorners() {
        BasicObj obj = new BasicObj(rectCorners(), Color.RED);
        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);

        obj.setShapeCorners(new Point[]{new Point(0, 0), new Point(20, 0), new Point(20, 10), new Point(0, 10)});

        assertEquals(new Rectangle(0, 0, 20, 10), blocker.getLightBounds());
    }

    // ---- simple properties ----

    @Test
    void setTagChangesTag() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);

        blocker.setTag(LightTag.LIT);

        assertEquals(LightTag.LIT, blocker.getTag());
    }

    @Test
    void setReflectDistKeepsPositiveValues() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.REFLECT);

        blocker.setReflectDist(40);

        assertEquals(40, blocker.getReflectDist());
    }

    @Test
    void setReflectDistTurnsZeroAndNegativeIntoUnset() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.REFLECT);
        blocker.setReflectDist(40);

        blocker.setReflectDist(0);
        assertEquals(-1, blocker.getReflectDist());

        blocker.setReflectDist(40);
        blocker.setReflectDist(-25);
        assertEquals(-1, blocker.getReflectDist());
    }

    @Test
    void setLitBlendKeepsValuesFromZeroToOne() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.LIT);

        blocker.setLitBlend(0f);
        assertEquals(0f, blocker.getLitBlend());

        blocker.setLitBlend(0.6f);
        assertEquals(0.6f, blocker.getLitBlend());

        blocker.setLitBlend(1f);
        assertEquals(1f, blocker.getLitBlend());
    }

    @Test
    void setLitBlendCapsAtOne() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.LIT);

        blocker.setLitBlend(3.5f);

        assertEquals(1f, blocker.getLitBlend());
    }

    @Test
    void setLitBlendTurnsNegativeIntoUnset() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.LIT);
        blocker.setLitBlend(0.5f);

        blocker.setLitBlend(-0.2f);

        assertEquals(-1f, blocker.getLitBlend());
    }

    @Test
    void setAnimatedTogglesAnimation() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);

        blocker.setAnimated(true);
        assertTrue(blocker.isAnimated());

        blocker.setAnimated(false);
        assertFalse(blocker.isAnimated());
    }

    // ---- isActive ----

    @Test
    void blockerWithAreaIsActive() {
        assertTrue(new LightBlocker(0, 0, 10, 10, LightTag.BLOCK).isActive());
    }

    @Test
    void hiddenBlockerIsNotActive() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);

        blocker.setVisible(false);

        assertFalse(blocker.isActive());
    }

    @Test
    void blockerCanBeTurnedBackOn() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);

        blocker.setVisible(false);
        blocker.setVisible(true);

        assertTrue(blocker.isActive());
    }

    @Test
    void blockerWithNoWidthOrHeightIsNotActive() {
        assertFalse(new LightBlocker(0, 0, 0, 10, LightTag.BLOCK).isActive());
        assertFalse(new LightBlocker(0, 0, 10, 0, LightTag.BLOCK).isActive());
    }

    @Test
    void blockerFollowingAHiddenComponentIsNotActive() {
        JPanel target = component(0, 0, 10, 10, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);
        assertTrue(blocker.isActive());

        target.setVisible(false);

        assertFalse(blocker.isActive());
    }

    @Test
    void blockerFollowingAnEmptyComponentIsNotActive() {
        JPanel target = component(0, 0, 10, 10, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        target.setSize(0, 0);

        assertFalse(blocker.isActive());
    }

    @Test
    void blockerFollowingARoomObjectIsActive() {
        assertTrue(new LightBlocker(new BasicObj(rectCorners(), Color.RED), LightTag.BLOCK).isActive());
    }

    // ---- getAlphaImage ----

    @Test
    void alphaImageIsNullForNullOrEmptyArea() {
        JPanel target = component(0, 0, 10, 10, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        assertNull(blocker.getAlphaImage(null));
        assertNull(blocker.getAlphaImage(new Rectangle(0, 0, 0, 0)));
        assertNull(blocker.getAlphaImage(new Rectangle(0, 0, 10, 0)));
    }

    @Test
    void alphaImageIsNullWhenNothingIsFollowed() {
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.BLOCK);

        assertNull(blocker.getAlphaImage(new Rectangle(0, 0, 10, 10)));
    }

    @Test
    void alphaImageOfFollowedComponentMatchesTheAreaSize() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        BufferedImage image = blocker.getAlphaImage(new Rectangle(10, 10, 20, 20));

        assertNotNull(image);
        assertEquals(20, image.getWidth());
        assertEquals(20, image.getHeight());
    }

    @Test
    void alphaImageShowsTheComponentAsOpaque() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        BufferedImage image = blocker.getAlphaImage(new Rectangle(10, 10, 20, 20));

        assertEquals(Color.RED.getRGB(), image.getRGB(10, 10));
        assertEquals(255, alpha(image, 0, 0));
        assertEquals(255, alpha(image, 19, 19));
    }

    @Test
    void alphaImageIsTransparentOutsideTheComponent() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        // the area is bigger than the component, starting at the top left of the screen
        BufferedImage image = blocker.getAlphaImage(new Rectangle(0, 0, 50, 50));

        assertEquals(0, alpha(image, 5, 5));
        assertEquals(0, alpha(image, 45, 45));
        assertEquals(255, alpha(image, 20, 20));
    }

    @Test
    void alphaImageUsesTheAreaOffset() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        // only the bottom right part of the component is asked for
        BufferedImage image = blocker.getAlphaImage(new Rectangle(20, 20, 10, 10));

        assertEquals(10, image.getWidth());
        assertEquals(255, alpha(image, 0, 0));
        assertEquals(255, alpha(image, 9, 9));
    }

    @Test
    void alphaImageIsReusedUntilMarkedDirty() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);
        Rectangle area = new Rectangle(10, 10, 20, 20);
        BufferedImage first = blocker.getAlphaImage(area);
        assertEquals(Color.RED.getRGB(), first.getRGB(5, 5));

        // the look of the component changes, but the blocker hasn't been told
        target.setBackground(Color.BLUE);
        BufferedImage cached = blocker.getAlphaImage(area);
        assertEquals(Color.RED.getRGB(), cached.getRGB(5, 5));

        blocker.markAlphaDirty();
        BufferedImage redrawn = blocker.getAlphaImage(area);
        assertEquals(Color.BLUE.getRGB(), redrawn.getRGB(5, 5));
    }

    @Test
    void animatedBlockerRedrawsEveryTime() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);
        blocker.setAnimated(true);
        Rectangle area = new Rectangle(10, 10, 20, 20);
        assertEquals(Color.RED.getRGB(), blocker.getAlphaImage(area).getRGB(5, 5));

        target.setBackground(Color.BLUE);

        assertEquals(Color.BLUE.getRGB(), blocker.getAlphaImage(area).getRGB(5, 5));
    }

    @Test
    void alphaImageIsRedrawnWhenTheComponentMoves() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);
        Rectangle area = new Rectangle(0, 0, 60, 60);
        assertEquals(0, alpha(blocker.getAlphaImage(area), 40, 40));

        target.setBounds(35, 35, 20, 20);

        assertEquals(255, alpha(blocker.getAlphaImage(area), 40, 40));
        assertEquals(0, alpha(blocker.getAlphaImage(area), 15, 15));
    }

    @Test
    void alphaImageIsRedrawnWhenTheAreaChanges() {
        JPanel target = component(10, 10, 20, 20, Color.RED);
        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        BufferedImage small = blocker.getAlphaImage(new Rectangle(10, 10, 10, 10));
        assertEquals(10, small.getWidth());

        BufferedImage large = blocker.getAlphaImage(new Rectangle(10, 10, 20, 20));
        assertEquals(20, large.getWidth());
    }

    // ---- getAlphaImage for room objects ----

    @Test
    void alphaImageOfSolidRoomObjectIsItsCornerShape() {
        BasicObj obj = new BasicObj(trapezoidCorners(), Color.RED);
        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);

        BufferedImage image = blocker.getAlphaImage(new Rectangle(0, 0, 100, 100));

        assertNotNull(image);
        assertEquals(255, alpha(image, 20, 20));
        assertEquals(0, alpha(image, 95, 95));
    }

    @Test
    void alphaImageOfImageRoomObjectIsNotEmpty() {
        BasicObj obj = new BasicObj(rectCorners(), "/images/walls/BrickWall.png", false);
        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);

        BufferedImage image = blocker.getAlphaImage(new Rectangle(10, 20, 100, 50));

        assertNotNull(image);
        boolean anyVisible = false;
        for (int y = 0; y < image.getHeight() && !anyVisible; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) > 0) {
                    anyVisible = true;
                    break;
                }
            }
        }
        assertTrue(anyVisible);
    }

    @Test
    void alphaImageOfWarpedImageRoomObjectDoesNotThrow() {
        BasicObj obj = new BasicObj(trapezoidCorners(), "/images/walls/BrickWall.png", true);
        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);

        assertDoesNotThrow(() -> blocker.getAlphaImage(new Rectangle(0, 0, 100, 100)));
    }

    @Test
    void alphaImageOfRoomObjectFollowsCornerChanges() {
        BasicObj obj = new BasicObj(rectCorners(), Color.RED);
        LightBlocker blocker = new LightBlocker(obj, LightTag.BLOCK);
        Rectangle area = new Rectangle(0, 0, 200, 200);
        assertEquals(0, alpha(blocker.getAlphaImage(area), 150, 150));

        obj.setShapeCorners(new Point[]{new Point(140, 140), new Point(180, 140), new Point(180, 180), new Point(140, 180)});

        assertEquals(255, alpha(blocker.getAlphaImage(area), 150, 150));
        assertEquals(0, alpha(blocker.getAlphaImage(area), 50, 40));
    }

    @Test
    void followedComponentCanBeAnyComponent() {
        Component target = new javax.swing.JLabel("label");
        target.setBounds(0, 0, 10, 10);

        LightBlocker blocker = new LightBlocker(target, LightTag.BLOCK);

        assertSame(target, blocker.getFollow());
    }
}
