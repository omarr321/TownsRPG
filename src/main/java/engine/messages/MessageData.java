package engine.messages;

/**
 * Represents a data transfer object (DTO) that holds message details,
 * including the text content, sender name, and the positioning layout
 * for the name. Designed to be passed from the engine to the GUI for rendering.
 */
public class MessageData {
    private String message;
    private String name;
    private NamePosition namePos;

    /**
     * Constructs a new MessageData object with only a message.
     * The sender name defaults to an empty string and the name position defaults to {@link NamePosition#NONE}.
     *
     * @param message The content of the message.
     */
    public MessageData(String message) {
        this(message, "", NamePosition.NONE);
    }

    /**
     * Constructs a new MessageData object with a message and sender name.
     * The name position defaults to {@link NamePosition#LEFT}.
     *
     * @param message The content of the message.
     * @param name    The name of the sender.
     */
    public MessageData(String message, String name) {
        this(message, name, NamePosition.LEFT);
    }

    /**
     * Constructs a new MessageData object with all fields specified.
     *
     * @param message  The content of the message.
     * @param name     The name of the sender.
     * @param namePos  The display position for the sender's name.
     */
    public MessageData(String message, String name, NamePosition namePos) {
        this.message = message;
        this.name = name;
        this.namePos = namePos;
    }

    /**
     * Sets or updates the message content.
     *
     * @param message The new message content.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets or updates the sender's name.
     *
     * @param name The new sender name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets or updates the display position of the sender's name.
     *
     * @param namePos The new name position.
     */
    public void setNamePosition(NamePosition namePos) {
        this.namePos = namePos;
    }

    /**
     * Retrieves the content of the message.
     *
     * @return The message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves the sender's name.
     *
     * @return The sender name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the display position configuration for the sender's name.
     *
     * @return The {@link NamePosition} enum value.
     */
    public NamePosition getNamePos() {
        return namePos;
    }

    /**
     * Defines the alignment or position where the sender's name
     * should be rendered relative to the message bubble.
     */
    public enum NamePosition {
        /** Display the name on the left side of the message. */
        LEFT,
        /** Display the name on the right side of the message. */
        RIGHT,
        /** Do not display the name at all. */
        NONE
    }
}