package engine.interactions;

import engine.messages.MessageData;
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

    private static MessageData msg(String text) {
        return new MessageData(text);
    }

    @Test
    void storesMessage() {
        MessageData data = msg("Hi");
        DialogInteraction interaction = new DialogInteraction(data);

        assertSame(data, interaction.getMessage());
        assertEquals("Hi", interaction.getMessage().getMessage());
    }

    @Test
    void setMessageReplacesMessage() {
        DialogInteraction interaction = new DialogInteraction(msg("Hi"));

        interaction.setMessage(msg("Bye"));

        assertEquals("Bye", interaction.getMessage().getMessage());
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new DialogInteraction(msg("Hi")));
    }

    @Test
    void triggerDoesNotThrow() {
        DialogInteraction interaction = new DialogInteraction(msg("Hi"));

        assertDoesNotThrow(interaction::trigger);
    }

    @Test
    void triggerDoesNotPrintAnything() {
        DialogInteraction interaction = new DialogInteraction(msg("Hi"));

        interaction.trigger();

        assertEquals("", out.toString());
    }

    @Test
    void nextTriggerCanBeSet() {
        DialogInteraction interaction = new DialogInteraction(msg("Hi"));
        BasicInteraction next = new BasicInteraction(msg("Next"));

        interaction.setNextTrigger(next);

        assertSame(next, interaction.nextTrigger);
    }
}
