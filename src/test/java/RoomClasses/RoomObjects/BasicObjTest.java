package RoomClasses.RoomObjects;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.GameSettings;
import Helper.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BasicObjTest {
    private Point[] corners;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
        corners = new Point[]{new Point(10, 10), new Point(60, 10), new Point(60, 40), new Point(10, 40)};
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private LightBlocker blocker() {
        return new LightBlocker(0, 0, 5, 5, LightBlocker.LightTag.BLOCK);
    }

    private LightPoint lightPoint() {
        return new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE);
    }

    // ---- constructors ----

    @Test
    void colorConstructor() {
        BasicObj obj = new BasicObj(corners, Color.GREEN);

        assertEquals(RoomObj.DrawType.SOLID, obj.getType());
        assertEquals(Color.GREEN, obj.getColor());
        assertEquals("", obj.getImagePath());
        assertTrue(obj.getWarped());
        assertSame(corners, obj.getShapeCorners());
    }

    @Test
    void imageConstructor() {
        BasicObj obj = new BasicObj(corners, "/images/Crate.png");

        assertEquals(RoomObj.DrawType.IMAGE, obj.getType());
        assertNull(obj.getColor());
        assertEquals("/images/Crate.png", obj.getImagePath());
        assertTrue(obj.getWarped());
        assertSame(corners, obj.getShapeCorners());
    }

    @Test
    void imageConstructorWithWarpFalse() {
        BasicObj obj = new BasicObj(corners, "/images/Crate.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, obj.getType());
        assertFalse(obj.getWarped());
    }

    @Test
    void imageConstructorWithWarpTrue() {
        BasicObj obj = new BasicObj(corners, "/images/Crate.png", true);

        assertTrue(obj.getWarped());
    }

    // ---- setters ----

    @Test
    void setColorChangesColor() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        obj.setColor(Color.BLUE);

        assertEquals(Color.BLUE, obj.getColor());
    }

    @Test
    void setColorDoesNotChangeType() {
        BasicObj obj = new BasicObj(corners, "/images/Crate.png");

        obj.setColor(Color.BLUE);

        assertEquals(RoomObj.DrawType.IMAGE, obj.getType());
    }

    @Test
    void setImagePathChangesPath() {
        BasicObj obj = new BasicObj(corners, "/images/Crate.png");

        obj.setImagePath("/images/walls/BrickWall.png");

        assertEquals("/images/walls/BrickWall.png", obj.getImagePath());
    }

    @Test
    void setShapeCornersChangesCorners() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        Point[] newCorners = {new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(0, 1)};

        obj.setShapeCorners(newCorners);

        assertSame(newCorners, obj.getShapeCorners());
    }

    // ---- light blockers ----

    @Test
    void noLightBlockersByDefault() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertEquals(0, obj.convertLightBlockers().length);
    }

    @Test
    void addAndGetLightBlocker() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightBlocker lb = blocker();

        obj.addLightBlocker("wall", lb);

        assertSame(lb, obj.getLightBlocker("wall"));
    }

    @Test
    void getMissingLightBlockerIsNull() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertNull(obj.getLightBlocker("missing"));
    }

    @Test
    void addingSameNameReplacesLightBlocker() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightBlocker first = blocker();
        LightBlocker second = blocker();

        obj.addLightBlocker("wall", first);
        obj.addLightBlocker("wall", second);

        assertSame(second, obj.getLightBlocker("wall"));
        assertEquals(1, obj.convertLightBlockers().length);
    }

    @Test
    void convertLightBlockersReturnsAll() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightBlocker a = blocker();
        LightBlocker b = blocker();
        obj.addLightBlocker("a", a);
        obj.addLightBlocker("b", b);

        LightBlocker[] result = obj.convertLightBlockers();

        assertEquals(2, result.length);
        assertTrue(List.of(result).contains(a));
        assertTrue(List.of(result).contains(b));
    }

    @Test
    void removeLightBlockerReturnsTrueWhenPresent() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        obj.addLightBlocker("a", blocker());

        assertTrue(obj.removeLightBlocker("a"));
        assertNull(obj.getLightBlocker("a"));
        assertEquals(0, obj.convertLightBlockers().length);
    }

    @Test
    void removeLightBlockerReturnsFalseWhenMissing() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertFalse(obj.removeLightBlocker("missing"));
    }

    // ---- light points ----

    @Test
    void noLightPointsByDefault() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertEquals(0, obj.convertLightPoints().length);
    }

    @Test
    void addAndGetLightPoint() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightPoint lp = lightPoint();

        obj.addLightPoint("lamp", lp);

        assertSame(lp, obj.getLightPoint("lamp"));
    }

    @Test
    void getMissingLightPointIsNull() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertNull(obj.getLightPoint("missing"));
    }

    @Test
    void addingSameNameReplacesLightPoint() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightPoint first = lightPoint();
        LightPoint second = lightPoint();

        obj.addLightPoint("lamp", first);
        obj.addLightPoint("lamp", second);

        assertSame(second, obj.getLightPoint("lamp"));
        assertEquals(1, obj.convertLightPoints().length);
    }

    @Test
    void convertLightPointsReturnsAll() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        LightPoint a = lightPoint();
        LightPoint b = lightPoint();
        obj.addLightPoint("a", a);
        obj.addLightPoint("b", b);

        LightPoint[] result = obj.convertLightPoints();

        assertEquals(2, result.length);
        assertTrue(List.of(result).contains(a));
        assertTrue(List.of(result).contains(b));
    }

    @Test
    void removeLightPointReturnsTrueWhenPresent() {
        BasicObj obj = new BasicObj(corners, Color.RED);
        obj.addLightPoint("a", lightPoint());

        assertTrue(obj.removeLightPoint("a"));
        assertNull(obj.getLightPoint("a"));
        assertEquals(0, obj.convertLightPoints().length);
    }

    @Test
    void removeLightPointReturnsFalseWhenMissing() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        assertFalse(obj.removeLightPoint("missing"));
    }

    @Test
    void lightBlockersAndLightPointsAreStoredSeparately() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        obj.addLightBlocker("same", blocker());

        assertNull(obj.getLightPoint("same"));
        assertEquals(0, obj.convertLightPoints().length);
    }

    // ---- putToScreen ----

    @Test
    void putToScreenReturnsEmptyArray() {
        BasicObj obj = new BasicObj(corners, Color.RED);

        QuadrilateralPanel[] panels = obj.putToScreen();

        assertNotNull(panels);
        assertEquals(0, panels.length);
    }
}
