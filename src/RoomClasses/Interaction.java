package RoomClasses;

public class Interaction {
    private String message;
    private InteractionType interactionType;
    private Interaction nextTrigger = null;

    public Interaction(String message, InteractionType interactionType) {
        this.message = message;
        this.interactionType = interactionType;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return this.message;
    }

    public void setInteractionType(InteractionType type) {
        this.interactionType = interactionType;
    }
    public InteractionType getInteractionType() {
        return this.interactionType;
    }

    public void setNextTrigger(Interaction i) {
        this.nextTrigger = i;
    }

    public void trigger() {
        //TODO: Replace with real logic for triggering Interactions.
        System.out.println(this + " has been triggered.");

        if (this.nextTrigger == null) {
            System.out.println("Reached the end of the trigger stack.");
            return;
        } else {
            this.nextTrigger.trigger();
        }
    }

    public enum InteractionType {
        DIALOGUE,
        FLAG,
        ITEM
    }
}
