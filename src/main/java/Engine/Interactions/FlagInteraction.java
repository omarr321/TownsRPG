package Engine.Interactions;

import Engine.FlagHolder;

/**
 * Basic Flag Interaction that when you interact with the obj, sets a flag to true.
 */
public class FlagInteraction extends Interactable {
    private final FlagHolder flags;
    private String checkFlag;

    private String replaceMessage = "";

    /**
     * Creates a new flag interaction.
     * @param message The message that gets printed when this interaction is triggered.
     * @param flags The player flags so it can update the flags.
     * @param checkFlag The flag that will be set to true when the interaction is triggered.
     */
    public FlagInteraction(String message, FlagHolder flags, String checkFlag) {
        super(message);
        this.flags = flags;
        setCheckFlag(checkFlag);
    }

    /**
     * Creates a new flag interaction.
     * @param message The message that gets printed when this interaction is triggered.
     * @param replaceMessage The replacement message that replaces the message after first trigger.
     * @param flags The player flags so it can update the flags.
     * @param checkFlag The flag that will be set to true when the interaction is triggered.
     */
    public FlagInteraction(String message, String replaceMessage, FlagHolder flags, String checkFlag) {
        super(message);
        this.flags = flags;
        this.replaceMessage = replaceMessage;
        setCheckFlag(checkFlag);
    }

    /**
     * Changes what flag will be checked when this flag interaction is triggered.
     * @param flagName The new flag to replace the old flag.
     */
    public void setCheckFlag(String flagName) {
        this.checkFlag = flagName;
        if (!this.flags.flagExists(this.checkFlag)) {
            //System.out.println("Flag " + this.checkFlag + " does not exist, creating flag and setting it to false.");
            this.flags.addFlag(this.checkFlag, false);
        }
    }

    @Override
    public String getMessage() {
        if (this.flags.getFlag(this.checkFlag) && !(this.replaceMessage.isEmpty())) {
            this.message = this.replaceMessage;
        }
        return this.message;
    }

    @Override
    public void trigger() {
        System.out.println(this.getMessage());
        this.flags.addFlag(this.checkFlag, true);

        if(this.nextTrigger != null) {
            this.nextTrigger.trigger();
        }
    }
}