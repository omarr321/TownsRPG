package engine.interactions;

import engine.messages.MessageData;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class InteractableTest {

    /** Minimal concrete Interactable that just counts how many times it was triggered. */
    private static class CountingInteractable extends Interactable {
        int triggerCount = 0;

        CountingInteractable(MessageData message) {
            super(message);
        }

        CountingInteractable(MessageData message, Queue<MessageData> replacementMessages) {
            super(message, replacementMessages);
        }

        @Override
        public void trigger() {
            triggerCount++;
        }
    }

    private static MessageData msg(String text) {
        return new MessageData(text);
    }

    private static Queue<MessageData> queueOf(MessageData... messages) {
        return new ArrayDeque<>(List.of(messages));
    }

    // ---- message ----

    @Test
    void constructorStoresMessage() {
        MessageData data = msg("hello");
        Interactable interactable = new CountingInteractable(data);

        assertSame(data, interactable.getMessage());
        assertEquals("hello", interactable.getMessage().getMessage());
    }

    @Test
    void constructorAllowsNullMessage() {
        Interactable interactable = new CountingInteractable(null);

        assertNull(interactable.getMessage());
    }

    @Test
    void setMessageReplacesMessage() {
        Interactable interactable = new CountingInteractable(msg("old"));
        MessageData replacement = msg("new");

        interactable.setMessage(replacement);

        assertSame(replacement, interactable.getMessage());
    }

    // ---- replacement messages ----

    @Test
    void replacementMessagesDoNotChangeTheMessageUntilNextMessageIsCalled() {
        MessageData first = msg("first");
        Interactable interactable = new CountingInteractable(first, queueOf(msg("second")));

        assertSame(first, interactable.getMessage());
    }

    @Test
    void nextMessageSwapsInTheFirstReplacement() {
        MessageData second = msg("second");
        Interactable interactable = new CountingInteractable(msg("first"), queueOf(second));

        interactable.nextMessage();

        assertSame(second, interactable.getMessage());
    }

    @Test
    void nextMessageWalksTheQueueInOrder() {
        MessageData second = msg("second");
        MessageData third = msg("third");
        Interactable interactable = new CountingInteractable(msg("first"), queueOf(second, third));

        interactable.nextMessage();
        assertSame(second, interactable.getMessage());

        interactable.nextMessage();
        assertSame(third, interactable.getMessage());
    }

    @Test
    void nextMessageKeepsLastMessageWhenQueueRunsOut() {
        MessageData second = msg("second");
        Interactable interactable = new CountingInteractable(msg("first"), queueOf(second));

        interactable.nextMessage();
        interactable.nextMessage();
        interactable.nextMessage();

        assertSame(second, interactable.getMessage());
    }

    @Test
    void nextMessageWithEmptyQueueKeepsMessage() {
        MessageData first = msg("first");
        Interactable interactable = new CountingInteractable(first, queueOf());

        interactable.nextMessage();

        assertSame(first, interactable.getMessage());
    }

    @Test
    void nextMessageWithoutAQueueKeepsMessage() {
        MessageData first = msg("first");
        Interactable interactable = new CountingInteractable(first);

        assertDoesNotThrow(interactable::nextMessage);
        assertSame(first, interactable.getMessage());
    }

    // ---- next trigger ----

    @Test
    void nextTriggerIsNullByDefault() {
        Interactable interactable = new CountingInteractable(msg("hello"));

        assertNull(interactable.nextTrigger);
    }

    @Test
    void setNextTriggerStoresTheInteractable() {
        Interactable first = new CountingInteractable(msg("first"));
        Interactable second = new CountingInteractable(msg("second"));

        first.setNextTrigger(second);

        assertSame(second, first.nextTrigger);
    }

    @Test
    void setNextTriggerCanBeCleared() {
        Interactable first = new CountingInteractable(msg("first"));

        first.setNextTrigger(new CountingInteractable(msg("second")));
        first.setNextTrigger(null);

        assertNull(first.nextTrigger);
    }

    @Test
    void settingNextTriggerDoesNotTriggerIt() {
        Interactable first = new CountingInteractable(msg("first"));
        CountingInteractable second = new CountingInteractable(msg("second"));

        first.setNextTrigger(second);

        assertEquals(0, second.triggerCount);
    }

    @Test
    void triggerIsCalledOnConcreteClass() {
        CountingInteractable interactable = new CountingInteractable(msg("hello"));

        interactable.trigger();
        interactable.trigger();

        assertEquals(2, interactable.triggerCount);
    }
}
