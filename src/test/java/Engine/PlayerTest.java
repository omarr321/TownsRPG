package Engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    @Test
    void constructorStoresName() {
        Player player = new Player("Alice");

        assertEquals("Alice", player.getPlayerName());
    }

    @Test
    void constructorAllowsNullName() {
        Player player = new Player(null);

        assertNull(player.getPlayerName());
    }

    @Test
    void flagDoesNotExistByDefault() {
        Player player = new Player("Alice");

        assertFalse(player.flagExists("anything"));
    }

    @Test
    void addFlagTrue() {
        Player player = new Player("Alice");

        player.addFlag("door", true);

        assertTrue(player.flagExists("door"));
        assertTrue(player.getFlag("door"));
    }

    @Test
    void addFlagFalseStillExists() {
        Player player = new Player("Alice");

        player.addFlag("door", false);

        assertTrue(player.flagExists("door"));
        assertFalse(player.getFlag("door"));
    }

    @Test
    void addFlagOverwritesExistingValue() {
        Player player = new Player("Alice");

        player.addFlag("door", false);
        player.addFlag("door", true);

        assertTrue(player.getFlag("door"));

        player.addFlag("door", false);

        assertFalse(player.getFlag("door"));
    }

    @Test
    void multipleFlagsAreIndependent() {
        Player player = new Player("Alice");

        player.addFlag("a", true);
        player.addFlag("b", false);

        assertTrue(player.getFlag("a"));
        assertFalse(player.getFlag("b"));
        assertFalse(player.flagExists("c"));
    }

    @Test
    void flagsAreNotSharedBetweenPlayers() {
        Player one = new Player("One");
        Player two = new Player("Two");

        one.addFlag("shared", true);

        assertTrue(one.flagExists("shared"));
        assertFalse(two.flagExists("shared"));
    }

    @Test
    void getFlagOnMissingFlagThrows() {
        Player player = new Player("Alice");

        assertThrows(NullPointerException.class, () -> player.getFlag("missing"));
    }

    @Test
    void flagNamesAreCaseSensitive() {
        Player player = new Player("Alice");

        player.addFlag("Door", true);

        assertTrue(player.flagExists("Door"));
        assertFalse(player.flagExists("door"));
    }

    @Test
    void playerIsAFlagHolder() {
        assertInstanceOf(FlagHolder.class, new Player("Alice"));
    }
}
