package Engine.Interactions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InteractableTest {

    /** Minimal concrete Interactable that just counts how many times it was triggered. */
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

    @Test
    void constructorStoresMessage() {
        Interactable interactable = new CountingInteractable("hello");

        assertEquals("hello", interactable.getMessage());
    }

    @Test
    void constructorAllowsNullMessage() {
        Interactable interactable = new CountingInteractable(null);

        assertNull(interactable.getMessage());
    }

    @Test
    void setMessageReplacesMessage() {
        Interactable interactable = new CountingInteractable("old");

        interactable.setMessage("new");

        assertEquals("new", interactable.getMessage());
    }

    @Test
    void nextTriggerIsNullByDefault() {
        Interactable interactable = new CountingInteractable("hello");

        assertNull(interactable.nextTrigger);
    }

    @Test
    void setNextTriggerStoresTheInteractable() {
        Interactable first = new CountingInteractable("first");
        Interactable second = new CountingInteractable("second");

        first.setNextTrigger(second);

        assertSame(second, first.nextTrigger);
    }

    @Test
    void setNextTriggerCanBeCleared() {
        Interactable first = new CountingInteractable("first");

        first.setNextTrigger(new CountingInteractable("second"));
        first.setNextTrigger(null);

        assertNull(first.nextTrigger);
    }

    @Test
    void settingNextTriggerDoesNotTriggerIt() {
        Interactable first = new CountingInteractable("first");
        CountingInteractable second = new CountingInteractable("second");

        first.setNextTrigger(second);

        assertEquals(0, second.triggerCount);
    }

    @Test
    void triggerIsCalledOnConcreteClass() {
        CountingInteractable interactable = new CountingInteractable("hello");

        interactable.trigger();
        interactable.trigger();

        assertEquals(2, interactable.triggerCount);
    }
}
