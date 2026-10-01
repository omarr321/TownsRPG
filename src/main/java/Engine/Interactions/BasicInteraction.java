package Engine.Interactions;

/**
 * This is a basic interaction. it prints out the message and then calls the next trigger in the chain.
 */
public class BasicInteraction extends Interactable{

    /**
     * Creates a basic interaction with message.
     * @param message The message that is printed out when the interaction is triggered.
     */
    public BasicInteraction(String message) {
        super(message);
    }

    @Override
    public void trigger() {
        System.out.println(this.message);

        if(this.nextTrigger != null) {
            this.nextTrigger.trigger();
        }
    }
}
