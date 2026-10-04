package engine.room.objects;

import engine.interactions.BasicInteraction;
import engine.interactions.Interactable;
import engine.messages.MessageData;
import helpers.GameSettings;
import helpers.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class InteractableObjTest {
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private Point[] corners;

    private static class CountingInteractable extends Interactable {
        int triggerCount = 0;

        CountingInteractable(String message) {
            super(new MessageData(message));
        }

        @Override
        public void trigger() {
            triggerCount++;
        }
    }

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
        corners = new Point[]{new Point(10, 10), new Point(60, 10), new Point(60, 40), new Point(10, 40)};

        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(err, true));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    // ---- constructors ----

    @Test
    void isABasicObj() {
        assertInstanceOf(BasicObj.class, new InteractableObj(corners, Color.RED));
    }

    @Test
    void colorConstructor() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        assertEquals(RoomObj.DrawType.SOLID, obj.getType());
        assertEquals(Color.RED, obj.getColor());
        assertSame(corners, obj.getShapeCorners());
    }

    @Test
    void imageConstructor() {
        InteractableObj obj = new InteractableObj(corners, "/images/objects/Crate.png");

        assertEquals(RoomObj.DrawType.IMAGE, obj.getType());
        assertEquals("/images/objects/Crate.png", obj.getImagePath());
        assertTrue(obj.getWarped());
    }

    @Test
    void imageConstructorWithWarp() {
        InteractableObj obj = new InteractableObj(corners, "/images/objects/Crate.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, obj.getType());
        assertFalse(obj.getWarped());
    }

    // ---- trigger ----

    @Test
    void triggerWithoutEntryPointDoesNotThrow() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        assertDoesNotThrow(obj::trigger);
    }

    @Test
    void triggerWithoutEntryPointReportsToStdErr() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        obj.trigger();

        assertTrue(err.toString().contains("entry point"));
    }

    @Test
    void triggerCallsEntryPoint() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        CountingInteractable entry = new CountingInteractable("entry");
        obj.setEntryPoint(entry);

        obj.trigger();

        assertEquals(1, entry.triggerCount);
        assertEquals("", err.toString());
    }

    @Test
    void triggerCanBeRepeated() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        CountingInteractable entry = new CountingInteractable("entry");
        obj.setEntryPoint(entry);

        obj.trigger();
        obj.trigger();
        obj.trigger();

        assertEquals(3, entry.triggerCount);
    }

    @Test
    void triggerPrintsEntryPointMessage() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        obj.setEntryPoint(new BasicInteraction(new MessageData("You found a box.")));

        obj.trigger();

        assertEquals("You found a box.", out.toString().trim());
    }

    @Test
    void triggerRunsWholeInteractionChain() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        BasicInteraction first = new BasicInteraction(new MessageData("First"));
        BasicInteraction second = new BasicInteraction(new MessageData("Second"));
        first.setNextTrigger(second);
        obj.setEntryPoint(first);

        obj.trigger();

        assertArrayEquals(new String[]{"First", "Second"}, out.toString().trim().split("\\R"));
    }

    @Test
    void settingEntryPointDoesNotTriggerIt() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        CountingInteractable entry = new CountingInteractable("entry");

        obj.setEntryPoint(entry);

        assertEquals(0, entry.triggerCount);
    }

    @Test
    void entryPointCanBeReplaced() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        CountingInteractable first = new CountingInteractable("first");
        CountingInteractable second = new CountingInteractable("second");

        obj.setEntryPoint(first);
        obj.setEntryPoint(second);
        obj.trigger();

        assertEquals(0, first.triggerCount);
        assertEquals(1, second.triggerCount);
    }

    @Test
    void entryPointCanBeCleared() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        CountingInteractable entry = new CountingInteractable("entry");

        obj.setEntryPoint(entry);
        obj.setEntryPoint(null);
        obj.trigger();

        assertEquals(0, entry.triggerCount);
        assertTrue(err.toString().contains("entry point"));
    }

    // ---- contains ----

    @Test
    void containsPointInsideShape() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        assertTrue(obj.contains(30, 25));
    }

    @Test
    void doesNotContainPointFarOutsideShape() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        assertFalse(obj.contains(500, 500));
        assertFalse(obj.contains(0, 0));
    }

    @Test
    void doesNotContainPointJustOutsideEachSide() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        assertFalse(obj.contains(9, 25));
        assertFalse(obj.contains(61, 25));
        assertFalse(obj.contains(30, 9));
        assertFalse(obj.contains(30, 41));
    }

    @Test
    void containsFollowsNonRectangularShapes() {
        Point[] triangle = {new Point(0, 0), new Point(100, 0), new Point(0, 100)};
        InteractableObj obj = new InteractableObj(triangle, Color.RED);

        assertTrue(obj.contains(20, 20));
        assertFalse(obj.contains(80, 80));
    }

    @Test
    void containsUsesCurrentShapeCorners() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);

        obj.setShapeCorners(new Point[]{new Point(200, 200), new Point(260, 200), new Point(260, 240), new Point(200, 240)});

        assertFalse(obj.contains(30, 25));
        assertTrue(obj.contains(230, 220));
    }
}
