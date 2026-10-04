package engine.interactions;

import engine.FlagHolder;
import engine.messages.MessageData;

import java.util.Queue;

/**
 * Basic Flag Interaction that when you interact with the obj, sets a flag to true.
 */
public class FlagInteraction extends Interactable {
    private final FlagHolder flags;
    private String checkFlag;

    /**
     * Creates a new flag interaction.
     * @param message The message that gets printed when this interaction is triggered.
     * @param flags The player flags so it can update the flags.
     * @param checkFlag The flag that will be set to true when the interaction is triggered.
     */
    public FlagInteraction(MessageData message, FlagHolder flags, String checkFlag) {
        super(message);
        this.flags = flags;
        setCheckFlag(checkFlag);
    }

    /**
     * Creates a new flag interaction.
     * @param message The message that gets printed when this interaction is triggered.
     * @param replaceMessages The replacement messages that replaces the message after triggers.
     * @param flags The player flags so it can update the flags.
     * @param checkFlag The flag that will be set to true when the interaction is triggered.
     */
    public FlagInteraction(MessageData message, Queue<MessageData> replaceMessages, FlagHolder flags, String checkFlag) {
        super(message, replaceMessages);
        this.flags = flags;
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
    public void trigger() {
        System.out.println(this.getMessage().getMessage());
        this.nextMessage();
        this.flags.addFlag(this.checkFlag, true);

        if(this.nextTrigger != null) {
            this.nextTrigger.trigger();
        }
    }
}