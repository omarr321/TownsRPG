package engine;

/**
 * Represents a container that holds and manages boolean flags referenced by string names.
 */
public interface FlagHolder {
    /**
     * Checks whether a flag with the specified name exists.
     * @param name The name of the flag to check.
     * @return True if the flag exists, false otherwise.
     */
    boolean flagExists(String name);

    /**
     * Retrieves the boolean value of the flag with the specified name.
     * @param name The name of the flag to retrieve.
     * @return The boolean value of the flag.
     */
    boolean getFlag(String name);

    /**
     * Adds or updates a flag with the specified name and value.
     * @param name The name of the flag.
     * @param val The boolean value to assign to the flag.
     */
    void addFlag(String name, boolean val);
}
