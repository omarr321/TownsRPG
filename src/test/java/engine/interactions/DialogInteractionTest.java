package engine.interactions;

import engine.messages.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for DialogInteraction.
 */
class DialogInteractionTest {

    private MessageData messageData;
    private DialogInteraction dialogInteraction;

    @BeforeEach
    void setUp() {
        messageData = new MessageData("Welcome to the conversation!", "NPC", MessageData.NamePosition.LEFT);
        dialogInteraction = new DialogInteraction(messageData);
    }

    @Test
    void testConstructorAndGetMessage() {
        // Verifies message is properly assigned via constructor and retrievable[cite: 3, 7]
        assertNotNull(dialogInteraction.getMessage(), "Message should not be null.");
        assertEquals(messageData, dialogInteraction.getMessage(), "Message should match the provided object[cite: 3, 7].");
        assertEquals("Welcome to the conversation!", dialogInteraction.getMessage().getMessage(), "Message text should match[cite: 1].");
        assertEquals("NPC", dialogInteraction.getMessage().getName(), "Sender name should match[cite: 1].");
    }

    @Test
    void testSetMessage() {
        // Verifies updating the message content works via inherited Interactable methods[cite: 3]
        MessageData newMessage = new MessageData("Updated dialog line.");
        dialogInteraction.setMessage(newMessage);

        assertEquals(newMessage, dialogInteraction.getMessage(), "Message should be updated successfully[cite: 3].");
        assertEquals("Updated dialog line.", dialogInteraction.getMessage().getMessage(), "New message text should match[cite: 1].");
    }

    @Test
    void testTriggerExecutionStub() {
        // Ensures triggering the stub executes without error[cite: 7]
        assertDoesNotThrow(() -> dialogInteraction.trigger(), "Triggering DialogInteraction should execute cleanly even if not fully implemented[cite: 7].");
    }

    @Test
    void testNextTriggerChaining() {
        // Verifies chaining dialog interactions together[cite: 3]
        assertNull(dialogInteraction.getNextTrigger(), "Next trigger should initially be null[cite: 3].");

        DialogInteraction nextDialog = new DialogInteraction(new MessageData("Next line of dialog."));
        dialogInteraction.setNextTrigger(nextDialog);

        assertNotNull(dialogInteraction.getNextTrigger(), "Next trigger should be set[cite: 3].");
        assertEquals(nextDialog, dialogInteraction.getNextTrigger(), "The chained trigger should match[cite: 3].");
    }

    @Test
    void testQueueReplacementMessagesInheritance() {
        // Tests queue-based replacement messages for future multi-line dialog sequencing[cite: 3]
        Queue<MessageData> replacementQueue = new LinkedList<>();
        MessageData lineTwo = new MessageData("Line two of dialogue.");
        replacementQueue.add(lineTwo);

        // Subclass instantiation to inject queue since constructor takes only MessageData[cite: 3, 7]
        DialogInteraction queuedDialog = new DialogInteraction(messageData) {
            {
                this.replacementMessages = replacementQueue;
            }
        };

        assertEquals(messageData, queuedDialog.getMessage(), "Initial message should match.");

        // Simulate advancing to the next queued message via lateTrigger[cite: 3]
        queuedDialog.lateTrigger();
        assertEquals(lineTwo, queuedDialog.getMessage(), "Message should advance to the next line in the queue[cite: 3].");
    }
}