package engine.room.parts;

import helpers.GameSettings;
import helpers.Point;
import helpers.QuadShapeDrawer;
import engine.room.parts.RoomComponent.RoomPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoomPointsTest {
    private static final double RATIO = 0.5;
    private static final double PERCENT = 0.5;
    private static final int ANGLE = 30;
    private static final int MULTI = 2;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 1920;
        GameSettings.screenHeight = 1080;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private static void assertSamePoint(Point expected, Point actual) {
        assertEquals(expected.getX(), actual.getX(), "x");
        assertEquals(expected.getY(), actual.getY(), "y");
    }

    private static void assertSameRoom(RoomPoints expected, RoomPoints actual) {
        for (RoomPart part : RoomPart.values()) {
            Point[] e = expected.getPartPoints(part);
            Point[] a = actual.getPartPoints(part);
            for (int i = 0; i < 4; i++) {
                assertSamePoint(e[i], a[i]);
            }
        }
    }

    // ---- back wall ----

    @Test
    void backWallIsCenteredWithExpectedSize() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        // width = 1920 * 0.5 = 960, height = 960 * 0.5 = 480, centered on a 1920x1080 screen
        assertSamePoint(new Point(480, 300), back[0]);
        assertSamePoint(new Point(1440, 300), back[1]);
        assertSamePoint(new Point(1440, 780), back[2]);
        assertSamePoint(new Point(480, 780), back[3]);
    }

    @Test
    void getPartPointsAlwaysReturnsFourPoints() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        for (RoomPart part : RoomPart.values()) {
            Point[] points = room.getPartPoints(part);

            assertEquals(4, points.length);
            for (Point p : points) {
                assertNotNull(p);
            }
        }
    }

    // ---- how the parts share points ----

    @Test
    void ceilingSharesItsBottomEdgeWithTheBackWall() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        Point[] ceiling = room.getPartPoints(RoomPart.CEILING);

        assertSamePoint(back[1], ceiling[2]);
        assertSamePoint(back[0], ceiling[3]);
    }

    @Test
    void floorSharesItsTopEdgeWithTheBackWall() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        Point[] floor = room.getPartPoints(RoomPart.FLOOR);

        assertSamePoint(back[3], floor[0]);
        assertSamePoint(back[2], floor[1]);
    }

    @Test
    void leftWallSharesItsInnerEdgeWithTheBackWall() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        Point[] left = room.getPartPoints(RoomPart.LEFT_WALL);

        assertSamePoint(back[0], left[1]);
        assertSamePoint(back[3], left[2]);
    }

    @Test
    void rightWallSharesItsInnerEdgeWithTheBackWall() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        Point[] right = room.getPartPoints(RoomPart.RIGHT_WALL);

        assertSamePoint(back[1], right[0]);
        assertSamePoint(back[2], right[3]);
    }

    @Test
    void fourthWallCollapsesToTheBackWallTopLeftPoint() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point topLeft = room.getPartPoints(RoomPart.BACK_WALL)[0];

        for (Point p : room.getPartPoints(RoomPart.FOURTH_WALL)) {
            assertSamePoint(topLeft, p);
        }
    }

    // ---- offscreen corners ----

    @Test
    void offscreenCornersFanOutFromTheBackWall() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);
        Point tl = back[0];
        Point tr = back[1];
        Point br = back[2];
        Point bl = back[3];

        Point[] ceiling = room.getPartPoints(RoomPart.CEILING); // {4, 5, 1, 0}
        Point[] floor = room.getPartPoints(RoomPart.FLOOR);     // {3, 2, 6, 7}
        Point p4 = ceiling[0];
        Point p5 = ceiling[1];
        Point p6 = floor[2];
        Point p7 = floor[3];

        // top-left corner goes up and left, top-right goes up and right, etc.
        assertTrue(p4.getX() < tl.getX() && p4.getY() < tl.getY());
        assertTrue(p5.getX() > tr.getX() && p5.getY() < tr.getY());
        assertTrue(p6.getX() > br.getX() && p6.getY() > br.getY());
        assertTrue(p7.getX() < bl.getX() && p7.getY() > bl.getY());
    }

    @Test
    void offscreenCornersAreBackWidthTimesMultiplierAway() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);
        Point[] ceiling = room.getPartPoints(RoomPart.CEILING);

        int dist = QuadShapeDrawer.calcDist(back[0], ceiling[0]);

        assertEquals(960 * MULTI, dist, 2);
    }

    @Test
    void largerMultiplierMovesOffscreenCornersFurtherAway() {
        RoomPoints small = new RoomPoints(RATIO, PERCENT, ANGLE, 1);
        RoomPoints large = new RoomPoints(RATIO, PERCENT, ANGLE, 5);

        int smallDist = QuadShapeDrawer.calcDist(small.getPartPoints(RoomPart.BACK_WALL)[0], small.getPartPoints(RoomPart.CEILING)[0]);
        int largeDist = QuadShapeDrawer.calcDist(large.getPartPoints(RoomPart.BACK_WALL)[0], large.getPartPoints(RoomPart.CEILING)[0]);

        assertTrue(largeDist > smallDist);
    }

    // ---- clamping ----

    @Test
    void wallRatioAboveOneIsClampedToOne() {
        RoomPoints room = new RoomPoints(5.0, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        int width = back[1].getX() - back[0].getX();
        int height = back[2].getY() - back[1].getY();

        assertEquals(width, height);
        assertSameRoom(new RoomPoints(1.0, PERCENT, ANGLE, MULTI), room);
    }

    @Test
    void wallRatioBelowMinimumIsClampedToOneHundredth() {
        RoomPoints low = new RoomPoints(0.0, PERCENT, ANGLE, MULTI);
        RoomPoints negative = new RoomPoints(-3.0, PERCENT, ANGLE, MULTI);

        assertSameRoom(new RoomPoints(0.01, PERCENT, ANGLE, MULTI), low);
        assertSameRoom(new RoomPoints(0.01, PERCENT, ANGLE, MULTI), negative);
    }

    @Test
    void screenPercentAboveOneIsClampedToOne() {
        RoomPoints room = new RoomPoints(RATIO, 5.0, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        assertEquals(1920, back[1].getX() - back[0].getX());
        assertSameRoom(new RoomPoints(RATIO, 1.0, ANGLE, MULTI), room);
    }

    @Test
    void screenPercentBelowMinimumIsClampedToOneHundredth() {
        RoomPoints low = new RoomPoints(RATIO, 0.0, ANGLE, MULTI);
        RoomPoints negative = new RoomPoints(RATIO, -2.0, ANGLE, MULTI);

        assertSameRoom(new RoomPoints(RATIO, 0.01, ANGLE, MULTI), low);
        assertSameRoom(new RoomPoints(RATIO, 0.01, ANGLE, MULTI), negative);
    }

    @Test
    void angleBelowOneIsClampedToOne() {
        assertSameRoom(new RoomPoints(RATIO, PERCENT, 1, MULTI), new RoomPoints(RATIO, PERCENT, 0, MULTI));
        assertSameRoom(new RoomPoints(RATIO, PERCENT, 1, MULTI), new RoomPoints(RATIO, PERCENT, -45, MULTI));
    }

    @Test
    void angleAboveEightyNineIsClampedToEightyNine() {
        assertSameRoom(new RoomPoints(RATIO, PERCENT, 89, MULTI), new RoomPoints(RATIO, PERCENT, 90, MULTI));
        assertSameRoom(new RoomPoints(RATIO, PERCENT, 89, MULTI), new RoomPoints(RATIO, PERCENT, 200, MULTI));
    }

    @Test
    void multiplierBelowOneIsClampedToOne() {
        assertSameRoom(new RoomPoints(RATIO, PERCENT, ANGLE, 1), new RoomPoints(RATIO, PERCENT, ANGLE, 0));
        assertSameRoom(new RoomPoints(RATIO, PERCENT, ANGLE, 1), new RoomPoints(RATIO, PERCENT, ANGLE, -7));
    }

    @Test
    void boundaryValuesAreKept() {
        // values right on the allowed limits should not be changed by clamping
        assertDoesNotThrow(() -> new RoomPoints(1.0, 1.0, 89, 1));
        assertDoesNotThrow(() -> new RoomPoints(0.01, 0.01, 1, 1));
    }

    // ---- changeParms ----

    @Test
    void changeParmsRecalculatesPoints() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        room.changeParms(RATIO, 1.0, ANGLE, MULTI);

        assertSameRoom(new RoomPoints(RATIO, 1.0, ANGLE, MULTI), room);
    }

    @Test
    void changeParmsAlsoClampsValues() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        room.changeParms(9.0, 9.0, 500, -1);

        assertSameRoom(new RoomPoints(1.0, 1.0, 89, 1), room);
    }

    @Test
    void changeParmsPicksUpNewScreenSize() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        GameSettings.screenWidth = 1000;
        GameSettings.screenHeight = 500;
        room.changeParms(RATIO, PERCENT, ANGLE, MULTI);
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        assertEquals(500, back[1].getX() - back[0].getX());
    }

    @Test
    void pointsDoNotChangeUntilChangeParmsIsCalled() {
        RoomPoints room = new RoomPoints(RATIO, PERCENT, ANGLE, MULTI);

        GameSettings.screenWidth = 1000;
        Point[] back = room.getPartPoints(RoomPart.BACK_WALL);

        assertEquals(960, back[1].getX() - back[0].getX());
    }

    @Test
    void screenSizeNotSetGivesZeroSizedBackWall() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;

        RoomPoints room = assertDoesNotThrow(() -> new RoomPoints(RATIO, PERCENT, ANGLE, MULTI));

        for (Point p : room.getPartPoints(RoomPart.BACK_WALL)) {
            assertSamePoint(new Point(0, 0), p);
        }
    }
}
