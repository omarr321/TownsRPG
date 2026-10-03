package engine.messages;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageDataTest {
    private MessageData testMessage;

    @BeforeEach
    public void createNewMessage() {
        testMessage = new MessageData("");
    }

    @Test
    void checkNewMessage() {
        assertEquals("", this.testMessage.getMessage());
        assertEquals("", this.testMessage.getName());
        assertEquals(MessageData.NamePosition.NONE, this.testMessage.getNamePos());

        this.testMessage = new MessageData("message", "name");
        assertEquals("message", this.testMessage.getMessage());
        assertEquals("name", this.testMessage.getName());
        assertEquals(MessageData.NamePosition.LEFT, this.testMessage.getNamePos());

        this.testMessage = new MessageData("message", "name", MessageData.NamePosition.RIGHT);
        assertEquals("message", this.testMessage.getMessage());
        assertEquals("name", this.testMessage.getName());
        assertEquals(MessageData.NamePosition.RIGHT, this.testMessage.getNamePos());
    }

    @ParameterizedTest
    @CsvSource({
            "message, name, RIGHT",
            "This is a test, Omar, LEFT",
            "The inner machinations of my mind are an enigma, Patrick Star, NONE",
            "Pizza is good, YO MAMA, RIGHT",

    })
    void checkMessageValues(String message, String name, MessageData.NamePosition namePos) {
        this.testMessage = new MessageData(message, name, namePos);
        assertEquals(message, this.testMessage.getMessage());
        assertEquals(name, this.testMessage.getName());
        assertEquals(namePos, this.testMessage.getNamePos());
    }

    @ParameterizedTest
    @CsvSource({
            "message, name, RIGHT",
            "This is a test, Omar, LEFT",
            "The inner machinations of my mind are an enigma, Patrick Star, NONE",
            "Pizza is good, YO MAMA, RIGHT",

    })
    void checkSetMessageValues(String message, String name, MessageData.NamePosition namePos) {
        this.testMessage = new MessageData("");
        this.testMessage.setMessage(message);
        this.testMessage.setName(name);
        this.testMessage.setNamePosition(namePos);
        assertEquals(message, this.testMessage.getMessage());
        assertEquals(name, this.testMessage.getName());
        assertEquals(namePos, this.testMessage.getNamePos());
    }
}
