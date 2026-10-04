package gui.hud;

import engine.Player;
import engine.interactions.BasicInteraction;
import engine.interactions.FlagInteraction;
import engine.interactions.Interactable;
import engine.messages.MessageData;
import engine.room.Room;
import engine.room.objects.InteractableObj;
import engine.room.parts.RoomPoints;
import helpers.GameSettings;
import helpers.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HudUITest {

    private DummyRoom dummyRoom;
    private HudUI hudUI;
    private JLayer<JComponent> jLayer;
    private JPanel contentPanel;

    static class DummyRoom extends Room {
        boolean lookedLeft = false;
        boolean lookedRight = false;
        boolean rebuiltScreen = false;

        public DummyRoom() {
            super(null, null, new RoomPoints(0.75, 0.5, 45, 1));
        }

        @Override
        public Point[] getMessageBoxPoints(float sizeRatio) {
            return new Point[]{
                    new Point(10, 100),
                    new Point(200, 100),
                    new Point(200, 150),
                    new Point(10, 150)
            };
        }

        @Override
        public void lookLeft() {
            lookedLeft = true;
        }

        @Override
        public void lookRight() {
            lookedRight = true;
        }

        @Override
        public void rebuildScreen() {
            rebuiltScreen = true;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        runOnEDT(() -> {
            dummyRoom = new DummyRoom();
            hudUI = new HudUI(dummyRoom);

            contentPanel = new JPanel();
            contentPanel.setBounds(0, 0, GameSettings.screenWidth, GameSettings.screenHeight);
            jLayer = new JLayer<>(contentPanel, hudUI);
            hudUI.installUI(jLayer);
        });
    }

    private void runOnEDT(Runnable runnable) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeAndWait(runnable);
        }
    }

    @Test
    @DisplayName("Initialization default state check")
    void testInitialState() {
        assertFalse(hudUI.isBlockingInput(), "HUD should not block input on initial setup");
    }

    @Nested
    @DisplayName("Message Display & Chaining Tests")
    class MessageTests {

        @Test
        @DisplayName("Display single message blocks input and displays content")
        void testDisplaySingleMessage() throws Exception {
            runOnEDT(() -> {
                MessageData msg = new MessageData("Hello World", "Speaker", MessageData.NamePosition.LEFT);
                hudUI.displayMessage(msg);

                assertTrue(hudUI.isBlockingInput(), "HUD should block input while message is displayed");
            });
        }

        @Test
        @DisplayName("Display null message closes any open message")
        void testDisplayNullMessageCloses() throws Exception {
            runOnEDT(() -> {
                MessageData msg = new MessageData("Hello World", "Speaker", MessageData.NamePosition.LEFT);
                hudUI.displayMessage(msg);
                assertTrue(hudUI.isBlockingInput(), "HUD should block input when message is active");

                hudUI.displayMessage(null);
                assertFalse(hudUI.isBlockingInput(), "Displaying null message should close active message");
            });
        }

        @Test
        @DisplayName("Advancing messages via Supplier sequence")
        void testDisplayMessageChain() throws Exception {
            runOnEDT(() -> {
                MessageData msg1 = new MessageData("Page 1", "Speaker", MessageData.NamePosition.LEFT);
                MessageData msg2 = new MessageData("Page 2", "Speaker", MessageData.NamePosition.LEFT);

                AtomicInteger supplierCalls = new AtomicInteger(0);

                hudUI.displayMessage(msg1, () -> {
                    if (supplierCalls.getAndIncrement() == 0) {
                        return msg2;
                    }
                    return null;
                });

                assertTrue(hudUI.isBlockingInput());

                // Click 1: Finish typing page 1
                dispatchClick();
                assertTrue(hudUI.isBlockingInput(), "Still blocking input while showing page 1");

                // Click 2: Advance to page 2
                dispatchClick();
                assertTrue(hudUI.isBlockingInput(), "Still blocking input while showing page 2");

                // Click 3: Finish typing page 2
                dispatchClick();
                assertTrue(hudUI.isBlockingInput(), "Still blocking input at end of page 2");

                // Click 4: Close message chain
                dispatchClick();
                assertFalse(hudUI.isBlockingInput(), "Message chain should be closed after last item");
            });
        }
    }

    @Nested
    @DisplayName("Interaction Engine Domain Integration Tests")
    class InteractionTests {

        @Test
        @DisplayName("BasicInteraction triggers and executes lateTrigger sequence correctly")
        void testBasicInteractionChain() throws Exception {
            runOnEDT(() -> {
                MessageData msg1 = new MessageData("Trigger 1 Msg", "Speaker", MessageData.NamePosition.LEFT);
                MessageData msg2 = new MessageData("Trigger 2 Msg", "Speaker", MessageData.NamePosition.LEFT);

                Interactable step1 = new BasicInteraction(msg1);
                Interactable step2 = new BasicInteraction(msg2);
                step1.setNextTrigger(step2);

                hudUI.displayInteraction(step1);
                assertTrue(hudUI.isBlockingInput());

                // Step 1: Finish typing & advance
                dispatchClick();
                dispatchClick();

                // Step 2: Finish typing & advance
                dispatchClick();
                dispatchClick();

                assertFalse(hudUI.isBlockingInput(), "Interaction chain should complete and unblock input");
            });
        }

        @Test
        @DisplayName("FlagInteraction updates Player flags and replaces message queue on lateTrigger")
        void testFlagInteractionWithReplacementMessages() throws Exception {
            runOnEDT(() -> {
                Player player = new Player("Hero");
                MessageData initialMsg = new MessageData("First interact", "Guide");
                MessageData replacedMsg = new MessageData("Repeat interact", "Guide");

                Queue<MessageData> replacements = new ArrayDeque<>();
                replacements.add(replacedMsg);

                FlagInteraction flagInteract = new FlagInteraction(initialMsg, replacements, player, "OPENED_CHEST");

                // Verify initial flag state
                assertTrue(player.flagExists("OPENED_CHEST"));
                assertFalse(player.getFlag("OPENED_CHEST"));

                hudUI.displayInteraction(flagInteract);

                // Triggering should set flag to true immediately
                assertTrue(player.getFlag("OPENED_CHEST"), "Flag should be set to true on trigger");

                // Finish typing and advance to trigger lateTrigger()
                dispatchClick();
                dispatchClick();

                assertEquals(replacedMsg.getMessage(), flagInteract.getMessage().getMessage(), "Message should be replaced after lateTrigger");
                assertFalse(hudUI.isBlockingInput());
            });
        }

        @Test
        @DisplayName("InteractableObj shape contains check correctly detects point collisions")
        void testInteractableObjCollision() {
            Point[] corners = new Point[]{
                    new Point(10, 10),
                    new Point(50, 10),
                    new Point(50, 50),
                    new Point(10, 50)
            };
            InteractableObj obj = new InteractableObj(corners, Color.RED);

            assertTrue(obj.contains(30, 30), "Point inside bounding box should return true");
            assertFalse(obj.contains(5, 5), "Point outside bounding box should return false");
        }
    }

    @Nested
    @DisplayName("Mouse Input & Navigation Tests")
    class MouseInputTests {

        @Test
        @DisplayName("Mouse events consume input during active message display")
        void testMouseEventConsumedWhenMessageShowing() throws Exception {
            runOnEDT(() -> {
                MessageData msg = new MessageData("Blocking message", "Speaker", MessageData.NamePosition.NONE);
                hudUI.displayMessage(msg);

                MouseEvent pressEvent = new MouseEvent(
                        contentPanel,
                        MouseEvent.MOUSE_PRESSED,
                        System.currentTimeMillis(),
                        0,
                        50, 50,
                        1, false
                );

                sendMouseEvent(pressEvent);
                assertTrue(pressEvent.isConsumed(), "Mouse press should be consumed while message is displayed");
            });
        }

        @Test
        @DisplayName("Mouse move resets click swallow flag")
        void testMouseMotionResetsSwallowClick() throws Exception {
            runOnEDT(() -> {
                hudUI.displayMessage(new MessageData("Test", "Speaker", MessageData.NamePosition.NONE));

                long now = System.currentTimeMillis();

                // Press and release while message is active
                MouseEvent press = new MouseEvent(contentPanel, MouseEvent.MOUSE_PRESSED, now, 0, 10, 10, 1, false);
                MouseEvent release = new MouseEvent(contentPanel, MouseEvent.MOUSE_RELEASED, now + 10, 0, 10, 10, 1, false);

                sendMouseEvent(press);
                sendMouseEvent(release); // Finishes typing

                // Close message
                sendMouseEvent(press);
                sendMouseEvent(release);

                assertTrue(hudUI.isBlockingInput(), "Should temporarily block input after click to swallow release");

                // Move mouse to clear swallow click state
                MouseEvent move = new MouseEvent(contentPanel, MouseEvent.MOUSE_MOVED, now + 20, 0, 15, 15, 0, false);
                sendMouseMotionEvent(move);

                assertFalse(hudUI.isBlockingInput(), "Mouse movement should reset swallowClick flag and unblock input");
            });
        }
    }

    @Nested
    @DisplayName("UI Lifetime & Cleanup Tests")
    class CleanupTests {

        @Test
        @DisplayName("uninstallUI cleans up layer and stops processing")
        void testUninstallUI() throws Exception {
            runOnEDT(() -> {
                hudUI.uninstallUI(jLayer);
                assertFalse(hudUI.isBlockingInput(), "Input should not be blocking after uninstall");
            });
        }
    }

    private void dispatchClick() {
        long time = System.currentTimeMillis();
        MouseEvent press = new MouseEvent(contentPanel, MouseEvent.MOUSE_PRESSED, time, 0, 10, 10, 1, false);
        MouseEvent release = new MouseEvent(contentPanel, MouseEvent.MOUSE_RELEASED, time + 10, 0, 10, 10, 1, false);

        sendMouseEvent(press);
        sendMouseEvent(release);
    }

    private void sendMouseEvent(MouseEvent e) {
        try {
            Method m = LayerUI.class.getDeclaredMethod("processMouseEvent", MouseEvent.class, JLayer.class);
            m.setAccessible(true);
            m.invoke(hudUI, e, jLayer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendMouseMotionEvent(MouseEvent e) {
        try {
            Method m = LayerUI.class.getDeclaredMethod("processMouseMotionEvent", MouseEvent.class, JLayer.class);
            m.setAccessible(true);
            m.invoke(hudUI, e, jLayer);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}