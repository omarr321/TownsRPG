package Engine.Interactions;

/**
 * This class is for any Interactable class. Any class that needs interaction must extend Interactable.
 */
public abstract class Interactable {
    protected String message;
    protected Interactable nextTrigger = null;

    /**
     * Creates a basic interact with the message,
     * @param message The message that is output when the obj is triggered.
     */
    public Interactable(String message) {
        this.message = message;
    }

    /**
     * Sets the message to output.
     * @param message The message that is output when the obj is triggered.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the current message of the triggered obj.
     * @return The current message of the triggered obj.
     */
    public String getMessage() {
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
     * Method that is called when an obj is triggered.
     */
    public abstract void trigger();
}