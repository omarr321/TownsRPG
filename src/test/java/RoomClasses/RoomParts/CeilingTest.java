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

public class CeilingTest {
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
        assertTrue(err.toString().toLowerCase().contains("ceiling"), "Expected a message about the ceiling on System.err");
    }

    // ---- constructors ----

    @Test
    void isARoomComponent() {
        assertInstanceOf(RoomComponent.class, new Ceiling(Color.RED));
    }

    @Test
    void imageConstructor() {
        Ceiling part = new Ceiling("/images/Crate.png");

        assertEquals(RoomObj.DrawType.IMAGE, part.getType());
        assertEquals("/images/Crate.png", part.getImagePath());
        assertNull(part.getColor());
        assertTrue(part.getWarped());
        assertNull(part.getRoomPart());
    }

    @Test
    void imageConstructorWithWarpFalse() {
        Ceiling part = new Ceiling("/images/Crate.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, part.getType());
        assertFalse(part.getWarped());
    }

    @Test
    void imageConstructorWithWarpTrue() {
        Ceiling part = new Ceiling("/images/Crate.png", true);

        assertTrue(part.getWarped());
    }

    @Test
    void colorConstructor() {
        Ceiling part = new Ceiling(Color.ORANGE);

        assertEquals(RoomObj.DrawType.SOLID, part.getType());
        assertEquals(Color.ORANGE, part.getColor());
        assertEquals("", part.getImagePath());
        assertTrue(part.getWarped());
        assertNull(part.getRoomPart());
    }

    // ---- setters ----

    @Test
    void setColorChangesColor() {
        Ceiling part = new Ceiling(Color.RED);

        part.setColor(Color.BLUE);

        assertEquals(Color.BLUE, part.getColor());
    }

    @Test
    void setImagePathChangesPath() {
        Ceiling part = new Ceiling("/images/Crate.png");

        part.setImagePath("/images/BrickWall.png");

        assertEquals("/images/BrickWall.png", part.getImagePath());
    }

    @Test
    void setRoomPartChangesRoomPart() {
        Ceiling part = new Ceiling(Color.RED);

        for (RoomPart roomPart : RoomPart.values()) {
            part.setRoomPart(roomPart);
            assertEquals(roomPart, part.getRoomPart());
        }
    }

    // ---- light blockers are not supported ----

    @Test
    void convertLightBlockersIsEmptyAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertEquals(0, part.convertLightBlockers().length);
        assertReportedToStdErr();
    }

    @Test
    void addLightBlockerIsIgnoredAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        part.addLightBlocker("b", blocker());

        assertReportedToStdErr();
        assertEquals(0, part.convertLightBlockers().length);
        assertNull(part.getLightBlocker("b"));
    }

    @Test
    void removeLightBlockerReturnsFalseAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertFalse(part.removeLightBlocker("b"));
        assertReportedToStdErr();
    }

    @Test
    void getLightBlockerReturnsNullAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertNull(part.getLightBlocker("b"));
        assertReportedToStdErr();
    }

    // ---- light points are not supported ----

    @Test
    void convertLightPointsIsEmptyAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertEquals(0, part.convertLightPoints().length);
        assertReportedToStdErr();
    }

    @Test
    void addLightPointIsIgnoredAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        part.addLightPoint("p", lightPoint());

        assertReportedToStdErr();
        assertEquals(0, part.convertLightPoints().length);
        assertNull(part.getLightPoint("p"));
    }

    @Test
    void removeLightPointReturnsFalseAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertFalse(part.removeLightPoint("p"));
        assertReportedToStdErr();
    }

    @Test
    void getLightPointReturnsNullAndReports() {
        Ceiling part = new Ceiling(Color.RED);

        assertNull(part.getLightPoint("p"));
        assertReportedToStdErr();
    }

    // ---- drawing ----

    @Test
    void putToScreenReturnsEmptyArray() {
        Ceiling part = new Ceiling(Color.RED);

        assertNotNull(part.putToScreen());
        assertEquals(0, part.putToScreen().length);
    }

    @Test
    void getShapeCornersReturnsEmptyArray() {
        Ceiling part = new Ceiling(Color.RED);

        assertNotNull(part.getShapeCorners());
        assertEquals(0, part.getShapeCorners().length);
    }
}
