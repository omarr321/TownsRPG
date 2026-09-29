package Engine.Interactions;

public abstract class Interactable {
    private String message;
    private FlagInteraction nextTrigger = null;

    public Interactable(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return this.message;
    }

    public void setNextTrigger(FlagInteraction i) {
        this.nextTrigger = i;
    }

    public abstract void trigger();
}