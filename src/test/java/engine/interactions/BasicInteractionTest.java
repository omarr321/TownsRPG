package engine.interactions;

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

    private String[] outputLines() {
        String text = out.toString().trim();
        if (text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\R");
    }

    @Test
    void storesMessage() {
        BasicInteraction interaction = new BasicInteraction("Hello");

        assertEquals("Hello", interaction.getMessage());
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new BasicInteraction("Hello"));
    }

    @Test
    void triggerPrintsMessage() {
        BasicInteraction interaction = new BasicInteraction("Hello there");

        interaction.trigger();

        assertEquals("Hello there", out.toString().trim());
    }

    @Test
    void triggerPrintsUpdatedMessage() {
        BasicInteraction interaction = new BasicInteraction("Old");
        interaction.setMessage("New");

        interaction.trigger();

        assertEquals("New", out.toString().trim());
    }

    @Test
    void triggerWithoutNextDoesNotThrow() {
        BasicInteraction interaction = new BasicInteraction("Hello");

        assertDoesNotThrow(interaction::trigger);
    }

    @Test
    void triggerCanBeRepeated() {
        BasicInteraction interaction = new BasicInteraction("Again");

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"Again", "Again"}, outputLines());
    }

    @Test
    void triggerCallsNextTrigger() {
        BasicInteraction first = new BasicInteraction("First");
        BasicInteraction second = new BasicInteraction("Second");
        first.setNextTrigger(second);

        first.trigger();

        assertArrayEquals(new String[]{"First", "Second"}, outputLines());
    }

    @Test
    void triggerFollowsWholeChainInOrder() {
        BasicInteraction a = new BasicInteraction("A");
        BasicInteraction b = new BasicInteraction("B");
        BasicInteraction c = new BasicInteraction("C");
        a.setNextTrigger(b);
        b.setNextTrigger(c);

        a.trigger();

        assertArrayEquals(new String[]{"A", "B", "C"}, outputLines());
    }

    @Test
    void triggeringMiddleOfChainSkipsEarlierOnes() {
        BasicInteraction a = new BasicInteraction("A");
        BasicInteraction b = new BasicInteraction("B");
        BasicInteraction c = new BasicInteraction("C");
        a.setNextTrigger(b);
        b.setNextTrigger(c);

        b.trigger();

        assertArrayEquals(new String[]{"B", "C"}, outputLines());
    }

    @Test
    void clearingNextTriggerStopsTheChain() {
        BasicInteraction first = new BasicInteraction("First");
        first.setNextTrigger(new BasicInteraction("Second"));
        first.setNextTrigger(null);

        first.trigger();

        assertArrayEquals(new String[]{"First"}, outputLines());
    }
}
