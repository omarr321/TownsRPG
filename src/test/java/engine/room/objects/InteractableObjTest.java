package engine.room.objects;

import engine.interactions.Interactable;
import engine.interactions.BasicInteraction;
import engine.messages.MessageData;
import helpers.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for InteractableObj and its interactions.
 */
class InteractableObjTest {

    private Point[] sampleCorners;
    private InteractableObj colorObj;
    private InteractableObj imageObj;

    @BeforeEach
    void setUp() {
        // Define a simple square shape using engine.helpers.Point
        sampleCorners = new Point[]{
                new Point(0, 0),
                new Point(100, 0),
                new Point(100, 100),
                new Point(0, 100)
        };

        colorObj = new InteractableObj(sampleCorners, Color.RED);
        imageObj = new InteractableObj(sampleCorners, "assets/test_image.png", true);
    }

    @Test
    void testConstructorsAndInitialProperties() {
        // Test color constructor
        assertArrayEquals(sampleCorners, colorObj.getShapeCorners(), "Shape corners should match[cite: 8, 9].");
        assertEquals(RoomObj.DrawType.SOLID, colorObj.getType(), "Color-based object should have DrawType SOLID[cite: 8, 10].");
        assertEquals(Color.RED, colorObj.getColor(), "Color should match the one passed in constructor[cite: 8, 10].");
        assertNull(colorObj.getEntryPoint(), "Entry point should initially be null[cite: 9].");

        // Test image constructor with warp flag
        assertEquals(RoomObj.DrawType.IMAGE, imageObj.getType(), "Image-based object should have DrawType IMAGE[cite: 8, 10].");
        assertEquals("assets/test_image.png", imageObj.getImagePath(), "Image path should match[cite: 8, 10].");
        assertTrue(imageObj.getWarped(), "Warped property should be true[cite: 8, 10].");
    }

    @Test
    void testSetAndGetEntryPoint() {
        // Create a mock/basic interactable to act as the entry point
        Interactable interaction = new BasicInteraction(new MessageData("Interaction triggered!"));

        assertNull(colorObj.getEntryPoint(), "Entry point should start as null[cite: 9].");

        colorObj.setEntryPoint(interaction);
        assertEquals(interaction, colorObj.getEntryPoint(), "Get entry point should return the assigned interactable[cite: 9].");
    }

    @Test
    void testTriggerWithEntryPoint() {
        // Verifies that triggering the object successfully calls the underlying interactable's trigger behavior
        AtomicBoolean wasTriggered = new AtomicBoolean(false);

        Interactable customInteraction = new BasicInteraction(new MessageData("Test")) {
            @Override
            public void trigger() {
                wasTriggered.set(true);
            }
        };

        colorObj.setEntryPoint(customInteraction);
        colorObj.trigger();

        assertTrue(wasTriggered.get(), "Triggering the InteractableObj should invoke the entry point's trigger method[cite: 9].");
    }

    @Test
    void testTriggerWithoutEntryPoint() {
        // Ensures triggering an object with no entry point executes safely without throwing exceptions (logs to stderr)[cite: 9]
        assertDoesNotThrow(() -> colorObj.trigger(), "Triggering an object with a null entry point should handle it gracefully[cite: 9].");
    }

    @Test
    void testContainsPoint() {
        // Test point-in-polygon containment logic provided by InteractableObj
        // Inside points
        assertTrue(colorObj.contains(50, 50), "Point (50, 50) should be inside the 100x100 box[cite: 9].");
        assertTrue(colorObj.contains(10, 90), "Point (10, 90) should be inside the box[cite: 9].");

        // Outside points
        assertFalse(colorObj.contains(150, 50), "Point (150, 50) should be outside the box[cite: 9].");
        assertFalse(colorObj.contains(-10, 50), "Point (-10, 50) should be outside the box[cite: 9].");
    }
}