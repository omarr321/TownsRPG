package Engine.Interactions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DialogInteraction.trigger() is still a TODO in the source, so these tests only cover what exists today.
 * Add tests for the real dialog behavior once it is implemented.
 */
public class DialogInteractionTest {
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

    @Test
    void storesMessage() {
        DialogInteraction interaction = new DialogInteraction("Hi");

        assertEquals("Hi", interaction.getMessage());
    }

    @Test
    void setMessageReplacesMessage() {
        DialogInteraction interaction = new DialogInteraction("Hi");

        interaction.setMessage("Bye");

        assertEquals("Bye", interaction.getMessage());
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new DialogInteraction("Hi"));
    }

    @Test
    void triggerDoesNotThrow() {
        DialogInteraction interaction = new DialogInteraction("Hi");

        assertDoesNotThrow(interaction::trigger);
    }

    @Test
    void triggerDoesNotPrintAnything() {
        DialogInteraction interaction = new DialogInteraction("Hi");

        interaction.trigger();

        assertEquals("", out.toString());
    }

    @Test
    void nextTriggerCanBeSet() {
        DialogInteraction interaction = new DialogInteraction("Hi");
        BasicInteraction next = new BasicInteraction("Next");

        interaction.setNextTrigger(next);

        assertSame(next, interaction.nextTrigger);
    }
}
