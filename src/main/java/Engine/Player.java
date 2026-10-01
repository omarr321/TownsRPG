package Engine;

import java.util.HashMap;
import java.util.Map;

/**
 * The player of the game. Holds player info like Flags.
 */
public class Player implements FlagHolder{
    private final Map<String, Boolean> FLAGS = new HashMap<>();
    private String playerName = "";

    /**
     * Constructs the player with a name.
     * @param name The name of the player.
     */
    public Player(String name) {
        this.playerName = name;
    }

    /**
     * Gets the player name.
     * @return The player name.
     */
    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * Adds a flag to the player in a key value map.
     * @param name The name to put the flag under.
     * @param val The value to store.
     */
    public void addFlag(String name, boolean val) {
        this.FLAGS.put(name, val);
    }

    /**
     * Gets a flag from the player.
     * @param name The name to get the value from.
     * @return The value of the flag.
     */
    public boolean getFlag(String name) {
        return this.FLAGS.get(name);
    }

    /**
     * Checks to see if a flag exists.
     * !THIS DOES NOT RETURN THE VALUE OF THE FLAG!
     * @param name The name to check.
     * @return True if the flag does exist, false if otherwise.
     */
    public boolean flagExists(String name) {
        return this.FLAGS.get(name) != null;
    }
}
