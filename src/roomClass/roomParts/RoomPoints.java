package roomClass.roomParts;

import Helper.GameSettings;
import Helper.Point;
import Helper.QuadShapeDrawer;
import roomClass.roomParts.RoomComponent.RoomPart;

public class RoomPoints {
    private double WALL_RATIO;
    private double SC_PERCENT;
    private int SC_ANGLE;
    private int OFFSCREEN_MULI;
    private final Point[] roomPoints = new Point[8];

    private int backWidth;
    private int backHeight;
    private int backStartingPointX;
    private int backStartingPointy;

    /**
     * A constructor for the RoomPoint Class.
     * @param WALL_RATIO The ratio if the width in relation to the height.
     * @param SC_PERCENT A number between .01 and 1 inclusive. Indicates the percent of width the back wall will take up.
     * @param SC_ANGLE A number between 1 and 89 inclusive. Indicates the angle at which to draw the walls, 0 is horizontal.
     * @param OFFSCREEN_MULI A number greater than 0. Indicates how much to stretch the walls.
     */
    public RoomPoints(double WALL_RATIO, double SC_PERCENT, int SC_ANGLE, int OFFSCREEN_MULI){
        this.changeParms(WALL_RATIO, SC_PERCENT, SC_ANGLE, OFFSCREEN_MULI);
    }

    /**
     * This method sets the perms for the room view point.
     * @param WALL_RATIO A number between .01 and 1 inclusive. The ratio if the width in relation to the height.
     * @param SC_PERCENT A number between .01 and 1 inclusive. Indicates the percent of width the back wall will take up.
     * @param SC_ANGLE A number between 1 and 89 inclusive. Indicates the angle at which to draw the walls, 0 is horizontal.
     * @param OFFSCREEN_MULI A number greater than 0. Indicates how much to stretch the walls.
     */
    public void changeParms(double WALL_RATIO, double SC_PERCENT, int SC_ANGLE, int OFFSCREEN_MULI) {
        if (WALL_RATIO > 1.0) {
            this.WALL_RATIO = 1.0;
        } else if (WALL_RATIO < 0.01) {
            this.WALL_RATIO = 0.01;
        } else {
            this.WALL_RATIO = WALL_RATIO;
        }

        if (SC_PERCENT > 1.0) {
            this.SC_PERCENT = 1.0;
        } else if (SC_PERCENT < 0.01) {
            this.SC_PERCENT = 0.01;
        } else {
            this.SC_PERCENT = SC_PERCENT;
        }

        if (SC_ANGLE < 1) {
            this.SC_ANGLE = 1;
        } else if (SC_ANGLE > 89) {
            this.SC_ANGLE = 89;
        } else {
            this.SC_ANGLE = SC_ANGLE;
        }

        this.OFFSCREEN_MULI = Math.max(OFFSCREEN_MULI, 1);

        this.calcPoints();
    }

    /**
     * Calcs the roomPoints using the values provided.
     */
    private void calcPoints() {
        this.backWidth = Math.toIntExact(Math.round(GameSettings.screenWidth * this.SC_PERCENT));
        this.backHeight = Math.toIntExact(Math.round(this.backWidth * this.WALL_RATIO));
        this.backStartingPointX = (GameSettings.screenWidth/2)-(backWidth/2);
        this.backStartingPointy = (GameSettings.screenHeight/2)-(backHeight/2);

        this.roomPoints[0] = new Point(this.backStartingPointX, this.backStartingPointy);
        this.roomPoints[1] = QuadShapeDrawer.calcPoint(this.roomPoints[0], 0, this.backWidth);
        this.roomPoints[2] = QuadShapeDrawer.calcPoint(this.roomPoints[1], 90, this.backHeight);
        this.roomPoints[3] = QuadShapeDrawer.calcPoint(this.roomPoints[2], 180, backWidth);
        this.roomPoints[4] = QuadShapeDrawer.calcPoint(this.roomPoints[0], 180 + this.SC_ANGLE, this.backWidth * this.OFFSCREEN_MULI);
        this.roomPoints[5] = QuadShapeDrawer.calcPoint(this.roomPoints[1], 360 - this.SC_ANGLE, this.backWidth * this.OFFSCREEN_MULI);
        this.roomPoints[6] = QuadShapeDrawer.calcPoint(this.roomPoints[2], this.SC_ANGLE, this.backWidth * this.OFFSCREEN_MULI);
        this.roomPoints[7] = QuadShapeDrawer.calcPoint(this.roomPoints[3], 180 - this.SC_ANGLE, this.backWidth * this.OFFSCREEN_MULI);
    }

    public Point[] getPartPoints(RoomPart roomPart) {
        int[] pointLoc = roomPart.getPartPoints();
        return new Point[]{this.roomPoints[pointLoc[0]], this.roomPoints[pointLoc[1]], this.roomPoints[pointLoc[2]], this.roomPoints[pointLoc[3]]};
    }
}
