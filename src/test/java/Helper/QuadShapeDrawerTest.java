package Helper;

import GUI.CustomPanels.QuadrilateralPanel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuadShapeDrawerTest {
    private static final Point VAILD_POINT = new Point(0,0);
    private static final Point NULL_POINT = null;

    @Test
    void constructor() {
        GameSettings.screenWidth = 0;
        QuadShapeDrawer drawer = new QuadShapeDrawer(VAILD_POINT);

        assertEquals(VAILD_POINT, drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_LEFT));

        drawer = new QuadShapeDrawer(NULL_POINT);
        assertNull(drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_LEFT));
    }

    @Test
    void calcPoint() {
        Point expectedPoint = new Point(50, 0);
        Point point = QuadShapeDrawer.calcPoint(VAILD_POINT, 0, 50);
        assertEquals(expectedPoint.getX(), point.getX());
        assertEquals(expectedPoint.getY(), point.getY());

        expectedPoint = new Point(0, 50);
        point = QuadShapeDrawer.calcPoint(VAILD_POINT, 90, 50);
        assertEquals(expectedPoint.getX(), point.getX());
        assertEquals(expectedPoint.getY(), point.getY());

        expectedPoint = new Point(50, 50);
        point = QuadShapeDrawer.calcPoint(VAILD_POINT, 45, 70.71f);
        assertEquals(expectedPoint.getX(), point.getX());
        assertEquals(expectedPoint.getY(), point.getY());
    }

    @Test
    void calcPointNull() {
        Point expectedPoint = new Point(0, 0);
        Point point = QuadShapeDrawer.calcPoint(NULL_POINT, 0, 50);

        assertEquals(expectedPoint.getX(), point.getX());
        assertEquals(expectedPoint.getY(), point.getY());
    }

    @Test
    void calcDist() {
        Point p1 = VAILD_POINT;
        Point p2 = new Point(0, 100);
        assertEquals(100, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(100, QuadShapeDrawer.calcDist(p2, p1));

        p2 = new Point(100, 0);
        assertEquals(100, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(100, QuadShapeDrawer.calcDist(p2, p1));

        p2 = new Point(50, 50);
        assertEquals(70, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(70, QuadShapeDrawer.calcDist(p2, p1));
    }

    @Test
    void calcDistNull() {
        Point p1 = VAILD_POINT;
        Point p2 = NULL_POINT;
        assertEquals(0, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(0, QuadShapeDrawer.calcDist(p2, p1));

        p1 = NULL_POINT;
        p2 = new Point(50, 50);
        assertEquals(70, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(70, QuadShapeDrawer.calcDist(p2, p1));

        p2 = NULL_POINT;
        assertEquals(0, QuadShapeDrawer.calcDist(p1, p2));
        assertEquals(0, QuadShapeDrawer.calcDist(p2, p1));
    }

    @Test
    void getPoints() {
        QuadShapeDrawer drawer = new QuadShapeDrawer(new Point(0, 0));
        assertNull(drawer.getPoints());

        drawer.drawLine(0, 50);
        assertNull(drawer.getPoints());

        drawer.drawLine(90, 50);
        assertNull(drawer.getPoints());

        drawer.drawLine(180, 50);

        Point expectedP1 = new Point(0, 0);
        Point expectedP2 = new Point(50, 0);
        Point expectedP3 = new Point(50, 50);
        Point expectedP4 = new Point(0, 50);

        Point[] points = drawer.getPoints();
        assertEquals(expectedP1.getX(), points[0].getX());
        assertEquals(expectedP1.getY(), points[0].getY());
        assertEquals(expectedP2.getX(), points[1].getX());
        assertEquals(expectedP2.getY(), points[1].getY());
        assertEquals(expectedP3.getX(), points[2].getX());
        assertEquals(expectedP3.getY(), points[2].getY());
        assertEquals(expectedP4.getX(), points[3].getX());
        assertEquals(expectedP4.getY(), points[3].getY());
    }

    @Test
    void getPoint() {
        QuadShapeDrawer drawer = new QuadShapeDrawer(VAILD_POINT);
        drawer.drawLine(0, 50);
        drawer.drawLine(90, 50);
        drawer.drawLine(180, 50);

        Point expectedP1 = new Point(0, 0);
        Point expectedP2 = new Point(50, 0);
        Point expectedP3 = new Point(50, 50);
        Point expectedP4 = new Point(0, 50);

        Point P1 = drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_LEFT);
        Point P2 = drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_RIGHT);
        Point P3 = drawer.getPoint(QuadrilateralPanel.PointLocation.BOTTOM_RIGHT);
        Point P4 = drawer.getPoint(QuadrilateralPanel.PointLocation.BOTTOM_LEFT);

        assertEquals(expectedP1.getX(), P1.getX());
        assertEquals(expectedP1.getY(), P1.getY());
        assertEquals(expectedP2.getX(), P2.getX());
        assertEquals(expectedP2.getY(), P2.getY());
        assertEquals(expectedP3.getX(), P3.getX());
        assertEquals(expectedP3.getY(), P3.getY());
        assertEquals(expectedP4.getX(), P4.getX());
        assertEquals(expectedP4.getY(), P4.getY());
    }

    @Test
    void drawLine() {
        QuadShapeDrawer drawer = new QuadShapeDrawer(VAILD_POINT);
        drawer.drawLine(0, 50);
        drawer.drawLine(90, 50);
        drawer.drawLine(180, 50);

        Point expectedP1 = new Point(0, 0);
        Point expectedP2 = new Point(50, 0);
        Point expectedP3 = new Point(50, 50);
        Point expectedP4 = new Point(0, 50);

        Point P1 = drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_LEFT);
        Point P2 = drawer.getPoint(QuadrilateralPanel.PointLocation.TOP_RIGHT);
        Point P3 = drawer.getPoint(QuadrilateralPanel.PointLocation.BOTTOM_RIGHT);
        Point P4 = drawer.getPoint(QuadrilateralPanel.PointLocation.BOTTOM_LEFT);

        assertEquals(expectedP1.getX(), P1.getX());
        assertEquals(expectedP1.getY(), P1.getY());
        assertEquals(expectedP2.getX(), P2.getX());
        assertEquals(expectedP2.getY(), P2.getY());
        assertEquals(expectedP3.getX(), P3.getX());
        assertEquals(expectedP3.getY(), P3.getY());
        assertEquals(expectedP4.getX(), P4.getX());
        assertEquals(expectedP4.getY(), P4.getY());

        drawer.drawLine(100, 10);
        assertEquals(expectedP1.getX(), P1.getX());
        assertEquals(expectedP1.getY(), P1.getY());
        assertEquals(expectedP2.getX(), P2.getX());
        assertEquals(expectedP2.getY(), P2.getY());
        assertEquals(expectedP3.getX(), P3.getX());
        assertEquals(expectedP3.getY(), P3.getY());
        assertEquals(expectedP4.getX(), P4.getX());
        assertEquals(expectedP4.getY(), P4.getY());
    }
}
