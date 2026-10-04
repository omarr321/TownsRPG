package engine.interactions;

import engine.Player;
import engine.messages.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for FlagInteraction.
 */
class FlagInteractionTest {

    private Player flagHolder;
    private MessageData messageData;
    private static final String TEST_FLAG = "has_talked_to_npc";

    @BeforeEach
    void setUp() {
        // Player implements FlagHolder[cite: 5, 6]
        flagHolder = new Player("TestPlayer");
        messageData = new MessageData("Hello traveler!");
    }

    @Test
    void testStandardConstructorAndTrigger() {
        // Test constructor taking message, flagHolder, and checkFlag[cite: 4]
        FlagInteraction interaction = new FlagInteraction(messageData, flagHolder, TEST_FLAG);

        assertEquals(messageData, interaction.getMessage(), "Message should match the one provided in constructor.");

        // Trigger the interaction and verify that the specified flag is set to true[cite: 4]
        interaction.trigger();
        assertTrue(flagHolder.getFlag(TEST_FLAG), "Triggering the interaction should set the checkFlag to true.");
    }

    @Test
    void testNonExistentFlagAutoCreation() {
        // Verifies that if a flag does not exist, setCheckFlag / constructor creates it and defaults it to false[cite: 4, 5, 6]
        FlagInteraction interaction = new FlagInteraction(messageData, flagHolder, "brand_new_flag");

        assertTrue(flagHolder.flagExists("brand_new_flag"), "The flag should be automatically created if it doesn't exist.");
        assertFalse(flagHolder.getFlag("brand_new_flag"), "The newly auto-created flag should default to false.");
    }

    @Test
    void testConstructorWithReplacementMessages() {
        // Test overloaded constructor with replacement message queue[cite: 3, 4]
        Queue<MessageData> replacements = new LinkedList<>();
        MessageData nextMessage = new MessageData("Goodbye traveler!");
        replacements.add(nextMessage);

        FlagInteraction interaction = new FlagInteraction(messageData, replacements, flagHolder, TEST_FLAG);

        assertEquals(messageData, interaction.getMessage(), "Initial message should match.");

        // Use lateTrigger to swap to the replacement message[cite: 3]
        interaction.lateTrigger();
        assertEquals(nextMessage, interaction.getMessage(), "Message should update to the queued replacement message.");
    }

    @Test
    void testSetCheckFlagUpdatesTarget() {
        // Verifies that changing the active check flag dynamically updates behavior[cite: 4]
        FlagInteraction interaction = new FlagInteraction(messageData, flagHolder, TEST_FLAG);

        String secondaryFlag = "quest_completed";
        interaction.setCheckFlag(secondaryFlag);

        // Trigger should now affect the secondary flag instead of the initial one[cite: 4]
        interaction.trigger();

        assertFalse(flagHolder.getFlag(TEST_FLAG), "The original flag should remain untouched.");
        assertTrue(flagHolder.getFlag(secondaryFlag), "The newly set checkFlag should be updated to true upon trigger.");
    }

    @Test
    void testTriggerMultipleTimes() {
        // Ensures trigger is idempotent and behaves correctly on repeated calls[cite: 4]
        FlagInteraction interaction = new FlagInteraction(messageData, flagHolder, TEST_FLAG);

        interaction.trigger();
        assertTrue(flagHolder.getFlag(TEST_FLAG), "Flag should be true after first trigger.");

        // Triggering again should keep it true without throwing exceptions[cite: 4]
        assertDoesNotThrow(() -> interaction.trigger(), "Subsequent triggers should execute cleanly.");
        assertTrue(flagHolder.getFlag(TEST_FLAG), "Flag should remain true.");
    }
}