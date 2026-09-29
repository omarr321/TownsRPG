package Engine;

public interface FlagHolder {
    boolean flagExists(String name);
    boolean getFlag(String name);
    void addFlag(String name, boolean val);
}
