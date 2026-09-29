package Engine.Interactions;

public abstract class Interactable {
    protected String message;
    protected Interactable nextTrigger = null;

    public Interactable(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setNextTrigger(Interactable i) {
        this.nextTrigger = i;
    }

    public abstract void trigger();
}