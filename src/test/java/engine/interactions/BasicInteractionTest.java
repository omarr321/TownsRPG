package engine.interactions;

import engine.messages.MessageData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class BasicInteractionTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream out;

    @BeforeEach
    void captureOutput() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    private static MessageData msg(String text) {
        return new MessageData(text);
    }

    private String[] outputLines() {
        String text = out.toString().trim();
        if (text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\R");
    }

    @Test
    void storesMessage() {
        MessageData data = msg("Hello");
        BasicInteraction interaction = new BasicInteraction(data);

        assertSame(data, interaction.getMessage());
        assertEquals("Hello", interaction.getMessage().getMessage());
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new BasicInteraction(msg("Hello")));
    }

    @Test
    void triggerPrintsMessage() {
        BasicInteraction interaction = new BasicInteraction(msg("Hello there"));

        interaction.trigger();

        assertEquals("Hello there", out.toString().trim());
    }

    @Test
    void triggerPrintsUpdatedMessage() {
        BasicInteraction interaction = new BasicInteraction(msg("Old"));
        interaction.setMessage(msg("New"));

        interaction.trigger();

        assertEquals("New", out.toString().trim());
    }

    @Test
    void triggerPrintsMessageTextWithoutSenderName() {
        BasicInteraction interaction = new BasicInteraction(new MessageData("Hello", "Bob"));

        interaction.trigger();

        assertEquals("Hello", out.toString().trim());
    }

    @Test
    void triggerWithoutNextDoesNotThrow() {
        BasicInteraction interaction = new BasicInteraction(msg("Hello"));

        assertDoesNotThrow(interaction::trigger);
    }

    @Test
    void triggerCanBeRepeated() {
        BasicInteraction interaction = new BasicInteraction(msg("Again"));

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"Again", "Again"}, outputLines());
    }

    @Test
    void triggerCallsNextTrigger() {
        BasicInteraction first = new BasicInteraction(msg("First"));
        BasicInteraction second = new BasicInteraction(msg("Second"));
        first.setNextTrigger(second);

        first.trigger();

        assertArrayEquals(new String[]{"First", "Second"}, outputLines());
    }

    @Test
    void triggerFollowsWholeChainInOrder() {
        BasicInteraction a = new BasicInteraction(msg("A"));
        BasicInteraction b = new BasicInteraction(msg("B"));
        BasicInteraction c = new BasicInteraction(msg("C"));
        a.setNextTrigger(b);
        b.setNextTrigger(c);

        a.trigger();

        assertArrayEquals(new String[]{"A", "B", "C"}, outputLines());
    }

    @Test
    void triggeringMiddleOfChainSkipsEarlierOnes() {
        BasicInteraction a = new BasicInteraction(msg("A"));
        BasicInteraction b = new BasicInteraction(msg("B"));
        BasicInteraction c = new BasicInteraction(msg("C"));
        a.setNextTrigger(b);
        b.setNextTrigger(c);

        b.trigger();

        assertArrayEquals(new String[]{"B", "C"}, outputLines());
    }

    @Test
    void clearingNextTriggerStopsTheChain() {
        BasicInteraction first = new BasicInteraction(msg("First"));
        first.setNextTrigger(new BasicInteraction(msg("Second")));
        first.setNextTrigger(null);

        first.trigger();

        assertArrayEquals(new String[]{"First"}, outputLines());
    }
}
