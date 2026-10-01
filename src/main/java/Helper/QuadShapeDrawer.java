package Helper;

import GUI.CustomPanels.QuadrilateralPanel.PointLocation;

/**
 * This class helps makes the 4 points of a shape by letting you draw lines.
 */
public class QuadShapeDrawer {
    final private Point[] points = new Point[4];
    private boolean completed = false;
    private int currP = 1;

    /**
     * Constructs the drawer with a starting point.
     * @param startP The point to start at.
     */
    public  QuadShapeDrawer(Point startP) {
        this.points[0] = startP;
    }

    /**
     * Calculates a point from a origin point, distance, and angle.
     * @param point The origin point to start from.
     * @param angle The angle to draw the line from. 0 is horizontal and facing right. 90 is pointed directly down.
     * @param dist The distance to draw the line.
     * @return Returns the end point of the line drawn from the origin point and angle.
     */
    public static Point calcPoint(Point point, float angle, float dist){
        if(point == null) {
            return new Point(0, 0);
        }

        double x0 = point.getX();
        double y0 = point.getY();

        double angleRadians = Math.toRadians(angle);

        int x1 = (int) Math.round(x0 + (dist * Math.cos(angleRadians)));
        int y1 = (int) Math.round(y0 + (dist * Math.sin(angleRadians)));

        return new Point(x1, y1);
    }

    /**
     * Calcualte the distance between two points as an int.
     * @param p1 The starting point.
     * @param p2 The ending point.
     * @return The distance between the two points as an int. It drops anything after the decimal.
     */
    public static int calcDist(Point p1, Point p2) {
        if (p1 == null) {
            p1 = new Point(0, 0);
        }
        if (p2 == null) {
            p2 = new Point(0, 0);
        }

        double pt1 = Math.pow(p2.getX() - p1.getX(), 2);
        double pt2 = Math.pow(p2.getY() - p1.getY(), 2);
        return (int) Math.sqrt(pt1+pt2);
    }

    /**
     * Once all points are drawn, you can get the point array of the 4 corner points of the shape to draw to the screen.
     * @return An array of the corner points of the shape.
     */
    public Point[] getPoints() {
        if (!completed) {
            System.err.println("QuadShapeDrawer does not have a complete shape!");
            return null;
        }
        Point[] temp = new Point[4];
        for(int i = 0; i < temp.length; i++) {
            Point workingP = this.points[i];
            temp[i] = new Point(GameSettings.scale(workingP.getX()), GameSettings.scale(workingP.getY()));
        }
        return temp;
    }

    /**
     * Gets the point at the location provided.
     * @param loc - The location of the point.
     * @return The point at that location.
     */
    public Point getPoint(PointLocation loc) {
        return this.points[loc.getPointToNum()];
    }

    /**
     * This will create the next line in the point and map the point it makes it to uses a horzontal as 0 degrees and goes clockwise.
     * @param angle The angle in degrees to draw from, horizontal is 0.
     * @param length The length to draw in pixels.
     */
    public void drawLine (float angle, float length) {
        if (!completed) {
            this.points[currP] = calcPoint(this.points[currP-1], angle, length);
            currP++;

            if (currP >= 4) {
                this.completed = true;
            }
        }
    }
}
