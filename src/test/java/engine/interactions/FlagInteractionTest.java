package engine.interactions;

import engine.FlagHolder;
import engine.Player;
import engine.messages.MessageData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

public class FlagInteractionTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream out;
    private Player player;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true));
        player = new Player("Tester");
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    private static MessageData msg(String text) {
        return new MessageData(text);
    }

    private static Queue<MessageData> queueOf(String... texts) {
        Queue<MessageData> queue = new ArrayDeque<>();
        for (String text : texts) {
            queue.add(msg(text));
        }
        return queue;
    }

    private String[] outputLines() {
        String text = out.toString().trim();
        if (text.isEmpty()) {
            return new String[0];
        }
        return text.split("\\R");
    }

    // ---- constructors / flag creation ----

    @Test
    void constructorCreatesMissingFlagAsFalse() {
        new FlagInteraction(msg("msg"), player, "seen");

        assertTrue(player.flagExists("seen"));
        assertFalse(player.getFlag("seen"));
    }

    @Test
    void constructorKeepsExistingTrueFlag() {
        player.addFlag("seen", true);

        new FlagInteraction(msg("msg"), player, "seen");

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void replacementMessagesConstructorCreatesMissingFlag() {
        new FlagInteraction(msg("msg"), queueOf("replaced"), player, "seen");

        assertTrue(player.flagExists("seen"));
        assertFalse(player.getFlag("seen"));
    }

    @Test
    void replacementMessagesConstructorKeepsExistingTrueFlag() {
        player.addFlag("seen", true);

        new FlagInteraction(msg("msg"), queueOf("replaced"), player, "seen");

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new FlagInteraction(msg("msg"), player, "seen"));
    }

    // ---- getMessage ----

    @Test
    void getMessageReturnsTheOriginalMessageBeforeTrigger() {
        MessageData original = msg("msg");
        FlagInteraction interaction = new FlagInteraction(original, queueOf("replaced"), player, "seen");

        assertSame(original, interaction.getMessage());
    }

    @Test
    void getMessageReturnsFirstReplacementAfterTrigger() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), queueOf("replaced"), player, "seen");

        interaction.trigger();

        assertEquals("replaced", interaction.getMessage().getMessage());
    }

    @Test
    void getMessageKeepsOriginalAfterTriggerWhenThereAreNoReplacements() {
        MessageData original = msg("msg");
        FlagInteraction interaction = new FlagInteraction(original, player, "seen");

        interaction.trigger();

        assertSame(original, interaction.getMessage());
    }

    @Test
    void getMessageKeepsOriginalWhenReplacementQueueIsEmpty() {
        MessageData original = msg("msg");
        FlagInteraction interaction = new FlagInteraction(original, queueOf(), player, "seen");

        interaction.trigger();

        assertSame(original, interaction.getMessage());
    }

    @Test
    void settingTheFlagElsewhereDoesNotChangeTheMessage() {
        MessageData original = msg("msg");
        FlagInteraction interaction = new FlagInteraction(original, queueOf("replaced"), player, "seen");

        player.addFlag("seen", true);

        assertSame(original, interaction.getMessage());
    }

    // ---- trigger ----

    @Test
    void triggerPrintsMessage() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "seen");
        out.reset();

        interaction.trigger();

        assertArrayEquals(new String[]{"msg"}, outputLines());
    }

    @Test
    void triggerSetsFlagToTrue() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "seen");

        interaction.trigger();

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void triggerSetsFlagToTrueWhenThereAreReplacements() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), queueOf("replaced"), player, "seen");

        interaction.trigger();

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void secondTriggerPrintsReplacementMessage() {
        FlagInteraction interaction = new FlagInteraction(msg("first time"), queueOf("second time"), player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"first time", "second time"}, outputLines());
    }

    @Test
    void triggersWalkTheReplacementQueueInOrder() {
        FlagInteraction interaction = new FlagInteraction(msg("one"), queueOf("two", "three"), player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"one", "two", "three"}, outputLines());
    }

    @Test
    void lastReplacementRepeatsOnceTheQueueRunsOut() {
        FlagInteraction interaction = new FlagInteraction(msg("one"), queueOf("two"), player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();
        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"one", "two", "two", "two"}, outputLines());
    }

    @Test
    void secondTriggerWithoutReplacementsPrintsSameMessage() {
        FlagInteraction interaction = new FlagInteraction(msg("same"), player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"same", "same"}, outputLines());
    }

    @Test
    void triggerWithoutReplacementsDoesNotThrow() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "seen");

        assertDoesNotThrow(interaction::trigger);
    }

    @Test
    void triggerCallsNextTriggerAfterPrinting() {
        FlagInteraction first = new FlagInteraction(msg("First"), player, "flag1");
        FlagInteraction second = new FlagInteraction(msg("Second"), player, "flag2");
        first.setNextTrigger(second);
        out.reset();

        first.trigger();

        assertArrayEquals(new String[]{"First", "Second"}, outputLines());
        assertTrue(player.getFlag("flag1"));
        assertTrue(player.getFlag("flag2"));
    }

    @Test
    void triggerCanChainIntoBasicInteraction() {
        FlagInteraction first = new FlagInteraction(msg("First"), player, "flag1");
        first.setNextTrigger(new BasicInteraction(msg("Basic")));
        out.reset();

        first.trigger();

        assertArrayEquals(new String[]{"First", "Basic"}, outputLines());
    }

    // ---- setCheckFlag ----

    @Test
    void setCheckFlagCreatesNewFlagAsFalse() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "old");

        interaction.setCheckFlag("new");

        assertTrue(player.flagExists("new"));
        assertFalse(player.getFlag("new"));
    }

    @Test
    void setCheckFlagSwitchesWhichFlagTriggerSets() {
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "old");

        interaction.setCheckFlag("other");
        interaction.trigger();

        assertTrue(player.getFlag("other"));
        assertFalse(player.getFlag("old"));
    }

    @Test
    void setCheckFlagKeepsExistingFlagValue() {
        player.addFlag("existing", true);
        FlagInteraction interaction = new FlagInteraction(msg("msg"), player, "old");

        interaction.setCheckFlag("existing");

        assertTrue(player.getFlag("existing"));
    }

    @Test
    void worksWithAnyFlagHolder() {
        FlagHolder holder = new FlagHolder() {
            private final java.util.Map<String, Boolean> map = new java.util.HashMap<>();

            @Override
            public boolean flagExists(String name) {
                return map.containsKey(name);
            }

            @Override
            public boolean getFlag(String name) {
                return map.get(name);
            }

            @Override
            public void addFlag(String name, boolean val) {
                map.put(name, val);
            }
        };

        FlagInteraction interaction = new FlagInteraction(msg("msg"), holder, "custom");
        interaction.trigger();

        assertTrue(holder.getFlag("custom"));
    }
}
