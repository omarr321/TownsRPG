package RoomClasses.RoomParts;

import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.GameSettings;
import Helper.Point;
import RoomClasses.RoomObjects.RoomObj;
import RoomClasses.RoomParts.RoomComponent.RoomPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class FloorTest {
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
        err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private LightBlocker blocker() {
        return new LightBlocker(0, 0, 5, 5, LightBlocker.LightTag.BLOCK);
    }

    private LightPoint lightPoint() {
        return new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE);
    }

    private void assertReportedToStdErr() {
        assertTrue(err.toString().toLowerCase().contains("floor"), "Expected a message about the floor on System.err");
    }

    // ---- constructors ----

    @Test
    void isARoomComponent() {
        assertInstanceOf(RoomComponent.class, new Floor(Color.RED));
    }

    @Test
    void imageConstructor() {
        Floor part = new Floor("/images/Crate.png");

        assertEquals(RoomObj.DrawType.IMAGE, part.getType());
        assertEquals("/images/Crate.png", part.getImagePath());
        assertNull(part.getColor());
        assertTrue(part.getWarped());
        assertNull(part.getRoomPart());
    }

    @Test
    void imageConstructorWithWarpFalse() {
        Floor part = new Floor("/images/Crate.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, part.getType());
        assertFalse(part.getWarped());
    }

    @Test
    void imageConstructorWithWarpTrue() {
        Floor part = new Floor("/images/Crate.png", true);

        assertTrue(part.getWarped());
    }

    @Test
    void colorConstructor() {
        Floor part = new Floor(Color.ORANGE);

        assertEquals(RoomObj.DrawType.SOLID, part.getType());
        assertEquals(Color.ORANGE, part.getColor());
        assertEquals("", part.getImagePath());
        assertTrue(part.getWarped());
        assertNull(part.getRoomPart());
    }

    // ---- setters ----

    @Test
    void setColorChangesColor() {
        Floor part = new Floor(Color.RED);

        part.setColor(Color.BLUE);

        assertEquals(Color.BLUE, part.getColor());
    }

    @Test
    void setImagePathChangesPath() {
        Floor part = new Floor("/images/Crate.png");

        part.setImagePath("/images/BrickWall.png");

        assertEquals("/images/BrickWall.png", part.getImagePath());
    }

    @Test
    void setRoomPartChangesRoomPart() {
        Floor part = new Floor(Color.RED);

        for (RoomPart roomPart : RoomPart.values()) {
            part.setRoomPart(roomPart);
            assertEquals(roomPart, part.getRoomPart());
        }
    }

    // ---- light blockers are not supported ----

    @Test
    void convertLightBlockersIsEmptyAndReports() {
        Floor part = new Floor(Color.RED);

        assertEquals(0, part.convertLightBlockers().length);
        assertReportedToStdErr();
    }

    @Test
    void addLightBlockerIsIgnoredAndReports() {
        Floor part = new Floor(Color.RED);

        part.addLightBlocker("b", blocker());

        assertReportedToStdErr();
        assertEquals(0, part.convertLightBlockers().length);
        assertNull(part.getLightBlocker("b"));
    }

    @Test
    void removeLightBlockerReturnsFalseAndReports() {
        Floor part = new Floor(Color.RED);

        assertFalse(part.removeLightBlocker("b"));
        assertReportedToStdErr();
    }

    @Test
    void getLightBlockerReturnsNullAndReports() {
        Floor part = new Floor(Color.RED);

        assertNull(part.getLightBlocker("b"));
        assertReportedToStdErr();
    }

    // ---- light points are not supported ----

    @Test
    void convertLightPointsIsEmptyAndReports() {
        Floor part = new Floor(Color.RED);

        assertEquals(0, part.convertLightPoints().length);
        assertReportedToStdErr();
    }

    @Test
    void addLightPointIsIgnoredAndReports() {
        Floor part = new Floor(Color.RED);

        part.addLightPoint("p", lightPoint());

        assertReportedToStdErr();
        assertEquals(0, part.convertLightPoints().length);
        assertNull(part.getLightPoint("p"));
    }

    @Test
    void removeLightPointReturnsFalseAndReports() {
        Floor part = new Floor(Color.RED);

        assertFalse(part.removeLightPoint("p"));
        assertReportedToStdErr();
    }

    @Test
    void getLightPointReturnsNullAndReports() {
        Floor part = new Floor(Color.RED);

        assertNull(part.getLightPoint("p"));
        assertReportedToStdErr();
    }

    // ---- drawing ----

    @Test
    void putToScreenReturnsEmptyArray() {
        Floor part = new Floor(Color.RED);

        assertNotNull(part.putToScreen());
        assertEquals(0, part.putToScreen().length);
    }

    @Test
    void getShapeCornersReturnsEmptyArray() {
        Floor part = new Floor(Color.RED);

        assertNotNull(part.getShapeCorners());
        assertEquals(0, part.getShapeCorners().length);
    }
}
