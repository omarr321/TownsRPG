package engine.interactions;

import engine.messages.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for BasicInteraction and its inherited Interactable functionalities.
 */
class BasicInteractionTest {

    private MessageData initialMessage;
    private BasicInteraction interaction;

    @BeforeEach
    void setUp() {
        initialMessage = new MessageData("Hello, World!", "TestUser", MessageData.NamePosition.LEFT);
        interaction = new BasicInteraction(initialMessage);
    }

    @Test
    void testConstructorAndGetMessage() {
        // Verifies that the message passed during construction is correctly set and retrievable
        assertNotNull(interaction.getMessage(), "The message should not be null.");
        assertEquals(initialMessage, interaction.getMessage(), "The message should match the one provided in the constructor.");
        assertEquals("Hello, World!", interaction.getMessage().getMessage(), "The message text should match[cite: 1].");
        assertEquals("TestUser", interaction.getMessage().getName(), "The sender name should match[cite: 1].");
    }

    @Test
    void testSetMessage() {
        // Verifies that updating the message works as expected
        MessageData newMessage = new MessageData("Updated message content");
        interaction.setMessage(newMessage);

        assertEquals(newMessage, interaction.getMessage(), "The current message should be updated to the new MessageData object.");
        assertEquals("Updated message content", interaction.getMessage().getMessage(), "The new message text should match[cite: 1].");
    }

    @Test
    void testTriggerExecution() {
        // BasicInteraction has an empty trigger method; ensures it executes cleanly without throwing exceptions[cite: 2]
        assertDoesNotThrow(() -> interaction.trigger(), "Triggering a BasicInteraction should execute successfully[cite: 2].");
    }

    @Test
    void testNextTriggerChaining() {
        // Verifies setting and getting the next interactable trigger in the chain
        assertNull(interaction.getNextTrigger(), "The next trigger should initially be null[cite: 3].");

        MessageData nextMessageData = new MessageData("Next interaction");
        BasicInteraction nextInteraction = new BasicInteraction(nextMessageData);

        interaction.setNextTrigger(nextInteraction);
        assertNotNull(interaction.getNextTrigger(), "The next trigger should now be set[cite: 3].");
        assertEquals(nextInteraction, interaction.getNextTrigger(), "The retrieved next trigger should match the one assigned[cite: 3].");
    }

    @Test
    void testLateTriggerWithNoReplacementMessages() {
        // Verifies that lateTrigger executes safely when no replacement messages exist[cite: 3]
        assertDoesNotThrow(() -> interaction.lateTrigger(), "lateTrigger should execute safely when replacement messages are null[cite: 3].");
        assertEquals(initialMessage, interaction.getMessage(), "The message should remain unchanged if there are no replacement messages[cite: 3].");
    }

    @Test
    void testReplacementMessagesQueueBehavior() {
        // Tests queue-based message replacement inherited from Interactable[cite: 3]
        Queue<MessageData> replacements = new LinkedList<>();
        MessageData firstReplacement = new MessageData("First replacement");
        MessageData secondReplacement = new MessageData("Second replacement");

        replacements.add(firstReplacement);
        replacements.add(secondReplacement);

        // Subclassing or leveraging package visibility to test replacement messages queue initialization
        BasicInteraction customInteraction = new BasicInteraction(initialMessage) {
            {
                this.replacementMessages = replacements;
            }
        };

        // Trigger first message replacement via lateTrigger
        customInteraction.lateTrigger();
        assertEquals(firstReplacement, customInteraction.getMessage(), "Message should update to the first replacement in the queue[cite: 3].");

        // Trigger second message replacement
        customInteraction.lateTrigger();
        assertEquals(secondReplacement, customInteraction.getMessage(), "Message should update to the second replacement in the queue[cite: 3].");

        // Subsequent lateTrigger should not fail when queue is empty
        assertDoesNotThrow(() -> customInteraction.lateTrigger(), "lateTrigger should handle empty replacement queues gracefully[cite: 3].");
    }
}