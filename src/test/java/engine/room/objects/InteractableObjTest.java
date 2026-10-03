package engine.room.objects;

import engine.interactions.BasicInteraction;
import engine.interactions.Interactable;
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
            super(message);
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
        obj.setEntryPoint(new BasicInteraction("You found a box."));

        obj.trigger();

        assertEquals("You found a box.", out.toString().trim());
    }

    @Test
    void triggerRunsWholeInteractionChain() {
        InteractableObj obj = new InteractableObj(corners, Color.RED);
        BasicInteraction first = new BasicInteraction("First");
        BasicInteraction second = new BasicInteraction("Second");
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
}
