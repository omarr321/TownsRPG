package helpers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class GameSettingsTest {
    /** How long to wait for the click flash (120 ms in GameSettings) to revert before giving up. */
    private static final long REVERT_TIMEOUT_MS = 3000;

    private JFrame frame;

    @AfterEach
    void disposeFrame() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }

    /** A panel whose "mouse position" can be faked, since there is no real mouse in a test. */
    private static class TestPanel extends JPanel {
        Point mouse = null;

        @Override
        public Point getMousePosition() {
            return mouse;
        }
    }

    /** Custom cursors and frames need a display, so those tests are skipped on headless machines. */
    private static void requireDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
    }

    /** Calls the listeners the way Swing would, so the tests do not depend on a window being on screen. */
    private static void send(java.awt.Component target, int id, int x, int y) {
        MouseEvent e = new MouseEvent(target, id, System.currentTimeMillis(), 0, x, y, 1, false);
        switch (id) {
            case MouseEvent.MOUSE_PRESSED -> {
                for (MouseListener l : target.getMouseListeners()) l.mousePressed(e);
            }
            case MouseEvent.MOUSE_RELEASED -> {
                for (MouseListener l : target.getMouseListeners()) l.mouseReleased(e);
            }
            case MouseEvent.MOUSE_ENTERED -> {
                for (MouseListener l : target.getMouseListeners()) l.mouseEntered(e);
            }
            case MouseEvent.MOUSE_EXITED -> {
                for (MouseListener l : target.getMouseListeners()) l.mouseExited(e);
            }
            case MouseEvent.MOUSE_MOVED -> {
                for (MouseMotionListener l : target.getMouseMotionListeners()) l.mouseMoved(e);
            }
            case MouseEvent.MOUSE_DRAGGED -> {
                for (MouseMotionListener l : target.getMouseMotionListeners()) l.mouseDragged(e);
            }
            default -> throw new IllegalArgumentException("Unsupported mouse event id " + id);
        }
    }

    private static void waitForCursor(java.awt.Component target, Cursor expected) throws InterruptedException {
        long end = System.currentTimeMillis() + REVERT_TIMEOUT_MS;
        while (target.getCursor() != expected && System.currentTimeMillis() < end) {
            Thread.sleep(10);
        }
    }

    @Test
    void defaultValuesNotChanged() {
        assertFalse(GameSettings.fullScreen);

        assertEquals(1920, GameSettings.REFERENCE_WIDTH);
        assertEquals(0, GameSettings.screenWidth);
        assertEquals(0, GameSettings.screenHeight);
    }

    @Test
    void getScaleValid() {
        GameSettings.screenWidth = 1920;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = 960;
        assertEquals(0.50f, GameSettings.getScale());

        GameSettings.screenWidth = 3840;
        assertEquals(2f, GameSettings.getScale());
    }

    @Test
    void getScaleInvalid() {
        GameSettings.screenWidth = 0;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = -1920;
        assertEquals(1f, GameSettings.getScale());
    }

    @Test
    void scaleValid() {
        GameSettings.screenWidth = 0;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(1, GameSettings.scale(1));
        assertEquals(100, GameSettings.scale(100));
        assertEquals(200, GameSettings.scale(200));
        assertEquals(300, GameSettings.scale(300));
        assertEquals(400, GameSettings.scale(400));
        assertEquals(500, GameSettings.scale(500));

        GameSettings.screenWidth = 960;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(1, GameSettings.scale(1));
        assertEquals(50, GameSettings.scale(100));
        assertEquals(100, GameSettings.scale(200));
        assertEquals(150, GameSettings.scale(300));
        assertEquals(200, GameSettings.scale(400));
        assertEquals(250, GameSettings.scale(500));

        GameSettings.screenWidth = 3840;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(2, GameSettings.scale(1));
        assertEquals(200, GameSettings.scale(100));
        assertEquals(400, GameSettings.scale(200));
        assertEquals(600, GameSettings.scale(300));
        assertEquals(800, GameSettings.scale(400));
        assertEquals(1000, GameSettings.scale(500));
    }

    @Test
    void descaleValid() {
        GameSettings.screenWidth = 0;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(1, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(100));
        assertEquals(200, GameSettings.descale(200));
        assertEquals(300, GameSettings.descale(300));
        assertEquals(400, GameSettings.descale(400));
        assertEquals(500, GameSettings.descale(500));

        GameSettings.screenWidth = 960;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(2, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(50));
        assertEquals(200, GameSettings.descale(100));
        assertEquals(300, GameSettings.descale(150));
        assertEquals(400, GameSettings.descale(200));
        assertEquals(500, GameSettings.descale(250));

        GameSettings.screenWidth = 3840;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(1, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(200));
        assertEquals(150, GameSettings.descale(300));
        assertEquals(200, GameSettings.descale(400));
        assertEquals(250, GameSettings.descale(500));
        assertEquals(300, GameSettings.descale(600));
    }

    @Test
    void testPrivateConstructorIsHidden() throws Exception {
        Constructor<GameSettings> constructor = GameSettings.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // This asserts that calling the private constructor throws an UnsupportedOperationException
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void testCustomCursor() {
        GameSettings.getCursor(GameSettings.CursorType.CURSOR);
    }

    // ---- getCursor ----

    @Test
    void getCursorBuildsACustomCursorForEveryType() {
        requireDisplay();

        for (GameSettings.CursorType type : GameSettings.CursorType.values()) {
            Cursor cursor = GameSettings.getCursor(type);

            assertNotNull(cursor);
            assertEquals(Cursor.CUSTOM_CURSOR, cursor.getType());
            assertEquals("Custom Cursor", cursor.getName());
        }
    }

    @Test
    void getCursorGivesADifferentCursorForEachType() {
        requireDisplay();

        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);
        Cursor clicked = GameSettings.getCursor(GameSettings.CursorType.CURSOR_CLICKED);
        Cursor highlighted = GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

        assertNotSame(normal, clicked);
        assertNotSame(normal, highlighted);
        assertNotSame(clicked, highlighted);
    }

    @Test
    void getCursorReusesTheCachedCursor() {
        requireDisplay();

        for (GameSettings.CursorType type : GameSettings.CursorType.values()) {
            assertSame(GameSettings.getCursor(type), GameSettings.getCursor(type));
        }
    }

    // ---- setCustomMouse ----

    @Test
    void setCustomMouseStartsWithTheNormalCursor() {
        requireDisplay();
        frame = new JFrame();

        GameSettings.setCustomMouse(frame);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR), frame.getCursor());
    }

    @Test
    void setCustomMousePressShowsTheClickedCursor() {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);

        send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_CLICKED), frame.getCursor());
    }

    @Test
    void setCustomMouseRevertsToTheNormalCursorAfterTheClick() throws InterruptedException {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);

        send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);
        waitForCursor(frame, normal);

        assertSame(normal, frame.getCursor());
    }

    @Test
    void setCustomMouseIgnoresEventsOtherThanPress() {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);

        send(frame, MouseEvent.MOUSE_RELEASED, 5, 5);
        send(frame, MouseEvent.MOUSE_ENTERED, 5, 5);
        send(frame, MouseEvent.MOUSE_EXITED, 5, 5);

        assertSame(normal, frame.getCursor());
    }

    @Test
    void setCustomMouseCanBePressedAgainAfterItReverts() throws InterruptedException {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);
        Cursor clicked = GameSettings.getCursor(GameSettings.CursorType.CURSOR_CLICKED);

        send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);
        waitForCursor(frame, normal);
        send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);

        assertSame(clicked, frame.getCursor());
    }

    // ---- setInteractableMouse ----

    @Test
    void setInteractableMouseStartsWithTheNormalCursor() {
        requireDisplay();
        TestPanel panel = new TestPanel();

        GameSettings.setInteractableMouse(panel, p -> true);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR), panel.getCursor());
    }

    @Test
    void setInteractableMouseHighlightsWhenMovedOverAnInteractable() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());
    }

    @Test
    void setInteractableMouseGoesBackToNormalWhenMovedOffTheInteractable() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        send(panel, MouseEvent.MOUSE_MOVED, 10, 10);
        send(panel, MouseEvent.MOUSE_MOVED, 80, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR), panel.getCursor());
    }

    @Test
    void setInteractableMouseHighlightsWhileDragging() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        send(panel, MouseEvent.MOUSE_DRAGGED, 10, 10);
        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());

        send(panel, MouseEvent.MOUSE_DRAGGED, 80, 10);
        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR), panel.getCursor());
    }

    @Test
    void setInteractableMouseHighlightsWhenTheMouseEntersOverAnInteractable() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        send(panel, MouseEvent.MOUSE_ENTERED, 10, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());
    }

    @Test
    void setInteractableMouseStaysNormalWhenTheMouseEntersOffAnInteractable() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        send(panel, MouseEvent.MOUSE_ENTERED, 80, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR), panel.getCursor());
    }

    @Test
    void setInteractableMouseGivesThePredicateThePointOfTheEvent() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        List<Point> seen = new ArrayList<>();
        Predicate<Point> record = p -> {
            seen.add(p);
            return false;
        };
        GameSettings.setInteractableMouse(panel, record);

        send(panel, MouseEvent.MOUSE_MOVED, 12, 34);

        assertEquals(1, seen.size());
        assertEquals(new Point(12, 34), seen.get(0));
    }

    @Test
    void setInteractableMousePressShowsTheClickedCursor() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> true);

        send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_CLICKED), panel.getCursor());
    }

    @Test
    void setInteractableMouseIgnoresMovesWhileTheClickFlashes() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> true);

        send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
        send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

        assertSame(GameSettings.getCursor(GameSettings.CursorType.CURSOR_CLICKED), panel.getCursor());
    }

    @Test
    void setInteractableMouseRevertsToNormalWhenTheMouseIsNotOverThePanel() throws InterruptedException {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = null;
        GameSettings.setInteractableMouse(panel, p -> true);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);

        send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
        waitForCursor(panel, normal);

        assertSame(normal, panel.getCursor());
    }

    @Test
    void setInteractableMouseRevertsToHighlightedWhenTheMouseIsStillOverAnInteractable() throws InterruptedException {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = new Point(10, 10);
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor highlighted = GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

        send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
        waitForCursor(panel, highlighted);

        assertSame(highlighted, panel.getCursor());
    }

    @Test
    void setInteractableMouseRevertsToNormalWhenTheMouseIsOverThePanelButNotAnInteractable() throws InterruptedException {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = new Point(80, 10);
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);

        send(panel, MouseEvent.MOUSE_PRESSED, 80, 10);
        waitForCursor(panel, normal);

        assertSame(normal, panel.getCursor());
    }

    @Test
    void setInteractableMouseHighlightsAgainOnceTheClickFlashIsOver() throws InterruptedException {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor normal = GameSettings.getCursor(GameSettings.CursorType.CURSOR);
        Cursor highlighted = GameSettings.getCursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

        send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
        waitForCursor(panel, normal);
        send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

        assertSame(highlighted, panel.getCursor());
    }
}
