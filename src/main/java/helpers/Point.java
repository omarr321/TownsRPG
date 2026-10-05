package helpers;

/**
 * Represents a 2D point with integer coordinates (x and y).
 */
public class Point {
    private int x;
    private int y;

    /**
     * Constructs a Point with the specified x and y coordinates.
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the x coordinate of this point.
     * @return The x coordinate as an integer.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of this point.
     * @return The y coordinate as an integer.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the x coordinate of this point.
     * @param x The new x coordinate to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y coordinate of this point.
     * @param y The new y coordinate to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the coordinates of this point as an integer array.
     * @return An integer array in the format {@code [x, y]}.
     */
    public int[] getCoords() {
        return new int[]{x, y};
    }
}
