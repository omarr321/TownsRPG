package engine.interactions;

import engine.messages.MessageData;

import java.util.Queue;

/**
 * This class is for any Interactable class. Any class that needs interaction must extend Interactable.
 */
public abstract class Interactable {
    /** The message displayed to the player when this object is triggered. */
    protected MessageData message;
    /** A queue of replacement messages to use.*/
    protected Queue<MessageData> replacementMessages;

    /** The next interactable to trigger after this one, or {@code null} if there is none. */
    protected Interactable nextTrigger = null;

    /**
     * Creates a basic interact with the message.
     * @param message The message that is output when the obj is triggered.
     */
    public Interactable(MessageData message) {
        this.message = message;
    }

    /**
     * Creates a basic interact with the message and replacement message.
     * @param message The message that is output when the obj is triggered.
     * @param replacementMessages The replacement messages to swap out.
     */
    public Interactable(MessageData message, Queue<MessageData> replacementMessages) {
        this.message = message;
        this.replacementMessages = replacementMessages;
    }

    /**
     * Sets the message to output.
     * @param message The message that is output when the obj is triggered.
     */
    public void setMessage(MessageData message) {
        this.message = message;
    }

    /**
     * Gets the current message of the triggered obj.
     * @return The current message of the triggered obj.
     */
    public MessageData getMessage() {
        return this.message;
    }

    /**
     * Sets the next trigger in the chain.
     * @param i Any class that extends Interactable.
     */
    public void setNextTrigger(Interactable i) {
        this.nextTrigger = i;
    }

    /**
     * Replaces the current message with the first one in the queue.
     */
    public void nextMessage() {
        if (this.replacementMessages == null || this.replacementMessages.isEmpty()) {
            return;
        }
        this.message = this.replacementMessages.remove();
    }

    /**
     * Method that is called when an obj is triggered.
     */
    public abstract void trigger();
}