package engine.interactions;

import engine.FlagHolder;
import engine.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
        new FlagInteraction("msg", player, "seen");

        assertTrue(player.flagExists("seen"));
        assertFalse(player.getFlag("seen"));
    }

    @Test
    void constructorKeepsExistingTrueFlag() {
        player.addFlag("seen", true);

        new FlagInteraction("msg", player, "seen");

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void replaceMessageConstructorCreatesMissingFlag() {
        new FlagInteraction("msg", "replaced", player, "seen");

        assertTrue(player.flagExists("seen"));
        assertFalse(player.getFlag("seen"));
    }

    @Test
    void isAnInteractable() {
        assertInstanceOf(Interactable.class, new FlagInteraction("msg", player, "seen"));
    }

    // ---- getMessage ----

    @Test
    void getMessageReturnsMessageWhileFlagIsFalse() {
        FlagInteraction interaction = new FlagInteraction("msg", "replaced", player, "seen");

        assertEquals("msg", interaction.getMessage());
    }

    @Test
    void getMessageReturnsReplaceMessageOnceFlagIsTrue() {
        FlagInteraction interaction = new FlagInteraction("msg", "replaced", player, "seen");

        player.addFlag("seen", true);

        assertEquals("replaced", interaction.getMessage());
    }

    @Test
    void getMessageKeepsOriginalWhenFlagIsTrueButNoReplaceMessage() {
        FlagInteraction interaction = new FlagInteraction("msg", player, "seen");

        player.addFlag("seen", true);

        assertEquals("msg", interaction.getMessage());
    }

    @Test
    void emptyReplaceMessageIsIgnored() {
        FlagInteraction interaction = new FlagInteraction("msg", "", player, "seen");

        player.addFlag("seen", true);

        assertEquals("msg", interaction.getMessage());
    }

    // ---- trigger ----

    @Test
    void triggerPrintsMessage() {
        FlagInteraction interaction = new FlagInteraction("msg", player, "seen");
        out.reset();

        interaction.trigger();

        assertArrayEquals(new String[]{"msg"}, outputLines());
    }

    @Test
    void triggerSetsFlagToTrue() {
        FlagInteraction interaction = new FlagInteraction("msg", player, "seen");

        interaction.trigger();

        assertTrue(player.getFlag("seen"));
    }

    @Test
    void secondTriggerPrintsReplaceMessage() {
        FlagInteraction interaction = new FlagInteraction("first time", "second time", player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"first time", "second time"}, outputLines());
    }

    @Test
    void secondTriggerWithoutReplaceMessagePrintsSameMessage() {
        FlagInteraction interaction = new FlagInteraction("same", player, "seen");
        out.reset();

        interaction.trigger();
        interaction.trigger();

        assertArrayEquals(new String[]{"same", "same"}, outputLines());
    }

    @Test
    void triggerUsesFlagAlreadySetElsewhere() {
        FlagInteraction interaction = new FlagInteraction("first time", "second time", player, "seen");
        player.addFlag("seen", true);
        out.reset();

        interaction.trigger();

        assertArrayEquals(new String[]{"second time"}, outputLines());
    }

    @Test
    void triggerCallsNextTriggerAfterPrinting() {
        FlagInteraction first = new FlagInteraction("First", player, "flag1");
        FlagInteraction second = new FlagInteraction("Second", player, "flag2");
        first.setNextTrigger(second);
        out.reset();

        first.trigger();

        assertArrayEquals(new String[]{"First", "Second"}, outputLines());
        assertTrue(player.getFlag("flag1"));
        assertTrue(player.getFlag("flag2"));
    }

    @Test
    void triggerCanChainIntoBasicInteraction() {
        FlagInteraction first = new FlagInteraction("First", player, "flag1");
        first.setNextTrigger(new BasicInteraction("Basic"));
        out.reset();

        first.trigger();

        assertArrayEquals(new String[]{"First", "Basic"}, outputLines());
    }

    // ---- setCheckFlag ----

    @Test
    void setCheckFlagCreatesNewFlagAsFalse() {
        FlagInteraction interaction = new FlagInteraction("msg", player, "old");

        interaction.setCheckFlag("new");

        assertTrue(player.flagExists("new"));
        assertFalse(player.getFlag("new"));
    }

    @Test
    void setCheckFlagSwitchesWhichFlagIsChecked() {
        FlagInteraction interaction = new FlagInteraction("msg", "replaced", player, "old");

        interaction.setCheckFlag("other");

        // "old" is still false and "other" was just created as false, so the original message shows
        assertEquals("msg", interaction.getMessage());

        player.addFlag("other", true);

        assertEquals("replaced", interaction.getMessage());
    }

    @Test
    void setCheckFlagKeepsExistingFlagValue() {
        player.addFlag("existing", true);
        FlagInteraction interaction = new FlagInteraction("msg", player, "old");

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

        FlagInteraction interaction = new FlagInteraction("msg", holder, "custom");
        interaction.trigger();

        assertTrue(holder.getFlag("custom"));
    }
}
