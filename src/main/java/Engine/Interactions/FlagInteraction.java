package Engine.Interactions;

import Engine.FlagHolder;

public class FlagInteraction extends Interactable {
    private final FlagHolder flags;
    private String checkFlag;

    private String replaceMessage = "";

    public FlagInteraction(String message, FlagHolder flags, String checkFlag) {
        super(message);
        this.flags = flags;
        setCheckFlag(checkFlag);
    }
    public FlagInteraction(String message, String replaceMessage, FlagHolder flags, String checkFlag) {
        super(message);
        this.flags = flags;
        this.replaceMessage = replaceMessage;
        setCheckFlag(checkFlag);
    }


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