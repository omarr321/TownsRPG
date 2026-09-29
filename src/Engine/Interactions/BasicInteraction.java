package Engine.Interactions;

public class BasicInteraction extends Interactable{
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
