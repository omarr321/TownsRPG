package Helper;

import GUI.CustomPanels.QuadrilateralPanel.PointLocation;

/**
 * This class helps make a 4 point shape by you giving the draw angle and length to get the length.
 */
public class QuadShapeDrawer {
    private Point[] points = new Point[4];
    private Point startingP = new Point(0,0);
    private boolean completed = false;
    private int currP = 1;

    public  QuadShapeDrawer(Point startP) {
        this.startingP = startP;
        points[0] = startingP;
    }

    public static Point calcPoint(Point point, float angle, float dist){
        double x0 = point.getX();
        double y0 = point.getY();

        double angleRadians = Math.toRadians(angle);

        int x1 = (int) Math.round(x0 + (dist * Math.cos(angleRadians)));
        int y1 = (int) Math.round(y0 + (dist * Math.sin(angleRadians)));

        return new Point(x1, y1);
    }

    public static int calcDist(Point p1, Point p2) {
        double pt1 = Math.pow(p2.getX() - p1.getX(), 2);
        double pt2 = Math.pow(p2.getY() - p1.getY(), 2);
        return (int) Math.sqrt(pt1+pt2);
    }

    /**
     * Once all points are drown, you get the point array which this method returns.
     * @return The completed point array.
     */
    public Point[] getPoints() {
        if (!completed) {
            System.err.println("QuadShapeDrawer does not have a complete shape!");
            return null;
        }
        return points;
    }

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
            this.points[currP] = calcPoint(this.points[currP-1], angle, length);;
            currP++;

            if (currP >= 4) {
                this.completed = true;
            }
        }
    }
}
