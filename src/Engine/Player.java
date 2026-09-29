package Engine;

import java.util.HashMap;
import java.util.Map;

public class Player implements FlagHolder{
    private final Map<String, Boolean> FLAGS = new HashMap<>();
    private String playerName = "";

    public Player(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void addFlag(String name, boolean val) {
        this.FLAGS.put(name, val);
    }

    public boolean getFlag(String name) {
        return this.FLAGS.get(name);
    }

    public boolean flagExists(String name) {
        return this.FLAGS.get(name) != null;
    }
}
