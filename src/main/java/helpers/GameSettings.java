package helpers;

/**
 * Static setting of the game that is shared across the engine.
 */
public class GameSettings {
    /** Whether the game runs in full-screen mode instead of a window. */
    public static boolean fullScreen = false;
    /** The width of the screen or game window in pixels. */
    public static int screenWidth = 0;
    /** The height of the screen or game window in pixels. */
    public static int screenHeight = 0;

    /**
     * The screen width that pixel sizes (like light distances) are written for.
     * Set this to the resolution you tuned your lights at; anything given in pixels
     * is scaled from this width to the real screen width.
     */
    public static final int REFERENCE_WIDTH = 1920;

    /**
     * How much bigger (or smaller) the current screen is than the reference screen.
     * Based on width, the same way RoomPoints sizes the room, so lights and rooms scale together.
     * @return 1 if the screen size hasn't been set yet.
     */
    public static float getScale() {
        if (screenWidth <= 0) {
            return 1f;
        }
        return screenWidth / (float) REFERENCE_WIDTH;
    }

    /**
     * Converts a size in reference-screen pixels to real screen pixels.
     * Values of 0 or less are returned unchanged, and positive values never scale below 1.
     *
     * @param value the size in reference-screen pixels
     * @return the size in real screen pixels, or {@code value} itself if it is 0 or less
     */
    public static int scale(int value) {
        if (value <= 0) {
            return value;
        }
        return Math.max(1, Math.round(value * getScale()));
    }

    /**
     * Converts a size in real screen pixels to reference-screen pixels.
     * Values of 0 or less are returned unchanged, and positive values never scale below 1.
     *
     * @param value the size in real screen pixels
     * @return the size in reference-screen pixels, or {@code value} itself if it is 0 or less
     */
    public static int descale(int value) {
        if (value <= 0) {
            return value;
        }
        float scale = getScale();
        return Math.max(1, Math.round(value / scale));
    }

    //For Unit Testing
    // Private constructor to hide the implicit public one and prevent instantiation
    private GameSettings() {
        throw new UnsupportedOperationException("GameSettings.java Can not be instantiation.");
    }
}
