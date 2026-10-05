package gui.hud;

import engine.Player;
import engine.interactions.BasicInteraction;
import engine.interactions.FlagInteraction;
import engine.interactions.Interactable;
import engine.messages.MessageData;
import engine.messages.MessageData.NamePosition;
import engine.room.Room;
import engine.room.parts.Ceiling;
import engine.room.parts.Floor;
import engine.room.parts.RoomPoints;
import engine.room.parts.Wall;
import helpers.GameSettings;
import helpers.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HudUI}.
 * <p>
 * Notes on how this test is put together (these are the things that used to make it fail):
 * <ul>
 *   <li>Everything that touches the HUD runs on the Swing thread. {@code displayMessage} hops onto the
 *       Swing thread when called from anywhere else, so asserting on the test thread would race it.</li>
 *   <li>The click that closes a message is deliberately swallowed until the mouse moves or presses again
 *       (see {@link HudUI#isBlockingInput()}), so "the message is gone" is only visible after a mouse move.</li>
 *   <li>The arrow click areas are only worked out while painting, so arrow tests paint first.</li>
 *   <li>{@link GameSettings} is static, so the screen size is set up and restored around every test.</li>
 * </ul>
 * Mouse events are sent straight to {@code processMouseEvent}/{@code processMouseMotionEvent} (this test lives
 * in the same package), and painting is done onto a {@link BufferedImage}, so no window is needed.
 */
class HudUITest {

    // ------------------------------------------------------------------------------------------
    // Layout of DummyRoom's message box at 1920x1080 (scale 1). HudUI insets the box by 10px:
    //   raw box (300,500)-(1500,800)  ->  drawn box (310,510)-(1490,790)
    // Arrows are 120px squares, 20px away from the box, top-aligned with it.
    // Name plates are 45x15: left one at x 295..340, y 510..525; right one at x 1475..1520, y 465..480.
    // ------------------------------------------------------------------------------------------
    private static final int SCREEN_W = 1920;
    private static final int SCREEN_H = 1080;

    private static final Rectangle LEFT_ARROW = new Rectangle(170, 510, 120, 120);
    private static final Rectangle RIGHT_ARROW = new Rectangle(1510, 510, 120, 120);
    /** A spot inside the box that short text never reaches. */
    private static final Rectangle BOX_EMPTY_SPOT = new Rectangle(1380, 760, 20, 20);
    /** Where the first line of text lands. */
    private static final Rectangle TEXT_AREA = new Rectangle(326, 526, 300, 40);
    /** A sliver of each name plate that is outside the message box and away from the name text. */
    private static final Rectangle LEFT_PLATE_SPOT = new Rectangle(299, 514, 4, 6);
    private static final Rectangle RIGHT_PLATE_SPOT = new Rectangle(1478, 469, 4, 6);
    /** Clicks here hit nothing: not an arrow, not the box, not a plate. */
    private static final int NEUTRAL_X = 900;
    private static final int NEUTRAL_Y = 300;

    private static final int BACKGROUND = 0x0000FF;
    /** The typing timer fires every 33ms, so this is plenty for a couple of characters. */
    private static final long TYPING_SETTLE_MS = 600;

    private int savedScreenWidth;
    private int savedScreenHeight;

    private DummyRoom room;
    private HudUI hud;
    private JPanel content;
    private JLayer<JComponent> layer;

    // ------------------------------------------------------------------------------------------
    // Test doubles
    // ------------------------------------------------------------------------------------------

    /** A room with a fixed message box that records what the HUD asks it to do. */
    static class DummyRoom extends Room {
        Point[] box = {new Point(300, 500), new Point(1500, 500), new Point(1500, 800), new Point(300, 800)};
        int lookLeftCalls = 0;
        int lookRightCalls = 0;
        int rebuildCalls = 0;
        int geometryCalls = 0;
        float lastSizeRatio = -1f;

        DummyRoom() {
            super(null, null, new RoomPoints(0.75, 0.5, 45, 1));
        }

        @Override
        public Point[] getMessageBoxPoints(float sizeRatio) {
            geometryCalls++;
            lastSizeRatio = sizeRatio;
            Point[] copy = new Point[box.length];
            for (int i = 0; i < box.length; i++) {
                copy[i] = new Point(box[i].getX(), box[i].getY());
            }
            return copy;
        }

        @Override
        public void lookLeft() {
            lookLeftCalls++;
        }

        @Override
        public void lookRight() {
            lookRightCalls++;
        }

        @Override
        public void rebuildScreen() {
            rebuildCalls++;
        }
    }

    /**
     * An interactable that writes every trigger/lateTrigger into a shared log.
     * <p>
     * It deliberately does not call {@code super.lateTrigger()}, so these tests only see what HudUI itself
     * triggers and don't depend on what the base class does in lateTrigger (see {@link Counting} for that).
     */
    static class Recording extends Interactable {
        private final String id;
        private final List<String> log;

        Recording(String id, MessageData message, List<String> log) {
            super(message);
            this.id = id;
            this.log = log;
        }

        @Override
        public void trigger() {
            log.add(id + ".trigger");
        }

        @Override
        public void lateTrigger() {
            log.add(id + ".late");
        }
    }

    /** An interactable that only counts trigger() calls and keeps the base class's own lateTrigger(). */
    static class Counting extends Interactable {
        int triggers = 0;

        Counting(MessageData message) {
            super(message);
        }

        @Override
        public void trigger() {
            triggers++;
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // ------------------------------------------------------------------------------------------
    // Setup / teardown
    // ------------------------------------------------------------------------------------------

    @BeforeEach
    void setUp() throws Exception {
        savedScreenWidth = GameSettings.screenWidth;
        savedScreenHeight = GameSettings.screenHeight;
        GameSettings.screenWidth = SCREEN_W;
        GameSettings.screenHeight = SCREEN_H;

        onEdt(() -> {
            room = new DummyRoom();
            useHud(new HudUI(room));
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        // Stops the typing timer so it can't leak into the next test
        onEdt(() -> hud.displayMessage(null));
        GameSettings.screenWidth = savedScreenWidth;
        GameSettings.screenHeight = savedScreenHeight;
    }

    /**
     * Makes {@code newHud} the HUD under test. Creating a {@link JLayer} with the UI installs it,
     * so installUI is not called again by hand.
     */
    private void useHud(HudUI newHud) {
        hud = newHud;
        content = new JPanel();
        content.setBounds(0, 0, SCREEN_W, SCREEN_H);
        layer = new JLayer<>(content, hud);
        layer.setSize(SCREEN_W, SCREEN_H);
    }

    // ------------------------------------------------------------------------------------------
    // Helpers: threading
    // ------------------------------------------------------------------------------------------

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
        // Rethrow as-is so a failed assertion reports as an assertion failure, not an InvocationTargetException
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

    /** Waits on the test thread, leaving the Swing thread free to run the typing timer. */
    private static void settle(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    // ------------------------------------------------------------------------------------------
    // Helpers: mouse
    // ------------------------------------------------------------------------------------------

    private MouseEvent send(int id, int x, int y) {
        MouseEvent e = new MouseEvent(content, id, System.currentTimeMillis(), 0, x, y, 1, false);
        hud.processMouseEvent(e, layer);
        return e;
    }

    private MouseEvent sendMotion(int id, int x, int y) {
        MouseEvent e = new MouseEvent(content, id, System.currentTimeMillis(), 0, x, y, 0, false);
        hud.processMouseMotionEvent(e, layer);
        return e;
    }

    /** A complete mouse click, in the order the real system sends it: press, release, clicked. */
    private void click(int x, int y) {
        send(MouseEvent.MOUSE_PRESSED, x, y);
        send(MouseEvent.MOUSE_RELEASED, x, y);
        send(MouseEvent.MOUSE_CLICKED, x, y);
    }

    private void click() {
        click(NEUTRAL_X, NEUTRAL_Y);
    }

    private void moveMouse() {
        sendMotion(MouseEvent.MOUSE_MOVED, NEUTRAL_X + 5, NEUTRAL_Y + 5);
    }

    private static int centerX(Rectangle r) {
        return r.x + r.width / 2;
    }

    private static int centerY(Rectangle r) {
        return r.y + r.height / 2;
    }

    // ------------------------------------------------------------------------------------------
    // Helpers: painting
    // ------------------------------------------------------------------------------------------

    private BufferedImage render() {
        return render(SCREEN_W, SCREEN_H);
    }

    /** Paints the HUD over a solid blue panel of the given size. Painting also works out the arrow click areas. */
    private BufferedImage render(int width, int height) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(BACKGROUND));
        panel.setSize(width, height);
        BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            hud.paint(g, panel);
        } finally {
            g.dispose();
        }
        return image;
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    private static boolean regionIsBackground(BufferedImage image, Rectangle r) {
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                if (rgb(image, x, y) != BACKGROUND) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean regionDiffers(BufferedImage a, BufferedImage b, Rectangle r) {
        for (int y = r.y; y < r.y + r.height; y++) {
            for (int x = r.x; x < r.x + r.width; x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Rectangle whole() {
        return new Rectangle(0, 0, SCREEN_W, SCREEN_H);
    }

    // ------------------------------------------------------------------------------------------
    // Helpers: messages
    // ------------------------------------------------------------------------------------------

    private static MessageData plain(String text) {
        return new MessageData(text);
    }

    private static MessageData named(String text, String name, NamePosition pos) {
        return new MessageData(text, name, pos);
    }

    /** A supplier that counts how often it is asked and always answers "no more messages". */
    private static Supplier<MessageData> endingSupplier(AtomicInteger calls) {
        return () -> {
            calls.incrementAndGet();
            return null;
        };
    }

    // ==========================================================================================
    // Initial state
    // ==========================================================================================

    @Test
    @DisplayName("A new HUD does not block input")
    void testInitialState() {
        assertFalse(hud.isBlockingInput());
    }

    @Test
    @DisplayName("The HUD asks the room for a message box that is 30% of the screen, once, on construction")
    void testConstructorAsksRoomForGeometry() {
        assertEquals(1, room.geometryCalls);
        assertEquals(0.30f, room.lastSizeRatio, 0.0001f);
    }

    // ==========================================================================================
    // displayMessage
    // ==========================================================================================

    @Nested
    @DisplayName("displayMessage")
    class DisplayMessageTests {

        @Test
        @DisplayName("Showing a message blocks input")
        void testDisplayBlocksInput() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hello World", "Speaker", NamePosition.LEFT));
                assertTrue(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A single message closes after two clicks: one to finish typing, one to dismiss")
        void testSingleMessageLifecycle() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello World"));

                click(); // finishes typing
                assertTrue(hud.isBlockingInput());

                click(); // dismisses
                // The dismissing click is swallowed until the mouse moves, so it can't hit what's underneath
                assertTrue(hud.isBlockingInput());
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Passing a null supplier explicitly behaves like the single-message overload")
        void testNullSupplier() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello"), null);
                click();
                click();
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Displaying null closes an open message immediately, without swallowing a click")
        void testDisplayNullCloses() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello"));
                assertTrue(hud.isBlockingInput());

                hud.displayMessage(null);
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Displaying null never asks the supplier for anything")
        void testDisplayNullIgnoresSupplier() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(null, endingSupplier(calls));
                assertFalse(hud.isBlockingInput());
                assertEquals(0, calls.get());
            });
        }

        @Test
        @DisplayName("Displaying null when nothing is showing does nothing")
        void testDisplayNullWhenIdle() throws Exception {
            onEdt(() -> {
                assertDoesNotThrow(() -> hud.displayMessage(null));
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("The first click only finishes typing; the supplier is asked on the second click")
        void testSupplierOnlyAskedAfterTypingFinished() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain("Some text to type"), endingSupplier(calls));

                click();
                assertEquals(0, calls.get(), "First click should only finish typing");
                assertTrue(hud.isBlockingInput());

                click();
                assertEquals(1, calls.get(), "Second click should ask for the next message");
            });
        }

        @Test
        @DisplayName("A chain of messages is walked in order and closes when the supplier returns null")
        void testMessageChain() throws Exception {
            Queue<MessageData> remaining = new ArrayDeque<>(List.of(plain("Page 2"), plain("Page 3")));
            AtomicInteger calls = new AtomicInteger();
            Supplier<MessageData> supplier = () -> {
                calls.incrementAndGet();
                return remaining.poll();
            };

            onEdt(() -> {
                hud.displayMessage(plain("Page 1"), supplier);

                click(); // page 1 typed
                click(); // -> page 2
                assertEquals(1, calls.get());
                assertTrue(hud.isBlockingInput());

                click(); // page 2 typed
                click(); // -> page 3
                assertEquals(2, calls.get());
                assertTrue(hud.isBlockingInput());

                click(); // page 3 typed
                assertTrue(hud.isBlockingInput(), "Still showing the last page");
                click(); // supplier returns null -> closes
                assertEquals(3, calls.get());
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Each page of a chain starts typing from scratch")
        void testEachPageTypesAgain() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            Supplier<MessageData> supplier = () -> calls.getAndIncrement() == 0 ? plain("Second page") : null;

            onEdt(() -> {
                hud.displayMessage(plain("First page"), supplier);
                click();
                click(); // now on page 2, freshly typing
                click(); // finishes page 2's typing, must NOT ask for page 3 yet
                assertEquals(1, calls.get());
                click();
                assertEquals(2, calls.get());
            });
        }

        @Test
        @DisplayName("Showing a new message replaces the current one and its supplier")
        void testNewMessageReplacesOld() throws Exception {
            AtomicInteger oldCalls = new AtomicInteger();
            AtomicInteger newCalls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain("Old"), endingSupplier(oldCalls));
                hud.displayMessage(plain("New"), endingSupplier(newCalls));

                click();
                click();
                assertEquals(0, oldCalls.get(), "The replaced supplier must not be used");
                assertEquals(1, newCalls.get());
            });
        }

        @Test
        @DisplayName("Closing mid-chain throws away the rest of the chain")
        void testCloseMidChain() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain("One"), endingSupplier(calls));
                hud.displayMessage(null);
                click();
                click();
                assertEquals(0, calls.get());
            });
        }

        @Test
        @DisplayName("A null message text counts as empty: there is nothing to type, so one click moves on")
        void testNullText() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain(null), endingSupplier(calls));
                assertTrue(hud.isBlockingInput());
                click();
                assertEquals(1, calls.get());
            });
        }

        @Test
        @DisplayName("An empty message text is not typed either: one click moves on")
        void testEmptyText() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain(""), endingSupplier(calls));
                click();
                assertEquals(1, calls.get());
            });
        }

        @Test
        @DisplayName("Null and empty text paint without throwing")
        void testNullAndEmptyTextPaint() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain(null));
                assertDoesNotThrow(() -> render());
                hud.displayMessage(plain(""));
                assertDoesNotThrow(() -> render());
            });
        }

        @Test
        @DisplayName("The HUD works before it is installed on a layer")
        void testWorksWithoutLayer() throws Exception {
            onEdt(() -> {
                HudUI standalone = new HudUI(new DummyRoom());
                assertDoesNotThrow(() -> standalone.displayMessage(plain("No layer yet")));
                assertTrue(standalone.isBlockingInput());
                assertDoesNotThrow(() -> standalone.displayMessage(null));
                assertFalse(standalone.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Calling from another thread is moved onto the Swing thread")
        void testDisplayFromOtherThread() throws Exception {
            assertFalse(SwingUtilities.isEventDispatchThread(), "This test must run off the Swing thread");

            hud.displayMessage(plain("From the test thread"));
            SwingUtilities.invokeAndWait(() -> { }); // let the queued call run first
            assertTrue(onEdtGet(() -> hud.isBlockingInput()));

            hud.displayMessage(null);
            SwingUtilities.invokeAndWait(() -> { });
            assertFalse(onEdtGet(() -> hud.isBlockingInput()));
        }

        @Test
        @DisplayName("The typing timer reveals the whole message on its own, so the next click advances")
        void testTimerFinishesTyping() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> hud.displayMessage(plain("Hi"), endingSupplier(calls)));

            settle(TYPING_SETTLE_MS); // Swing thread is free here, so the timer runs

            onEdt(() -> {
                click(); // nothing left to type, so this is the dismissing click
                assertEquals(1, calls.get());
            });
        }
    }

    // ==========================================================================================
    // displayInteraction
    // ==========================================================================================

    @Nested
    @DisplayName("displayInteraction")
    class InteractionTests {

        @Test
        @DisplayName("A null interaction does nothing")
        void testNullInteraction() throws Exception {
            onEdt(() -> {
                assertDoesNotThrow(() -> hud.displayInteraction(null));
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("trigger runs up front; lateTrigger runs when the message is clicked away")
        void testSingleInteractionOrdering() throws Exception {
            List<String> log = new ArrayList<>();
            onEdt(() -> {
                hud.displayInteraction(new Recording("A", plain("Message A"), log));
                assertEquals(List.of("A.trigger"), log);
                assertTrue(hud.isBlockingInput());

                click(); // finish typing
                assertEquals(List.of("A.trigger"), log, "lateTrigger must wait until the message is dismissed");

                click(); // dismiss
                assertEquals(List.of("A.trigger", "A.late"), log);
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A chain triggers the next interactable only after the previous message is dismissed")
        void testChainOrdering() throws Exception {
            List<String> log = new ArrayList<>();
            Recording a = new Recording("A", plain("A"), log);
            Recording b = new Recording("B", plain("B"), log);
            Recording c = new Recording("C", plain("C"), log);
            a.setNextTrigger(b);
            b.setNextTrigger(c);

            onEdt(() -> {
                hud.displayInteraction(a);
                assertEquals(List.of("A.trigger"), log);

                click();
                click();
                assertEquals(List.of("A.trigger", "A.late", "B.trigger"), log);
                assertTrue(hud.isBlockingInput());

                click();
                click();
                assertEquals(List.of("A.trigger", "A.late", "B.trigger", "B.late", "C.trigger"), log);
                assertTrue(hud.isBlockingInput());

                click();
                click();
                assertEquals(List.of("A.trigger", "A.late", "B.trigger", "B.late", "C.trigger", "C.late"), log);
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A first interactable with no message is triggered and skipped; the next one is shown")
        void testMessagelessFirst() throws Exception {
            List<String> log = new ArrayList<>();
            Recording a = new Recording("A", null, log);
            Recording b = new Recording("B", plain("B"), log);
            a.setNextTrigger(b);

            onEdt(() -> {
                hud.displayInteraction(a);
                assertEquals(List.of("A.trigger", "A.late", "B.trigger"), log);
                assertTrue(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A message-less interactable in the middle of a chain is skipped")
        void testMessagelessMiddle() throws Exception {
            List<String> log = new ArrayList<>();
            Recording a = new Recording("A", plain("A"), log);
            Recording b = new Recording("B", null, log);
            Recording c = new Recording("C", plain("C"), log);
            a.setNextTrigger(b);
            b.setNextTrigger(c);

            onEdt(() -> {
                hud.displayInteraction(a);
                click();
                click();
                assertEquals(List.of("A.trigger", "A.late", "B.trigger", "B.late", "C.trigger"), log);
                assertTrue(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A message-less interactable at the end of a chain closes the box")
        void testMessagelessLast() throws Exception {
            List<String> log = new ArrayList<>();
            Recording a = new Recording("A", plain("A"), log);
            Recording b = new Recording("B", null, log);
            a.setNextTrigger(b);

            onEdt(() -> {
                hud.displayInteraction(a);
                click();
                click();
                assertEquals(List.of("A.trigger", "A.late", "B.trigger", "B.late"), log);
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("If no interactable has a message, all are triggered and nothing is shown")
        void testAllMessageless() throws Exception {
            List<String> log = new ArrayList<>();
            Recording a = new Recording("A", null, log);
            Recording b = new Recording("B", null, log);
            a.setNextTrigger(b);

            onEdt(() -> {
                hud.displayInteraction(a);
                assertEquals(List.of("A.trigger", "A.late", "B.trigger", "B.late"), log);
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Closing the box mid-interaction means lateTrigger never runs")
        void testCloseMidInteraction() throws Exception {
            List<String> log = new ArrayList<>();
            onEdt(() -> {
                hud.displayInteraction(new Recording("A", plain("A"), log));
                hud.displayMessage(null);
                click();
                click();
                assertEquals(List.of("A.trigger"), log);
            });
        }

        @Test
        @DisplayName("With the base class's own lateTrigger, every interactable in a chain is triggered exactly once")
        void testEachInteractableTriggeredOnce() throws Exception {
            onEdt(() -> {
                Counting a = new Counting(plain("A"));
                Counting b = new Counting(plain("B"));
                Counting c = new Counting(null); // no message: triggered and skipped
                Counting d = new Counting(plain("D"));
                a.setNextTrigger(b);
                b.setNextTrigger(c);
                c.setNextTrigger(d);

                hud.displayInteraction(a);
                for (int i = 0; i < 3; i++) { // three messages: A, B, D
                    click();
                    click();
                }

                assertEquals(1, a.triggers, "A");
                assertEquals(1, b.triggers, "B");
                assertEquals(1, c.triggers, "C");
                assertEquals(1, d.triggers, "D");
            });
        }

        @Test
        @DisplayName("BasicInteraction chains show every message")
        void testBasicInteractionChain() throws Exception {
            onEdt(() -> {
                BasicInteraction step1 = new BasicInteraction(plain("Trigger 1"));
                BasicInteraction step2 = new BasicInteraction(plain("Trigger 2"));
                step1.setNextTrigger(step2);

                hud.displayInteraction(step1);
                assertTrue(hud.isBlockingInput());

                click();
                click();
                assertTrue(hud.isBlockingInput(), "Second interaction should now be showing");

                click();
                click();
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("FlagInteraction sets its flag on trigger and swaps in the replacement message afterwards")
        void testFlagInteractionWithReplacementMessages() throws Exception {
            onEdt(() -> {
                Player player = new Player("Hero");
                MessageData initial = new MessageData("First interact", "Guide");
                MessageData replacement = new MessageData("Repeat interact", "Guide");
                Queue<MessageData> replacements = new ArrayDeque<>();
                replacements.add(replacement);

                FlagInteraction flag = new FlagInteraction(initial, replacements, player, "OPENED_CHEST");
                assertTrue(player.flagExists("OPENED_CHEST"));
                assertFalse(player.getFlag("OPENED_CHEST"));

                hud.displayInteraction(flag);
                assertTrue(player.getFlag("OPENED_CHEST"), "Flag is set as soon as the interaction triggers");
                assertSame(initial, flag.getMessage(), "Message is not replaced until it has been read");

                click();
                click();
                assertSame(replacement, flag.getMessage(), "Message is replaced after lateTrigger");
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("FlagInteraction without replacement messages keeps its message")
        void testFlagInteractionWithoutReplacements() throws Exception {
            onEdt(() -> {
                Player player = new Player("Hero");
                MessageData only = new MessageData("Only message", "Guide");
                FlagInteraction flag = new FlagInteraction(only, player, "SEEN");

                hud.displayInteraction(flag);
                click();
                click();

                assertTrue(player.getFlag("SEEN"));
                assertSame(only, flag.getMessage());
            });
        }

        @Test
        @DisplayName("Showing the same interaction a second time uses the replacement message")
        void testInteractionRepeatUsesReplacement() throws Exception {
            onEdt(() -> {
                Player player = new Player("Hero");
                MessageData initial = new MessageData("First", "Guide");
                MessageData replacement = new MessageData("Second", "Guide");
                FlagInteraction flag = new FlagInteraction(initial, new ArrayDeque<>(List.of(replacement)), player, "F");

                hud.displayInteraction(flag);
                click();
                click();
                moveMouse();

                hud.displayInteraction(flag);
                assertTrue(hud.isBlockingInput());
                assertSame(replacement, flag.getMessage());
            });
        }

        @Test
        @DisplayName("Calling from another thread triggers right away and shows the message on the Swing thread")
        void testInteractionFromOtherThread() throws Exception {
            assertFalse(SwingUtilities.isEventDispatchThread(), "This test must run off the Swing thread");
            List<String> log = new ArrayList<>();

            hud.displayInteraction(new Recording("A", plain("A"), log));
            assertEquals(List.of("A.trigger"), log, "trigger() runs on the calling thread");

            SwingUtilities.invokeAndWait(() -> { });
            assertTrue(onEdtGet(() -> hud.isBlockingInput()));
        }
    }

    // ==========================================================================================
    // Mouse handling
    // ==========================================================================================

    @Nested
    @DisplayName("Mouse input")
    class MouseTests {

        @Test
        @DisplayName("While a message is showing every kind of mouse event is consumed")
        void testEverythingConsumedWhileShowing() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Blocking message"));

                assertTrue(send(MouseEvent.MOUSE_ENTERED, 50, 50).isConsumed());
                assertTrue(send(MouseEvent.MOUSE_EXITED, 50, 50).isConsumed());
                assertTrue(send(MouseEvent.MOUSE_PRESSED, 50, 50).isConsumed());
                assertTrue(send(MouseEvent.MOUSE_RELEASED, 50, 50).isConsumed());
                assertTrue(send(MouseEvent.MOUSE_CLICKED, 50, 50).isConsumed());
                assertTrue(sendMotion(MouseEvent.MOUSE_MOVED, 50, 50).isConsumed());
                assertTrue(sendMotion(MouseEvent.MOUSE_DRAGGED, 50, 50).isConsumed());
            });
        }

        @Test
        @DisplayName("With no message showing, mouse events are left alone")
        void testNothingConsumedWhenIdle() throws Exception {
            onEdt(() -> {
                assertFalse(send(MouseEvent.MOUSE_ENTERED, 50, 50).isConsumed());
                assertFalse(send(MouseEvent.MOUSE_EXITED, 50, 50).isConsumed());
                assertFalse(send(MouseEvent.MOUSE_PRESSED, 50, 50).isConsumed());
                assertFalse(send(MouseEvent.MOUSE_RELEASED, 50, 50).isConsumed());
                assertFalse(send(MouseEvent.MOUSE_CLICKED, NEUTRAL_X, NEUTRAL_Y).isConsumed());
                assertFalse(sendMotion(MouseEvent.MOUSE_MOVED, 50, 50).isConsumed());
                assertFalse(sendMotion(MouseEvent.MOUSE_DRAGGED, 50, 50).isConsumed());
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A message advances on release, not on press")
        void testAdvancesOnRelease() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain(""), endingSupplier(calls)); // nothing to type: one release moves on

                send(MouseEvent.MOUSE_PRESSED, NEUTRAL_X, NEUTRAL_Y);
                assertEquals(0, calls.get(), "Press alone must not advance");

                send(MouseEvent.MOUSE_RELEASED, NEUTRAL_X, NEUTRAL_Y);
                assertEquals(1, calls.get());
            });
        }

        @Test
        @DisplayName("A release with no matching press does not advance the message")
        void testReleaseWithoutPress() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain(""), endingSupplier(calls));

                MouseEvent release = send(MouseEvent.MOUSE_RELEASED, NEUTRAL_X, NEUTRAL_Y);

                assertTrue(release.isConsumed());
                assertEquals(0, calls.get());
                assertTrue(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("The click that opened a message cannot also dismiss it")
        void testOpeningClickDoesNotAdvance() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                // Press happens while nothing is showing, e.g. on an interactable object...
                send(MouseEvent.MOUSE_PRESSED, NEUTRAL_X, NEUTRAL_Y);
                // ...which opens a message before the button comes back up
                hud.displayMessage(plain("Just opened"), endingSupplier(calls));
                MouseEvent release = send(MouseEvent.MOUSE_RELEASED, NEUTRAL_X, NEUTRAL_Y);
                send(MouseEvent.MOUSE_CLICKED, NEUTRAL_X, NEUTRAL_Y);

                assertTrue(release.isConsumed());
                assertEquals(0, calls.get());

                click(); // finishes typing: if the opening click had advanced, this would ask the supplier
                assertEquals(0, calls.get(), "The opening click must not have finished typing");
                click();
                assertEquals(1, calls.get());
            });
        }

        @Test
        @DisplayName("The dismissing click stays swallowed until the mouse moves")
        void testSwallowUntilMouseMoves() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain(""));
                click(); // dismisses

                assertTrue(hud.isBlockingInput());
                assertTrue(send(MouseEvent.MOUSE_CLICKED, NEUTRAL_X, NEUTRAL_Y).isConsumed(),
                        "A stray click event right after dismissing is eaten");
                assertTrue(hud.isBlockingInput(), "Eating a click event does not end the swallowing");

                MouseEvent move = sendMotion(MouseEvent.MOUSE_MOVED, NEUTRAL_X + 5, NEUTRAL_Y + 5);
                assertFalse(move.isConsumed(), "The move itself is not consumed once the message is gone");
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Dragging also ends the swallowing")
        void testDragEndsSwallow() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain(""));
                click();
                assertTrue(hud.isBlockingInput());

                sendMotion(MouseEvent.MOUSE_DRAGGED, NEUTRAL_X, NEUTRAL_Y);
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("A new press also ends the swallowing")
        void testPressEndsSwallow() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain(""));
                click();
                assertTrue(hud.isBlockingInput());

                MouseEvent press = send(MouseEvent.MOUSE_PRESSED, NEUTRAL_X, NEUTRAL_Y);
                assertFalse(press.isConsumed());
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Moving the mouse while nothing is blocking changes nothing")
        void testMotionWhenIdle() throws Exception {
            onEdt(() -> {
                moveMouse();
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Moving the mouse while a message is showing does not unblock input")
        void testMotionWhileShowing() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello"));
                moveMouse();
                assertTrue(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Between pages of a chain input stays blocked")
        void testBlockedBetweenPages() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            Supplier<MessageData> supplier = () -> calls.getAndIncrement() == 0 ? plain("Page 2") : null;
            onEdt(() -> {
                hud.displayMessage(plain(""), supplier);
                click(); // moves to page 2
                assertTrue(hud.isBlockingInput());
                assertTrue(send(MouseEvent.MOUSE_CLICKED, NEUTRAL_X, NEUTRAL_Y).isConsumed());
            });
        }
    }

    // ==========================================================================================
    // Arrows
    // ==========================================================================================

    @Nested
    @DisplayName("Navigation arrows")
    class ArrowTests {

        @Test
        @DisplayName("Clicking the left arrow turns the view left and rebuilds the screen and geometry")
        void testLeftArrow() throws Exception {
            onEdt(() -> {
                render();
                int geometryBefore = room.geometryCalls;

                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));

                assertTrue(e.isConsumed());
                assertEquals(1, room.lookLeftCalls);
                assertEquals(0, room.lookRightCalls);
                assertEquals(1, room.rebuildCalls);
                assertEquals(geometryBefore + 1, room.geometryCalls, "Message box geometry is recalculated after turning");
            });
        }

        @Test
        @DisplayName("Clicking the right arrow turns the view right and rebuilds the screen and geometry")
        void testRightArrow() throws Exception {
            onEdt(() -> {
                render();
                int geometryBefore = room.geometryCalls;

                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));

                assertTrue(e.isConsumed());
                assertEquals(1, room.lookRightCalls);
                assertEquals(0, room.lookLeftCalls);
                assertEquals(1, room.rebuildCalls);
                assertEquals(geometryBefore + 1, room.geometryCalls);
            });
        }

        @Test
        @DisplayName("Every click on an arrow turns the view once")
        void testRepeatedClicks() throws Exception {
            onEdt(() -> {
                render();
                for (int i = 0; i < 3; i++) {
                    send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));
                }
                send(MouseEvent.MOUSE_CLICKED, centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));

                assertEquals(3, room.lookLeftCalls);
                assertEquals(1, room.lookRightCalls);
                assertEquals(4, room.rebuildCalls);
            });
        }

        @Test
        @DisplayName("Clicking away from the arrows does nothing")
        void testClickElsewhere() throws Exception {
            onEdt(() -> {
                render();

                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, NEUTRAL_X, NEUTRAL_Y);

                assertFalse(e.isConsumed());
                assertEquals(0, room.lookLeftCalls);
                assertEquals(0, room.lookRightCalls);
                assertEquals(0, room.rebuildCalls);
            });
        }

        @Test
        @DisplayName("The arrow edges: inside corner hits, one pixel past the far edge misses")
        void testArrowEdges() throws Exception {
            onEdt(() -> {
                render();

                send(MouseEvent.MOUSE_CLICKED, LEFT_ARROW.x, LEFT_ARROW.y);
                assertEquals(1, room.lookLeftCalls, "Top-left corner is inside");

                send(MouseEvent.MOUSE_CLICKED, LEFT_ARROW.x - 1, LEFT_ARROW.y);
                send(MouseEvent.MOUSE_CLICKED, LEFT_ARROW.x + LEFT_ARROW.width, LEFT_ARROW.y);
                send(MouseEvent.MOUSE_CLICKED, LEFT_ARROW.x, LEFT_ARROW.y + LEFT_ARROW.height);
                assertEquals(1, room.lookLeftCalls, "Just outside every edge is a miss");

                send(MouseEvent.MOUSE_CLICKED, RIGHT_ARROW.x + RIGHT_ARROW.width - 1, RIGHT_ARROW.y + RIGHT_ARROW.height - 1);
                assertEquals(1, room.lookRightCalls, "Bottom-right pixel is inside");
            });
        }

        @Test
        @DisplayName("Arrows only react to a completed click, not to press or release")
        void testPressAndReleaseDoNotNavigate() throws Exception {
            onEdt(() -> {
                render();

                send(MouseEvent.MOUSE_PRESSED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));
                send(MouseEvent.MOUSE_RELEASED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));
                send(MouseEvent.MOUSE_PRESSED, centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));
                send(MouseEvent.MOUSE_RELEASED, centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));

                assertEquals(0, room.lookLeftCalls);
                assertEquals(0, room.lookRightCalls);
            });
        }

        @Test
        @DisplayName("Before the first paint there are no click areas, so arrows do nothing")
        void testNoClickAreasBeforePaint() throws Exception {
            onEdt(() -> {
                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));

                assertFalse(e.isConsumed());
                assertEquals(0, room.lookLeftCalls);
            });
        }

        @Test
        @DisplayName("Arrows are dead while a message is showing, even over the arrow")
        void testArrowsBlockedWhileShowing() throws Exception {
            onEdt(() -> {
                render();
                hud.displayMessage(plain("Read me"));

                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));

                assertTrue(e.isConsumed());
                assertEquals(0, room.lookLeftCalls);
                assertEquals(0, room.rebuildCalls);
            });
        }

        @Test
        @DisplayName("Painting while a message shows removes the click areas")
        void testPaintWithMessageRemovesClickAreas() throws Exception {
            onEdt(() -> {
                render();
                hud.displayMessage(plain("Read me"));
                render(); // arrows are now the disabled "X" versions
                hud.displayMessage(null);

                // Not repainted yet, so the old (empty) click areas are still in place
                MouseEvent e = send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));
                assertFalse(e.isConsumed());
                assertEquals(0, room.lookLeftCalls);
            });
        }

        @Test
        @DisplayName("Arrows work again once the message is gone and the HUD has repainted")
        void testArrowsComeBackAfterMessage() throws Exception {
            onEdt(() -> {
                render();
                hud.displayMessage(plain(""));
                render();
                click(); // dismiss
                moveMouse();
                render();

                send(MouseEvent.MOUSE_CLICKED, centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));

                assertEquals(1, room.lookRightCalls);
            });
        }

        @Test
        @DisplayName("The click that dismisses a message cannot hit an arrow underneath it")
        void testDismissingClickDoesNotHitArrow() throws Exception {
            onEdt(() -> {
                render(); // arrows are live
                hud.displayMessage(plain(""));

                // Dismiss the message by clicking right where the left arrow is
                int x = centerX(LEFT_ARROW);
                int y = centerY(LEFT_ARROW);
                click(x, y);

                assertEquals(0, room.lookLeftCalls);
                assertEquals(0, room.rebuildCalls);

                // Another stray click event before the mouse moves is still eaten
                MouseEvent stray = send(MouseEvent.MOUSE_CLICKED, x, y);
                assertTrue(stray.isConsumed());
                assertEquals(0, room.lookLeftCalls);
            });
        }

        @Test
        @DisplayName("After the mouse moves, the same arrow works again")
        void testArrowWorksAfterSwallowEnds() throws Exception {
            onEdt(() -> {
                render();
                hud.displayMessage(plain(""));
                click(centerX(LEFT_ARROW), centerY(LEFT_ARROW));
                moveMouse();

                send(MouseEvent.MOUSE_CLICKED, centerX(LEFT_ARROW), centerY(LEFT_ARROW));

                assertEquals(1, room.lookLeftCalls);
            });
        }

        @Test
        @DisplayName("A complete click (press, release, clicked) on an arrow navigates exactly once")
        void testFullClickOnArrow() throws Exception {
            onEdt(() -> {
                render();
                click(centerX(RIGHT_ARROW), centerY(RIGHT_ARROW));
                assertEquals(1, room.lookRightCalls);
            });
        }
    }

    // ==========================================================================================
    // Painting
    // ==========================================================================================

    @Nested
    @DisplayName("Painting")
    class PaintTests {

        @Test
        @DisplayName("Idle: the arrows are drawn and there is no message box")
        void testIdleLooksLikeArrowsOnly() throws Exception {
            onEdt(() -> {
                BufferedImage image = render();

                assertFalse(regionIsBackground(image, LEFT_ARROW), "Left arrow should be drawn");
                assertFalse(regionIsBackground(image, RIGHT_ARROW), "Right arrow should be drawn");
                assertTrue(regionIsBackground(image, BOX_EMPTY_SPOT), "No message box while idle");
                assertTrue(regionIsBackground(image, LEFT_PLATE_SPOT));
                assertTrue(regionIsBackground(image, RIGHT_PLATE_SPOT));
            });
        }

        @Test
        @DisplayName("The underlying view is still painted")
        void testBackgroundStillPainted() throws Exception {
            onEdt(() -> {
                BufferedImage image = render();
                assertEquals(BACKGROUND, rgb(image, NEUTRAL_X, NEUTRAL_Y));
            });
        }

        @Test
        @DisplayName("A showing message draws the box")
        void testMessageDrawsBox() throws Exception {
            onEdt(() -> {
                BufferedImage idle = render();
                hud.displayMessage(plain("Hello"));
                BufferedImage showing = render();

                assertNotEquals(BACKGROUND, rgb(showing, BOX_EMPTY_SPOT.x, BOX_EMPTY_SPOT.y));
                assertTrue(regionDiffers(idle, showing, BOX_EMPTY_SPOT));
            });
        }

        @Test
        @DisplayName("The box disappears when the message is closed")
        void testBoxGoneAfterClose() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello"));
                hud.displayMessage(null);

                assertTrue(regionIsBackground(render(), BOX_EMPTY_SPOT));
            });
        }

        @Test
        @DisplayName("Arrows are swapped for their X versions while a message shows, in the same place")
        void testArrowsBecomeX() throws Exception {
            onEdt(() -> {
                BufferedImage idle = render();
                hud.displayMessage(plain("Hello"));
                BufferedImage showing = render();

                assertTrue(regionDiffers(idle, showing, LEFT_ARROW), "Left arrow should change to its X version");
                assertTrue(regionDiffers(idle, showing, RIGHT_ARROW), "Right arrow should change to its X version");
                assertFalse(regionIsBackground(showing, LEFT_ARROW), "X arrow is still drawn on the left");
                assertFalse(regionIsBackground(showing, RIGHT_ARROW), "X arrow is still drawn on the right");
            });
        }

        @Test
        @DisplayName("Text is not drawn until it is typed, and appears once typing is finished")
        void testTextAppearsWhenTyped() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello World"));
                BufferedImage untyped = render();

                click(); // finish typing
                BufferedImage typed = render();

                assertTrue(regionDiffers(untyped, typed, TEXT_AREA), "Typed text should be visible");
            });
        }

        @Test
        @DisplayName("The typing timer reveals text on its own")
        void testTimerRevealsText() throws Exception {
            BufferedImage untyped = onEdtGet(() -> {
                hud.displayMessage(plain("Hello World"));
                return render();
            });

            boolean revealed = false;
            long deadline = System.currentTimeMillis() + 3000;
            while (!revealed && System.currentTimeMillis() < deadline) {
                settle(50);
                BufferedImage now = onEdtGet(HudUITest.this::render);
                revealed = regionDiffers(untyped, now, TEXT_AREA);
            }

            assertTrue(revealed, "Text should appear without any click");
        }

        @Test
        @DisplayName("A newline in the message starts a new line")
        void testNewlineBreaksLine() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Top\nBottom"));
                click();
                BufferedImage twoLines = render();

                hud.displayMessage(plain("Top Bottom"));
                click();
                BufferedImage oneLine = render();

                assertTrue(regionDiffers(twoLines, oneLine, new Rectangle(326, 526, 1100, 120)));
            });
        }

        @Test
        @DisplayName("Long text wraps inside the box and lines that don't fit are not drawn")
        void testLongTextStaysInsideBox() throws Exception {
            onEdt(() -> {
                String longText = "word ".repeat(400);
                hud.displayMessage(plain(longText));
                BufferedImage untyped = render();
                click();
                BufferedImage typed = render();

                assertTrue(regionDiffers(untyped, typed, TEXT_AREA), "Text was drawn");
                // Nothing may be drawn past the right-hand text margin (box edge 1490 minus 16 padding)...
                assertFalse(regionDiffers(untyped, typed, new Rectangle(1478, 520, 8, 260)),
                        "Text must wrap before the right edge");
                // ...or below the bottom text margin (box edge 790 minus 16 padding)
                assertFalse(regionDiffers(untyped, typed, new Rectangle(326, 778, 1100, 8)),
                        "Lines that do not fit must be dropped");
            });
        }

        @Test
        @DisplayName("A left name plate is drawn only on the left")
        void testLeftNamePlate() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hi", "Bo", NamePosition.NONE));
                BufferedImage none = render();
                hud.displayMessage(named("Hi", "Bo", NamePosition.LEFT));
                BufferedImage left = render();

                assertTrue(regionDiffers(none, left, LEFT_PLATE_SPOT));
                assertFalse(regionDiffers(none, left, RIGHT_PLATE_SPOT));
            });
        }

        @Test
        @DisplayName("A right name plate is drawn only on the right")
        void testRightNamePlate() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hi", "Bo", NamePosition.NONE));
                BufferedImage none = render();
                hud.displayMessage(named("Hi", "Bo", NamePosition.RIGHT));
                BufferedImage right = render();

                assertTrue(regionDiffers(none, right, RIGHT_PLATE_SPOT));
                assertFalse(regionDiffers(none, right, LEFT_PLATE_SPOT));
            });
        }

        @Test
        @DisplayName("NamePosition.NONE draws no plate even when a name is given")
        void testNoneDrawsNoPlate() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hi", "Bo", NamePosition.NONE));
                BufferedImage image = render();

                assertTrue(regionIsBackground(image, LEFT_PLATE_SPOT));
                assertTrue(regionIsBackground(image, RIGHT_PLATE_SPOT));
            });
        }

        @Test
        @DisplayName("A null or blank name means no plate, whatever position was asked for")
        void testBlankNameDrawsNoPlate() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hi", "Bo", NamePosition.NONE));
                BufferedImage reference = render();

                for (String name : new String[]{null, "", "   "}) {
                    for (NamePosition pos : new NamePosition[]{NamePosition.LEFT, NamePosition.RIGHT}) {
                        hud.displayMessage(named("Hi", name, pos));
                        BufferedImage image = render();
                        assertFalse(regionDiffers(reference, image, whole()),
                                "name=" + name + ", pos=" + pos + " should look like a message with no name");
                    }
                }
            });
        }

        @Test
        @DisplayName("A long name is shrunk to fit the plate instead of failing")
        void testLongNameFits() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("Hi", "An Extremely Long Speaker Name Indeed", NamePosition.LEFT));
                assertDoesNotThrow(() -> render());
                hud.displayMessage(named("Hi", "An Extremely Long Speaker Name Indeed", NamePosition.RIGHT));
                assertDoesNotThrow(() -> render());
            });
        }

        @Test
        @DisplayName("Painting a second message after the first one starts clean")
        void testSecondMessageStartsClean() throws Exception {
            onEdt(() -> {
                hud.displayMessage(named("First message", "Bo", NamePosition.LEFT));
                click();
                render();

                hud.displayMessage(plain("Second"));
                BufferedImage image = render();

                assertTrue(regionIsBackground(image, LEFT_PLATE_SPOT), "Old name plate must be gone");
                assertTrue(regionIsBackground(image, RIGHT_PLATE_SPOT));
            });
        }

        @Test
        @DisplayName("A zero-sized component paints nothing and does not throw")
        void testZeroSizePaint() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello"));

                BufferedImage noWidth = render(0, 100);
                BufferedImage noHeight = render(100, 0);
                BufferedImage nothing = render(0, 0);

                assertEquals(0, noWidth.getRGB(0, 0));
                assertEquals(0, noHeight.getRGB(0, 0));
                assertEquals(0, nothing.getRGB(0, 0));
            });
        }

        @Test
        @DisplayName("A box too small to hold text or a name does not throw")
        void testTinyBox() throws Exception {
            onEdt(() -> {
                room.box = new Point[]{new Point(10, 10), new Point(40, 10), new Point(40, 30), new Point(10, 30)};
                HudUI tiny = new HudUI(room);
                useHud(tiny);

                tiny.displayMessage(named("Some text that cannot possibly fit", "Name", NamePosition.LEFT));
                assertDoesNotThrow(() -> render());
                click();
                assertDoesNotThrow(() -> render());

                tiny.displayMessage(named("More text", "Name", NamePosition.RIGHT));
                assertDoesNotThrow(() -> render());
            });
        }

        @Test
        @DisplayName("Painting works at several screen sizes, including an unset (0x0) screen")
        void testDifferentScreenSizes() throws Exception {
            int[][] sizes = {{0, 0}, {800, 600}, {1280, 720}, {3840, 2160}};
            for (int[] size : sizes) {
                GameSettings.screenWidth = size[0];
                GameSettings.screenHeight = size[1];
                onEdt(() -> {
                    HudUI sized = new HudUI(new DummyRoom());
                    useHud(sized);

                    sized.displayMessage(named("Hello there, traveller", "Bo", NamePosition.LEFT));
                    assertDoesNotThrow(() -> render(Math.max(1, size[0]), Math.max(1, size[1])));
                    click();
                    assertDoesNotThrow(() -> render(Math.max(1, size[0]), Math.max(1, size[1])));

                    sized.displayMessage(named("Hello again", "Bo", NamePosition.RIGHT));
                    assertDoesNotThrow(() -> render(Math.max(1, size[0]), Math.max(1, size[1])));
                    sized.displayMessage(null);
                });
            }
        }
    }

    // ==========================================================================================
    // Install / uninstall
    // ==========================================================================================

    @Nested
    @DisplayName("Install and uninstall")
    class LifecycleTests {

        @Test
        @DisplayName("Installing makes the layer deliver mouse and mouse-motion events")
        void testInstallSetsEventMask() throws Exception {
            onEdt(() -> {
                long expected = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
                assertEquals(expected, layer.getLayerEventMask());
            });
        }

        @Test
        @DisplayName("Uninstalling clears the layer's event mask")
        void testUninstallClearsEventMask() throws Exception {
            onEdt(() -> {
                hud.uninstallUI(layer);
                assertEquals(0L, layer.getLayerEventMask());
            });
        }

        @Test
        @DisplayName("Uninstalling stops the typing timer")
        void testUninstallStopsTimer() throws Exception {
            AtomicInteger calls = new AtomicInteger();
            onEdt(() -> {
                hud.displayMessage(plain("Hi"), endingSupplier(calls));
                hud.uninstallUI(layer);
            });

            settle(TYPING_SETTLE_MS); // a running timer would have typed "Hi" by now

            onEdt(() -> {
                click(); // if the timer was stopped, text is still untyped, so this only finishes typing
                assertEquals(0, calls.get(), "Typing should have stopped at uninstall");
            });
        }

        @Test
        @DisplayName("Messages can still be shown and closed after uninstalling")
        void testMessagesAfterUninstall() throws Exception {
            onEdt(() -> {
                hud.uninstallUI(layer);

                assertDoesNotThrow(() -> hud.displayMessage(plain("Hello")));
                assertTrue(hud.isBlockingInput());
                assertDoesNotThrow(() -> hud.displayMessage(null));
                assertFalse(hud.isBlockingInput());
            });
        }

        @Test
        @DisplayName("Uninstalling an idle HUD leaves input unblocked")
        void testUninstallIdle() throws Exception {
            onEdt(() -> {
                hud.uninstallUI(layer);
                assertFalse(hud.isBlockingInput());
            });
        }
    }

    // ==========================================================================================
    // drawMessageText with no current message
    //
    // paint() only calls drawMessageText while showMessage is true, and the public API always sets
    // showMessage and currentMessage together, so "showing with no message" cannot happen through
    // displayMessage. These tests call the private method directly, and force the impossible state
    // with reflection, to prove that it can never crash or draw stray text if it ever did.
    // They check behaviour only (no exception, nothing drawn), not how HudUI achieves it, so they
    // hold whether or not drawMessageText has an explicit null check.
    // ==========================================================================================

    @Nested
    @DisplayName("drawMessageText with no current message")
    class DrawMessageTextNoMessageTests {

        private void callDrawMessageText(Graphics2D g2d) throws Exception {
            Method m = HudUI.class.getDeclaredMethod("drawMessageText", Graphics2D.class);
            m.setAccessible(true);
            try {
                m.invoke(hud, g2d);
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Rethrow the real cause so a failure points at HudUI, not at reflection
                if (e.getCause() instanceof Exception ex) {
                    throw ex;
                }
                throw e;
            }
        }

        private Object field(String name) throws Exception {
            Field f = HudUI.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(hud);
        }

        private void setField(String name, Object value) throws Exception {
            Field f = HudUI.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(hud, value);
        }

        private BufferedImage blankImage() {
            return new BufferedImage(SCREEN_W, SCREEN_H, BufferedImage.TYPE_INT_ARGB);
        }

        private boolean anythingDrawn(BufferedImage image) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (image.getRGB(x, y) != 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Runs drawMessageText onto a blank image and returns the image. */
        private BufferedImage drawOntoBlank() throws Exception {
            BufferedImage image = blankImage();
            Graphics2D g = image.createGraphics();
            try {
                callDrawMessageText(g);
            } finally {
                g.dispose();
            }
            return image;
        }

        @Test
        @DisplayName("Before any message was ever shown, it draws nothing and does not throw")
        void testNeverShownDrawsNothing() throws Exception {
            onEdt(() -> {
                assertNull(field("currentMessage"));

                BufferedImage image = assertDoesNotThrow(this::drawOntoBlank);

                assertFalse(anythingDrawn(image));
            });
        }

        @Test
        @DisplayName("After a message is closed, it draws nothing and does not throw")
        void testAfterCloseDrawsNothing() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello World"));
                click(); // finish typing so the text is wrapped and cached
                render();
                hud.displayMessage(null); // closeMessage clears currentMessage
                assertNull(field("currentMessage"));

                BufferedImage image = assertDoesNotThrow(this::drawOntoBlank);

                assertFalse(anythingDrawn(image));
            });
        }

        @Test
        @DisplayName("Positive control: with a typed message the same call does draw text")
        void testWithMessageDraws() throws Exception {
            onEdt(() -> {
                hud.displayMessage(plain("Hello World"));
                click(); // finish typing

                BufferedImage image = drawOntoBlank();

                assertTrue(anythingDrawn(image), "Text should be drawn");
            });
        }

        @Test
        @DisplayName("Showing with no message (an impossible state forced by reflection) paints the box but no text")
        void testForcedShowWithoutMessage() throws Exception {
            onEdt(() -> {
                BufferedImage idle = render();

                setField("showMessage", true);
                assertNull(field("currentMessage"));

                BufferedImage image = assertDoesNotThrow(() -> render());

                assertTrue(regionDiffers(idle, image, BOX_EMPTY_SPOT), "The box is still drawn");
                assertTrue(regionIsBackground(image, LEFT_PLATE_SPOT), "No name plate without a message");
                assertTrue(regionIsBackground(image, RIGHT_PLATE_SPOT), "No name plate without a message");
            });
        }

        @Test
        @DisplayName("Forced show with no message leaves the text area exactly as an empty message would")
        void testForcedShowHasNoTextPixels() throws Exception {
            onEdt(() -> {
                setField("showMessage", true);
                BufferedImage noMessage = render();

                // Same box, but a real (empty) message: it also draws no text, so the pictures must match
                setField("showMessage", false);
                hud.displayMessage(plain(""));
                BufferedImage emptyMessage = render();

                assertFalse(regionDiffers(noMessage, emptyMessage, TEXT_AREA));
            });
        }
    }

    // ==========================================================================================
    // Real room
    // ==========================================================================================

    @Nested
    @DisplayName("With a real Room")
    class RealRoomTests {

        private Wall[] walls;
        private Room realRoom;

        @BeforeEach
        void buildRoom() throws Exception {
            // The screen size is already set by the outer setUp, and RoomPoints reads it on construction.
            walls = new Wall[]{new Wall(Color.RED), new Wall(Color.GREEN), new Wall(Color.BLUE), new Wall(Color.YELLOW)};
            realRoom = new Room(new Floor(Color.GRAY), new Ceiling(Color.WHITE), new RoomPoints(0.75, 0.5, 45, 1));
            for (int i = 0; i < walls.length; i++) {
                realRoom.setWall(walls[i], i);
            }
            realRoom.setLookingIndex(1);
            onEdt(() -> useHud(new HudUI(realRoom)));
        }

        /**
         * At 1920x1080 this room's back wall spans x 480..1440, so the message box is (480,756)-(1440,1080).
         * After the 10px inset the arrows are at x 350 (left) and 1450 (right), both starting at y 766.
         */
        private final Rectangle realLeftArrow = new Rectangle(350, 766, 120, 120);
        private final Rectangle realRightArrow = new Rectangle(1450, 766, 120, 120);

        @Test
        @DisplayName("The message box lines up with the room's back wall")
        void testBoxMatchesRoom(){
            Point[] box = realRoom.getMessageBoxPoints(0.30f);
            assertNotNull(box);
            assertEquals(480, box[0].getX());
            assertEquals(1440, box[1].getX());
            assertEquals(756, box[0].getY());
        }

        @Test
        @DisplayName("Clicking the arrows really turns the room, and it wraps around")
        void testArrowsTurnTheRoom() throws Exception {
            onEdt(() -> {
                render();
                assertSame(walls[1], realRoom.getLookingWall());

                send(MouseEvent.MOUSE_CLICKED, centerX(realLeftArrow), centerY(realLeftArrow));
                assertSame(walls[0], realRoom.getLookingWall());

                send(MouseEvent.MOUSE_CLICKED, centerX(realLeftArrow), centerY(realLeftArrow));
                assertSame(walls[3], realRoom.getLookingWall(), "Turning left from wall 0 wraps to wall 3");

                send(MouseEvent.MOUSE_CLICKED, centerX(realRightArrow), centerY(realRightArrow));
                assertSame(walls[0], realRoom.getLookingWall(), "Turning right from wall 3 wraps to wall 0");

                send(MouseEvent.MOUSE_CLICKED, centerX(realRightArrow), centerY(realRightArrow));
                assertSame(walls[1], realRoom.getLookingWall());
            });
        }

        @Test
        @DisplayName("The room does not turn while a message is showing")
        void testRoomStaysPutDuringMessage() throws Exception {
            onEdt(() -> {
                render();
                hud.displayMessage(plain("Hold still"));

                send(MouseEvent.MOUSE_CLICKED, centerX(realLeftArrow), centerY(realLeftArrow));
                send(MouseEvent.MOUSE_CLICKED, centerX(realRightArrow), centerY(realRightArrow));

                assertSame(walls[1], realRoom.getLookingWall());
            });
        }

        @Test
        @DisplayName("The box is drawn where the room says it should be")
        void testBoxDrawnInRoomPosition() throws Exception {
            onEdt(() -> {
                BufferedImage idle = render();
                hud.displayMessage(plain("Hello"));
                BufferedImage showing = render();

                // Well inside the box (box is (490,766)-(1430,1070) after the inset), nowhere near the text
                Rectangle inside = new Rectangle(1380, 1020, 20, 20);
                assertTrue(regionDiffers(idle, showing, inside));
            });
        }
    }
}