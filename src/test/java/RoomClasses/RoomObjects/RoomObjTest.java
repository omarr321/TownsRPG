package RoomClasses.RoomObjects;

import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.GameSettings;
import Helper.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoomObj is abstract, so its constructors and protected defaults are tested through BasicObj
 * (the simplest concrete subclass). This test class lives in the same package so it can read the protected fields.
 */
public class RoomObjTest {
    private static final Point[] CORNERS = {new Point(0, 0), new Point(10, 0), new Point(10, 10), new Point(0, 10)};

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @Test
    void drawTypeHasExpectedValues() {
        assertEquals(3, RoomObj.DrawType.values().length);
        assertNotNull(RoomObj.DrawType.valueOf("SOLID"));
        assertNotNull(RoomObj.DrawType.valueOf("IMAGE"));
        assertNotNull(RoomObj.DrawType.valueOf("DEFAULT"));
    }

    @Test
    void colorConstructorSetsSolidDefaults() {
        RoomObj obj = new BasicObj(CORNERS, Color.RED);

        assertEquals(RoomObj.DrawType.SOLID, obj.type);
        assertEquals(Color.RED, obj.color);
        assertEquals("", obj.imagePath);
        assertTrue(obj.warped);
        assertSame(CORNERS, obj.getShapeCorners());
    }

    @Test
    void imageConstructorSetsImageDefaults() {
        RoomObj obj = new BasicObj(CORNERS, "/images/Crate.png");

        assertEquals(RoomObj.DrawType.IMAGE, obj.type);
        assertNull(obj.color);
        assertEquals("/images/Crate.png", obj.imagePath);
        assertTrue(obj.warped);
    }

    @Test
    void warpedConstructorCanTurnWarpOff() {
        RoomObj obj = new BasicObj(CORNERS, "/images/Crate.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, obj.type);
        assertFalse(obj.warped);
    }

    @Test
    void warpedConstructorCanKeepWarpOn() {
        RoomObj obj = new BasicObj(CORNERS, "/images/Crate.png", true);

        assertTrue(obj.warped);
    }

    @Test
    void lightMapsStartEmpty() {
        RoomObj obj = new BasicObj(CORNERS, Color.RED);

        assertTrue(obj.lightBlockers.isEmpty());
        assertTrue(obj.lightPoints.isEmpty());
    }

    @Test
    void lightMapsAreNotSharedBetweenObjects() {
        RoomObj one = new BasicObj(CORNERS, Color.RED);
        RoomObj two = new BasicObj(CORNERS, Color.BLUE);

        one.addLightBlocker("b", new LightBlocker(0, 0, 5, 5, LightBlocker.LightTag.BLOCK));
        one.addLightPoint("p", new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE));

        assertNull(two.getLightBlocker("b"));
        assertNull(two.getLightPoint("p"));
    }

    @Test
    void nullCornersAreAccepted() {
        RoomObj obj = new BasicObj(null, Color.RED);

        assertNull(obj.getShapeCorners());
    }
}
