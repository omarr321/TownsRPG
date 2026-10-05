package engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlagHolder is an interface, so these tests check the contract using a small in-memory
 * implementation and the real Player implementation.
 */
public class FlagHolderTest {

    private static class MapFlagHolder implements FlagHolder {
        private final Map<String, Boolean> flags = new HashMap<>();

        @Override
        public boolean flagExists(String name) {
            return flags.containsKey(name);
        }

        @Override
        public boolean getFlag(String name) {
            return flags.get(name);
        }

        @Override
        public void addFlag(String name, boolean val) {
            flags.put(name, val);
        }
    }

    private void checkContract(FlagHolder holder) {
        assertFalse(holder.flagExists("flag"));

        holder.addFlag("flag", true);
        assertTrue(holder.flagExists("flag"));
        assertTrue(holder.getFlag("flag"));

        holder.addFlag("flag", false);
        assertTrue(holder.flagExists("flag"));
        assertFalse(holder.getFlag("flag"));
    }

    @Test
    void customImplementationFollowsContract() {
        checkContract(new MapFlagHolder());
    }

    @Test
    void playerFollowsContract() {
        checkContract(new Player("Tester"));
    }

    @Test
    void canBeUsedThroughTheInterfaceType() {
        FlagHolder holder = new Player("Tester");

        holder.addFlag("x", true);

        assertTrue(holder.getFlag("x"));
    }
}
