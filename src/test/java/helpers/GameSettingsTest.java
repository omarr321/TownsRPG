package helpers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Tests for {@link GameSettings}.
 * <p>
 * {@code GameSettings} is all static state, so every test starts from a clean slate (screen size 0x0, not
 * full screen) and the values found before the test are put back afterwards. That keeps these tests
 * independent of each other and of every other test class.
 * <p>
 * Cursor tests need a display and are skipped on headless machines. The "clicked" cursor flash reverts on a
 * Swing timer 120 ms after a press, so tests that check the flash press and assert inside one Swing-thread
 * task. The timer cannot fire in the middle of a task, which makes those checks deterministic instead of a
 * race against the clock.
 */
public class GameSettingsTest {
    /** How long to wait for the click flash (120 ms in GameSettings) to revert before giving up. */
    private static final long REVERT_TIMEOUT_MS = 3000;

    private static int initialScreenWidth;
    private static int initialScreenHeight;
    private static boolean initialFullScreen;

    private int savedScreenWidth;
    private int savedScreenHeight;
    private boolean savedFullScreen;

    private JFrame frame;

    /** What the static fields held when this class started, before any test of ours touched them. */
    @BeforeAll
    static void captureInitialState() {
        initialScreenWidth = GameSettings.screenWidth;
        initialScreenHeight = GameSettings.screenHeight;
        initialFullScreen = GameSettings.fullScreen;
    }

    @BeforeEach
    void cleanSlate() {
        savedScreenWidth = GameSettings.screenWidth;
        savedScreenHeight = GameSettings.screenHeight;
        savedFullScreen = GameSettings.fullScreen;

        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
        GameSettings.fullScreen = false;
    }

    @AfterEach
    void restoreAndDispose() {
        GameSettings.screenWidth = savedScreenWidth;
        GameSettings.screenHeight = savedScreenHeight;
        GameSettings.fullScreen = savedFullScreen;

        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    /** A panel whose "mouse position" can be faked, since there is no real mouse in a test. */
    private static class TestPanel extends JPanel {
        Point mouse = null;

        @Override
        public Point getMousePosition() {
            return mouse;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Custom cursors and frames need a display, so those tests are skipped on headless machines. */
    private static void requireDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
    }

    private static <T> T onEdtGet(Callable<T> task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return task.call();
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(task.call());
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        Throwable t = failure.get();
        // Rethrown as-is so a failed assertion shows up as an assertion failure
        if (t instanceof Exception ex) {
            throw ex;
        }
        if (t instanceof Error err) {
            throw err;
        }
        return result.get();
    }

    private static void onEdt(ThrowingRunnable task) throws Exception {
        onEdtGet(() -> {
            task.run();
            return null;
        });
    }

    private static Cursor cursor(GameSettings.CursorType type) {
        return GameSettings.getCursor(type);
    }

    /** Reads the cursor on the Swing thread so the test thread always sees the latest value. */
    private static Cursor cursorOf(Component target) throws Exception {
        return onEdtGet(target::getCursor);
    }

    /** Calls the listeners the way Swing would, so the tests do not depend on a window being on screen. */
    private static void send(Component target, int id, int x, int y) {
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

    private static void waitForCursor(Component target, Cursor expected) throws Exception {
        long end = System.currentTimeMillis() + REVERT_TIMEOUT_MS;
        while (cursorOf(target) != expected && System.currentTimeMillis() < end) {
            Thread.sleep(10);
        }
    }

    // ==========================================================================================
    // Fields and defaults
    // ==========================================================================================

    @Test
    void defaultValuesNotChanged() {
        // Checked against what the fields held when this class started, because other tests change them
        assertFalse(initialFullScreen);
        assertEquals(0, initialScreenWidth);
        assertEquals(0, initialScreenHeight);

        assertEquals(1920, GameSettings.REFERENCE_WIDTH);
    }

    @Test
    void screenFieldsCanBeChanged() {
        GameSettings.screenWidth = 1280;
        GameSettings.screenHeight = 720;
        GameSettings.fullScreen = true;

        assertEquals(1280, GameSettings.screenWidth);
        assertEquals(720, GameSettings.screenHeight);
        assertTrue(GameSettings.fullScreen);
    }

    @Test
    void testPrivateConstructorIsHidden() throws Exception {
        Constructor<GameSettings> constructor = GameSettings.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(UnsupportedOperationException.class, thrown.getCause());
        assertNotNull(thrown.getCause().getMessage());
    }

    @Test
    void cursorTypesAreTheThreeExpectedStates() {
        assertEquals(3, GameSettings.CursorType.values().length);
        assertSame(GameSettings.CursorType.CURSOR, GameSettings.CursorType.valueOf("CURSOR"));
        assertSame(GameSettings.CursorType.CURSOR_CLICKED, GameSettings.CursorType.valueOf("CURSOR_CLICKED"));
        assertSame(GameSettings.CursorType.CURSOR_HIGHLIGHTED, GameSettings.CursorType.valueOf("CURSOR_HIGHLIGHTED"));
    }

    // ==========================================================================================
    // getScale
    // ==========================================================================================

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
    void getScaleNonReferenceWidth() {
        GameSettings.screenWidth = 1280;
        assertEquals(1280 / 1920f, GameSettings.getScale(), 0.0001f);

        GameSettings.screenWidth = 1;
        assertEquals(1 / 1920f, GameSettings.getScale(), 0.000001f);
    }

    @Test
    void getScaleInvalid() {
        GameSettings.screenWidth = 0;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = -1920;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = -1;
        assertEquals(1f, GameSettings.getScale());
    }

    @Test
    void getScaleOnlyDependsOnWidth() {
        GameSettings.screenWidth = 960;

        GameSettings.screenHeight = 0;
        assertEquals(0.5f, GameSettings.getScale());
        GameSettings.screenHeight = 5000;
        assertEquals(0.5f, GameSettings.getScale());
        GameSettings.screenHeight = -5;
        assertEquals(0.5f, GameSettings.getScale());
    }

    // ==========================================================================================
    // scale
    // ==========================================================================================

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
    void scaleIsTheIdentityAtTheReferenceWidth() {
        GameSettings.screenWidth = GameSettings.REFERENCE_WIDTH;

        for (int v : new int[]{1, 2, 7, 99, 1234, 100_000}) {
            assertEquals(v, GameSettings.scale(v));
        }
    }

    @Test
    void scaleRoundsToTheNearestPixel() {
        GameSettings.screenWidth = 1280; // scale of 2/3

        assertEquals(7, GameSettings.scale(10));   // 6.67 rounds up
        assertEquals(67, GameSettings.scale(100)); // 66.67 rounds up
        assertEquals(2, GameSettings.scale(3));    // 2.0
    }

    @Test
    void scaleNeverShrinksAPositiveValueBelowOne() {
        GameSettings.screenWidth = 100; // scale of about 0.052

        assertEquals(1, GameSettings.scale(1));  // 0.05 would round to 0
        assertEquals(1, GameSettings.scale(5));  // 0.26 would round to 0
        assertEquals(5, GameSettings.scale(100)); // 5.2 is fine
    }

    @Test
    void scaleLeavesZeroAndNegativesAloneAtAnyWidth() {
        for (int width : new int[]{-500, 0, 100, 1920, 7680}) {
            GameSettings.screenWidth = width;

            assertEquals(0, GameSettings.scale(0), "width " + width);
            assertEquals(-1, GameSettings.scale(-1), "width " + width);
            assertEquals(-250, GameSettings.scale(-250), "width " + width);
            assertEquals(Integer.MIN_VALUE, GameSettings.scale(Integer.MIN_VALUE), "width " + width);
        }
    }

    @Test
    void scaleWithAnInvalidWidthActsLikeScaleOne() {
        GameSettings.screenWidth = -3840;

        assertEquals(1, GameSettings.scale(1));
        assertEquals(100, GameSettings.scale(100));
    }

    // ==========================================================================================
    // descale
    // ==========================================================================================

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
    void descaleIsTheIdentityAtTheReferenceWidth() {
        GameSettings.screenWidth = GameSettings.REFERENCE_WIDTH;

        for (int v : new int[]{1, 2, 7, 99, 1234, 100_000}) {
            assertEquals(v, GameSettings.descale(v));
        }
    }

    @Test
    void descaleRoundsToTheNearestPixel() {
        GameSettings.screenWidth = 1280; // scale of 2/3

        assertEquals(15, GameSettings.descale(10));   // 15.0
        assertEquals(150, GameSettings.descale(100)); // 150.0
        assertEquals(300, GameSettings.descale(200)); // 300.0
    }

    @Test
    void descaleNeverShrinksAPositiveValueBelowOne() {
        GameSettings.screenWidth = 19200; // scale of 10

        assertEquals(1, GameSettings.descale(1));   // 0.1 would round to 0
        assertEquals(1, GameSettings.descale(4));   // 0.4 would round to 0
        assertEquals(10, GameSettings.descale(100)); // 10.0 is fine
    }

    @Test
    void descaleLeavesZeroAndNegativesAloneAtAnyWidth() {
        for (int width : new int[]{-500, 0, 100, 1920, 7680}) {
            GameSettings.screenWidth = width;

            assertEquals(0, GameSettings.descale(0), "width " + width);
            assertEquals(-1, GameSettings.descale(-1), "width " + width);
            assertEquals(-250, GameSettings.descale(-250), "width " + width);
            assertEquals(Integer.MIN_VALUE, GameSettings.descale(Integer.MIN_VALUE), "width " + width);
        }
    }

    @Test
    void descaleWithAnInvalidWidthActsLikeScaleOne() {
        GameSettings.screenWidth = -3840;

        assertEquals(1, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(100));
    }

    @Test
    void scaleThenDescaleGetsBackTheOriginal() {
        for (int width : new int[]{960, 1920, 3840, 7680}) {
            GameSettings.screenWidth = width;

            for (int v : new int[]{10, 100, 640, 1000}) {
                assertEquals(v, GameSettings.descale(GameSettings.scale(v)), "width " + width + ", value " + v);
            }
        }
    }

    // ==========================================================================================
    // getCursor
    // ==========================================================================================

    @Test
    void testCustomCursor() {
        requireDisplay();

        assertNotNull(GameSettings.getCursor(GameSettings.CursorType.CURSOR));
    }

    @Test
    void getCursorWithNullTypeThrows() {
        // The type is switched on before anything touches the display, so this does not need one
        assertThrows(NullPointerException.class, () -> GameSettings.getCursor(null));
    }

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

        Cursor normal = cursor(GameSettings.CursorType.CURSOR);
        Cursor clicked = cursor(GameSettings.CursorType.CURSOR_CLICKED);
        Cursor highlighted = cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

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

    @Test
    void getCursorCacheIsNotAffectedByTheScreenSize() {
        requireDisplay();
        Cursor before = cursor(GameSettings.CursorType.CURSOR);

        GameSettings.screenWidth = 3840;
        GameSettings.screenHeight = 2160;

        assertSame(before, cursor(GameSettings.CursorType.CURSOR));
    }

    // ==========================================================================================
    // setCustomMouse
    // ==========================================================================================

    @Test
    void setCustomMouseStartsWithTheNormalCursor() {
        requireDisplay();
        frame = new JFrame();

        GameSettings.setCustomMouse(frame);

        assertSame(cursor(GameSettings.CursorType.CURSOR), frame.getCursor());
    }

    @Test
    void setCustomMouseAddsOneMouseListenerAndNoMotionListener() {
        requireDisplay();
        frame = new JFrame();
        int mouseBefore = frame.getMouseListeners().length;
        int motionBefore = frame.getMouseMotionListeners().length;

        GameSettings.setCustomMouse(frame);

        assertEquals(mouseBefore + 1, frame.getMouseListeners().length);
        assertEquals(motionBefore, frame.getMouseMotionListeners().length,
                "setCustomMouse never shows the highlighted cursor, so it does not follow the mouse");
    }

    @Test
    void setCustomMousePressShowsTheClickedCursor() throws Exception {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);

        onEdt(() -> {
            send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);

            assertSame(cursor(GameSettings.CursorType.CURSOR_CLICKED), frame.getCursor());
        });
    }

    @Test
    void setCustomMouseRevertsToTheNormalCursorAfterTheClick() throws Exception {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);

        onEdt(() -> send(frame, MouseEvent.MOUSE_PRESSED, 5, 5));
        waitForCursor(frame, normal);

        assertSame(normal, cursorOf(frame));
    }

    @Test
    void setCustomMouseIgnoresEventsOtherThanPress() throws Exception {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);

        onEdt(() -> {
            send(frame, MouseEvent.MOUSE_RELEASED, 5, 5);
            send(frame, MouseEvent.MOUSE_ENTERED, 5, 5);
            send(frame, MouseEvent.MOUSE_EXITED, 5, 5);

            assertSame(normal, frame.getCursor());
        });
    }

    @Test
    void setCustomMouseCanBePressedAgainAfterItReverts() throws Exception {
        requireDisplay();
        frame = new JFrame();
        GameSettings.setCustomMouse(frame);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);
        Cursor clicked = cursor(GameSettings.CursorType.CURSOR_CLICKED);

        onEdt(() -> send(frame, MouseEvent.MOUSE_PRESSED, 5, 5));
        waitForCursor(frame, normal);
        assertSame(normal, cursorOf(frame), "First flash should be over");

        onEdt(() -> {
            send(frame, MouseEvent.MOUSE_PRESSED, 5, 5);

            assertSame(clicked, frame.getCursor());
        });
    }

    @Test
    void setCustomMouseWithANullFrameThrows() {
        requireDisplay();

        assertThrows(NullPointerException.class, () -> GameSettings.setCustomMouse(null));
    }

    // ==========================================================================================
    // setInteractableMouse
    // ==========================================================================================

    @Test
    void setInteractableMouseStartsWithTheNormalCursor() {
        requireDisplay();
        TestPanel panel = new TestPanel();

        GameSettings.setInteractableMouse(panel, p -> true);

        assertSame(cursor(GameSettings.CursorType.CURSOR), panel.getCursor());
    }

    @Test
    void setInteractableMouseAddsOneMouseListenerAndOneMotionListener() {
        requireDisplay();
        TestPanel panel = new TestPanel();
        int mouseBefore = panel.getMouseListeners().length;
        int motionBefore = panel.getMouseMotionListeners().length;

        GameSettings.setInteractableMouse(panel, p -> true);

        assertEquals(mouseBefore + 1, panel.getMouseListeners().length);
        assertEquals(motionBefore + 1, panel.getMouseMotionListeners().length);
    }

    @Test
    void setInteractableMouseHighlightsWhenMovedOverAnInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseGoesBackToNormalWhenMovedOffTheInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_MOVED, 10, 10);
            send(panel, MouseEvent.MOUSE_MOVED, 80, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseHighlightsWhileDragging() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_DRAGGED, 10, 10);
            assertSame(cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());

            send(panel, MouseEvent.MOUSE_DRAGGED, 80, 10);
            assertSame(cursor(GameSettings.CursorType.CURSOR), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseHighlightsWhenTheMouseEntersOverAnInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_ENTERED, 10, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseStaysNormalWhenTheMouseEntersOffAnInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_ENTERED, 80, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseIgnoresExitAndRelease() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_MOVED, 10, 10);
            Cursor highlighted = cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);
            assertSame(highlighted, panel.getCursor());

            send(panel, MouseEvent.MOUSE_EXITED, 80, 10);
            send(panel, MouseEvent.MOUSE_RELEASED, 80, 10);

            assertSame(highlighted, panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseGivesThePredicateThePointOfTheEvent() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        List<Point> seen = new ArrayList<>();
        Predicate<Point> record = p -> {
            seen.add(p);
            return false;
        };
        GameSettings.setInteractableMouse(panel, record);

        onEdt(() -> send(panel, MouseEvent.MOUSE_MOVED, 12, 34));

        assertEquals(1, seen.size());
        assertEquals(new Point(12, 34), seen.get(0));
    }

    @Test
    void setInteractableMousePressShowsTheClickedCursor() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> true);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR_CLICKED), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseDoesNotAskThePredicateOnPress() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        List<Point> seen = new ArrayList<>();
        GameSettings.setInteractableMouse(panel, p -> {
            seen.add(p);
            return true;
        });

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);

            // Checked inside the same Swing task so the revert timer (which does ask) cannot have run yet
            assertTrue(seen.isEmpty());
        });
    }

    @Test
    void setInteractableMouseIgnoresMovesWhileTheClickFlashes() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> true);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
            send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR_CLICKED), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseIgnoresDragsAndEntersWhileTheClickFlashes() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> true);

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_PRESSED, 10, 10);
            send(panel, MouseEvent.MOUSE_DRAGGED, 10, 10);
            send(panel, MouseEvent.MOUSE_ENTERED, 10, 10);

            assertSame(cursor(GameSettings.CursorType.CURSOR_CLICKED), panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseRevertsToNormalWhenTheMouseIsNotOverThePanel() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = null;
        GameSettings.setInteractableMouse(panel, p -> true);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);

        onEdt(() -> send(panel, MouseEvent.MOUSE_PRESSED, 10, 10));
        waitForCursor(panel, normal);

        assertSame(normal, cursorOf(panel));
    }

    @Test
    void setInteractableMouseRevertsToHighlightedWhenTheMouseIsStillOverAnInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = new Point(10, 10);
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor highlighted = cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

        onEdt(() -> send(panel, MouseEvent.MOUSE_PRESSED, 10, 10));
        waitForCursor(panel, highlighted);

        assertSame(highlighted, cursorOf(panel));
    }

    @Test
    void setInteractableMouseRevertsToNormalWhenTheMouseIsOverThePanelButNotAnInteractable() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        panel.mouse = new Point(80, 10);
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);

        onEdt(() -> send(panel, MouseEvent.MOUSE_PRESSED, 80, 10));
        waitForCursor(panel, normal);

        assertSame(normal, cursorOf(panel));
    }

    @Test
    void setInteractableMouseHighlightsAgainOnceTheClickFlashIsOver() throws Exception {
        requireDisplay();
        TestPanel panel = new TestPanel();
        GameSettings.setInteractableMouse(panel, p -> p.x < 50);
        Cursor normal = cursor(GameSettings.CursorType.CURSOR);
        Cursor highlighted = cursor(GameSettings.CursorType.CURSOR_HIGHLIGHTED);

        onEdt(() -> send(panel, MouseEvent.MOUSE_PRESSED, 10, 10));
        waitForCursor(panel, normal);
        assertSame(normal, cursorOf(panel), "Flash should be over");

        onEdt(() -> {
            send(panel, MouseEvent.MOUSE_MOVED, 10, 10);

            assertSame(highlighted, panel.getCursor());
        });
    }

    @Test
    void setInteractableMouseWithANullSurfaceThrows() {
        requireDisplay();

        assertThrows(NullPointerException.class, () -> GameSettings.setInteractableMouse(null, p -> true));
    }
}