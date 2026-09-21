package GraphicClasses.CustomPanels;

import GraphicClasses.Point;

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

    /**
     * Once all points are drown, you get the point array which this method returns.
     * @return The completed point array.
     */
    public Point[] getPoints() {
        if (!completed) {
            System.err.println("QuadShapeDrawer does not have a complete shape!");
            System.exit(1);
        }
        return points;
    }

    /**
     * This will create the next line in the point and map the point it makes it to uses a horzontal as 0 degrees and goes clockwise.
     * @param angle The angle in degrees to draw from, horizontal is 0.
     * @param length The length to draw in pixels.
     */
    public void drawLine (float angle, float length) {
        if (!completed) {
            double x0 = this.points[currP - 1].getX();
            double y0 = this.points[currP - 1].getY();

            double dist = length;
            double angleRadians = Math.toRadians(angle);

            int x1 = (int) Math.round(x0 + (dist * Math.cos(angleRadians)));
            int y1 = (int) Math.round(y0 + (dist * Math.sin(angleRadians)));

            this.points[currP] = new Point(x1, y1);
            currP++;

            if (currP >= 4) {
                this.completed = true;
            }
        }
    }
}
