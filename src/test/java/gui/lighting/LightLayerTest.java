package gui.lighting;

import gui.lighting.LightBlocker.LightTag;
import helpers.GameSettings;
import helpers.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LightLayerTest {

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

    // ---- constructors ----

    @Test
    void defaultConstructorMakesAnEmptyTransparentLayer() {
        LightLayer layer = new LightLayer();

        assertFalse(layer.isOpaque());
        assertNull(layer.getLayout());
        assertTrue(layer.getBlockers().isEmpty());
    }

    @Test
    void sizedConstructorSetsSizeAndPreferredSize() {
        LightLayer layer = new LightLayer(300, 200);

        assertEquals(new Dimension(300, 200), layer.getSize());
        assertEquals(new Dimension(300, 200), layer.getPreferredSize());
        assertFalse(layer.isOpaque());
    }

    // ---- adding ----

    @Test
    void addBlockerWithBoundsCreatesAndReturnsABlocker() {
        LightLayer layer = new LightLayer();

        LightBlocker blocker = layer.addBlocker(10, 20, 30, 40, LightTag.BLOCK);

        assertNotNull(blocker);
        assertEquals(LightTag.BLOCK, blocker.getTag());
        assertEquals(new java.awt.Rectangle(10, 20, 30, 40), blocker.getBounds());
        assertTrue(layer.getBlockers().contains(blocker));
    }

    @Test
    void addBlockerThatFollowsAComponent() {
        LightLayer layer = new LightLayer();
        JPanel target = new JPanel();
        target.setBounds(5, 6, 7, 8);

        LightBlocker blocker = layer.addBlocker(target, LightTag.REFLECT);

        assertSame(target, blocker.getFollow());
        assertEquals(LightTag.REFLECT, blocker.getTag());
        assertTrue(layer.getBlockers().contains(blocker));
    }

    @Test
    void addBlockerAddsAnExistingBlocker() {
        LightLayer layer = new LightLayer();
        LightBlocker blocker = new LightBlocker(0, 0, 10, 10, LightTag.LIT);

        LightBlocker returned = layer.addBlocker(blocker);

        assertSame(blocker, returned);
        assertEquals(List.of(blocker), layer.getBlockers());
    }

    @Test
    void blockersAreReturnedInTheOrderTheyWereAdded() {
        LightLayer layer = new LightLayer();
        LightBlocker a = layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);
        LightBlocker b = layer.addBlocker(20, 0, 10, 10, LightTag.BLOCK);
        LightBlocker c = layer.addBlocker(40, 0, 10, 10, LightTag.BLOCK);

        assertEquals(List.of(a, b, c), layer.getBlockers());
    }

    @Test
    void getBlockersIncludesInactiveBlockers() {
        LightLayer layer = new LightLayer();
        LightBlocker hidden = layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);
        hidden.setVisible(false);

        assertEquals(1, layer.getBlockers().size());
    }

    @Test
    void getBlockersIgnoresOtherComponents() {
        LightLayer layer = new LightLayer();
        LightBlocker blocker = layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);
        layer.add(new JPanel());

        assertEquals(List.of(blocker), layer.getBlockers());
    }

    @Test
    void getBlockersReturnsACopy() {
        LightLayer layer = new LightLayer();
        layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);

        layer.getBlockers().clear();

        assertEquals(1, layer.getBlockers().size());
    }

    // ---- removing ----

    @Test
    void removeBlockerTakesItOutOfTheLayer() {
        LightLayer layer = new LightLayer();
        LightBlocker a = layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);
        LightBlocker b = layer.addBlocker(20, 0, 10, 10, LightTag.BLOCK);

        layer.removeBlocker(a);

        assertEquals(List.of(b), layer.getBlockers());
    }

    @Test
    void removeBlockerThatIsNotInTheLayerDoesNothing() {
        LightLayer layer = new LightLayer();
        LightBlocker inLayer = layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);

        assertDoesNotThrow(() -> layer.removeBlocker(new LightBlocker(0, 0, 5, 5, LightTag.BLOCK)));

        assertEquals(List.of(inLayer), layer.getBlockers());
    }

    @Test
    void clearBlockersRemovesEverything() {
        LightLayer layer = new LightLayer();
        layer.addBlocker(0, 0, 10, 10, LightTag.BLOCK);
        layer.addBlocker(20, 0, 10, 10, LightTag.REFLECT);

        layer.clearBlockers();

        assertTrue(layer.getBlockers().isEmpty());
    }

    @Test
    void clearBlockersOnEmptyLayerIsFine() {
        LightLayer layer = new LightLayer();

        assertDoesNotThrow(layer::clearBlockers);
    }

    // ---- nearestFreePoint ----

    @Test
    void nearestFreePointIsTheSamePointWhenNothingBlocks() {
        LightLayer layer = new LightLayer(100, 100);

        Point result = layer.nearestFreePoint(40, 50, 100, 100);

        assertEquals(40, result.getX());
        assertEquals(50, result.getY());
    }

    @Test
    void nearestFreePointIsTheSamePointWhenItIsOutsideEveryBlocker() {
        LightLayer layer = new LightLayer(100, 100);
        layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK);

        Point result = layer.nearestFreePoint(80, 80, 100, 100);

        assertEquals(80, result.getX());
        assertEquals(80, result.getY());
    }

    @Test
    void nearestFreePointMovesOutOfABlockerToTheClosestEdge() {
        LightLayer layer = new LightLayer(100, 100);
        layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK); // covers x 10..30, y 10..30 (edges count)

        // 2 pixels in from the left edge, so the left side is the closest way out
        Point result = layer.nearestFreePoint(12, 20, 100, 100);

        assertEquals(9, result.getX());
        assertEquals(20, result.getY());
    }

    @Test
    void nearestFreePointCanEscapeToTheTop() {
        LightLayer layer = new LightLayer(100, 100);
        layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK);

        Point result = layer.nearestFreePoint(20, 12, 100, 100);

        assertEquals(20, result.getX());
        assertEquals(9, result.getY());
    }

    @Test
    void nearestFreePointTreatsBlockerEdgesAsInside() {
        LightLayer layer = new LightLayer(100, 100);
        layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK);

        Point onLeftEdge = layer.nearestFreePoint(10, 20, 100, 100);
        Point onRightEdge = layer.nearestFreePoint(30, 20, 100, 100);

        assertNotEquals(10, onLeftEdge.getX());
        assertNotEquals(30, onRightEdge.getX());
    }

    @Test
    void nearestFreePointResultIsNeverInsideABlocker() {
        LightLayer layer = new LightLayer(200, 200);
        layer.addBlocker(20, 20, 60, 60, LightTag.BLOCK);
        layer.addBlocker(70, 30, 60, 60, LightTag.REFLECT);

        Point result = layer.nearestFreePoint(75, 50, 200, 200);

        assertNotNull(result);
        for (LightBlocker blocker : layer.getBlockers()) {
            java.awt.Rectangle r = blocker.getLightBounds();
            boolean inside = result.getX() >= r.x && result.getX() <= r.x + r.width
                    && result.getY() >= r.y && result.getY() <= r.y + r.height;
            assertFalse(inside, "Result " + result.getX() + "," + result.getY() + " is inside " + r);
        }
    }

    @Test
    void nearestFreePointIgnoresInactiveBlockers() {
        LightLayer layer = new LightLayer(100, 100);
        LightBlocker hidden = layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK);
        hidden.setVisible(false);

        Point result = layer.nearestFreePoint(20, 20, 100, 100);

        assertEquals(20, result.getX());
        assertEquals(20, result.getY());
    }

    @Test
    void nearestFreePointIsNullWhenTheWholeAreaIsBlocked() {
        LightLayer layer = new LightLayer(100, 100);
        layer.addBlocker(0, 0, 100, 100, LightTag.BLOCK);

        assertNull(layer.nearestFreePoint(50, 50, 100, 100));
    }

    @Test
    void nearestFreePointStaysInsideTheGivenArea() {
        LightLayer layer = new LightLayer(100, 100);
        // blocker covers the point and everything to its left, so the only way out is to the right
        layer.addBlocker(0, 0, 50, 100, LightTag.BLOCK);

        Point result = layer.nearestFreePoint(10, 50, 100, 100);

        assertNotNull(result);
        assertTrue(result.getX() >= 0 && result.getX() < 100);
        assertTrue(result.getY() >= 0 && result.getY() < 100);
        assertEquals(51, result.getX());
    }

    @Test
    void nearestFreePointWorksWithFollowedComponents() {
        LightLayer layer = new LightLayer(100, 100);
        JPanel target = new JPanel();
        target.setBounds(10, 10, 20, 20);
        layer.addBlocker(target, LightTag.BLOCK);

        Point result = layer.nearestFreePoint(12, 20, 100, 100);

        assertEquals(9, result.getX());
    }
}
