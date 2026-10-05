package engine.room.parts;

import gui.panels.QuadrilateralPanel;
import gui.lighting.LightBlocker;
import gui.lighting.LightPoint;
import helpers.GameSettings;
import helpers.Point;
import engine.room.objects.BasicObj;
import engine.room.objects.RoomObj;
import engine.room.parts.RoomComponent.RoomPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WallTest {

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

    private LightBlocker blocker() {
        return new LightBlocker(0, 0, 5, 5, LightBlocker.LightTag.BLOCK);
    }

    private LightPoint lightPoint() {
        return new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE);
    }

    private Point[] corners(int x, int y, int w, int h) {
        return new Point[]{new Point(x, y), new Point(x + w, y), new Point(x + w, y + h), new Point(x, y + h)};
    }

    private BasicObj solidObj(int x, int y, int w, int h) {
        return new BasicObj(corners(x, y, w, h), Color.RED);
    }

    // ---- constructors ----

    @Test
    void isARoomComponent() {
        assertInstanceOf(RoomComponent.class, new Wall(Color.RED));
    }

    @Test
    void imageConstructor() {
        Wall wall = new Wall("/images/walls/BrickWall.png");

        assertEquals(RoomObj.DrawType.IMAGE, wall.getType());
        assertEquals("/images/walls/BrickWall.png", wall.getImagePath());
        assertNull(wall.getColor());
        assertTrue(wall.getWarped());
        assertNull(wall.getRoomPart());
    }

    @Test
    void imageConstructorWithWarpFalse() {
        Wall wall = new Wall("/images/walls/BrickWall.png", false);

        assertEquals(RoomObj.DrawType.IMAGE, wall.getType());
        assertFalse(wall.getWarped());
    }

    @Test
    void colorConstructor() {
        Wall wall = new Wall(Color.CYAN);

        assertEquals(RoomObj.DrawType.SOLID, wall.getType());
        assertEquals(Color.CYAN, wall.getColor());
        assertEquals("", wall.getImagePath());
        assertTrue(wall.getWarped());
    }

    @Test
    void getShapeCornersIsNullBecauseWallsUseRoomPoints() {
        assertNull(new Wall(Color.RED).getShapeCorners());
    }

    // ---- setters ----

    @Test
    void setColorChangesColor() {
        Wall wall = new Wall(Color.RED);

        wall.setColor(Color.BLUE);

        assertEquals(Color.BLUE, wall.getColor());
    }

    @Test
    void setImagePathChangesPath() {
        Wall wall = new Wall("/images/walls/BrickWall.png");

        wall.setImagePath("/images/objects/Crate.png");

        assertEquals("/images/objects/Crate.png", wall.getImagePath());
    }

    @Test
    void setRoomPartChangesRoomPart() {
        Wall wall = new Wall(Color.RED);

        wall.setRoomPart(RoomPart.BACK_WALL);

        assertEquals(RoomPart.BACK_WALL, wall.getRoomPart());
    }

    // ---- room objects ----

    @Test
    void noRoomObjsByDefault() {
        Wall wall = new Wall(Color.RED);

        assertTrue(wall.getRoomObjs().isEmpty());
        assertNull(wall.getRoomObj("anything"));
    }

    @Test
    void addAndGetRoomObj() {
        Wall wall = new Wall(Color.RED);
        BasicObj obj = solidObj(0, 0, 10, 10);

        wall.addRoomObj("crate", obj);

        assertSame(obj, wall.getRoomObj("crate"));
        assertEquals(1, wall.getRoomObjs().size());
    }

    @Test
    void addingSameKeyReplacesRoomObj() {
        Wall wall = new Wall(Color.RED);
        BasicObj first = solidObj(0, 0, 10, 10);
        BasicObj second = solidObj(5, 5, 10, 10);

        wall.addRoomObj("crate", first);
        wall.addRoomObj("crate", second);

        assertSame(second, wall.getRoomObj("crate"));
        assertEquals(1, wall.getRoomObjs().size());
    }

    @Test
    void getRoomObjsReturnsAllObjects() {
        Wall wall = new Wall(Color.RED);
        BasicObj a = solidObj(0, 0, 10, 10);
        BasicObj b = solidObj(20, 20, 10, 10);
        wall.addRoomObj("a", a);
        wall.addRoomObj("b", b);

        List<RoomObj> objs = wall.getRoomObjs();

        assertEquals(2, objs.size());
        assertTrue(objs.contains(a));
        assertTrue(objs.contains(b));
    }

    @Test
    void getRoomObjsReturnsACopy() {
        Wall wall = new Wall(Color.RED);
        wall.addRoomObj("a", solidObj(0, 0, 10, 10));

        wall.getRoomObjs().clear();

        assertEquals(1, wall.getRoomObjs().size());
    }

    // ---- light blockers ----

    @Test
    void addGetAndRemoveLightBlocker() {
        Wall wall = new Wall(Color.RED);
        LightBlocker lb = blocker();

        wall.addLightBlocker("b", lb);
        assertSame(lb, wall.getLightBlocker("b"));

        assertTrue(wall.removeLightBlocker("b"));
        assertNull(wall.getLightBlocker("b"));
    }

    @Test
    void removeMissingLightBlockerReturnsFalse() {
        assertFalse(new Wall(Color.RED).removeLightBlocker("missing"));
    }

    @Test
    void convertLightBlockersIncludesOwnBlockers() {
        Wall wall = new Wall(Color.RED);
        LightBlocker lb = blocker();
        wall.addLightBlocker("b", lb);

        LightBlocker[] result = wall.convertLightBlockers();

        assertEquals(1, result.length);
        assertSame(lb, result[0]);
    }

    @Test
    void convertLightBlockersIncludesRoomObjBlockers() {
        Wall wall = new Wall(Color.RED);
        BasicObj obj = solidObj(0, 0, 10, 10);
        LightBlocker objBlocker = blocker();
        obj.addLightBlocker("objBlocker", objBlocker);
        wall.addRoomObj("obj", obj);

        LightBlocker[] result = wall.convertLightBlockers();

        assertEquals(1, result.length);
        assertSame(objBlocker, result[0]);
    }

    @Test
    void convertLightBlockersCombinesWallAndRoomObjBlockers() {
        Wall wall = new Wall(Color.RED);
        LightBlocker wallBlocker = blocker();
        wall.addLightBlocker("wallBlocker", wallBlocker);

        BasicObj obj = solidObj(0, 0, 10, 10);
        LightBlocker objBlocker = blocker();
        obj.addLightBlocker("objBlocker", objBlocker);
        wall.addRoomObj("obj", obj);

        List<LightBlocker> result = List.of(wall.convertLightBlockers());

        assertEquals(2, result.size());
        assertTrue(result.contains(wallBlocker));
        assertTrue(result.contains(objBlocker));
    }

    @Test
    void convertLightBlockersIsEmptyWhenNothingAdded() {
        assertEquals(0, new Wall(Color.RED).convertLightBlockers().length);
    }

    // ---- light points ----

    @Test
    void addGetAndRemoveLightPoint() {
        Wall wall = new Wall(Color.RED);
        LightPoint lp = lightPoint();

        wall.addLightPoint("p", lp);
        assertSame(lp, wall.getLightPoint("p"));

        assertTrue(wall.removeLightPoint("p"));
        assertNull(wall.getLightPoint("p"));
    }

    @Test
    void removeMissingLightPointReturnsFalse() {
        assertFalse(new Wall(Color.RED).removeLightPoint("missing"));
    }

    @Test
    void convertLightPointsCombinesWallAndRoomObjLights() {
        Wall wall = new Wall(Color.RED);
        LightPoint wallLight = lightPoint();
        wall.addLightPoint("wallLight", wallLight);

        BasicObj obj = solidObj(0, 0, 10, 10);
        LightPoint objLight = lightPoint();
        obj.addLightPoint("objLight", objLight);
        wall.addRoomObj("obj", obj);

        List<LightPoint> result = List.of(wall.convertLightPoints());

        assertEquals(2, result.size());
        assertTrue(result.contains(wallLight));
        assertTrue(result.contains(objLight));
    }

    @Test
    void convertLightPointsIsEmptyWhenNothingAdded() {
        assertEquals(0, new Wall(Color.RED).convertLightPoints().length);
    }

    // ---- putToScreen ----

    @Test
    void putToScreenWithoutRoomObjsIsEmpty() {
        assertEquals(0, new Wall(Color.RED).putToScreen().length);
    }

    @Test
    void putToScreenMakesOnePanelPerRoomObj() {
        Wall wall = new Wall(Color.RED);
        wall.addRoomObj("a", solidObj(10, 20, 50, 30));
        wall.addRoomObj("b", new BasicObj(corners(100, 100, 40, 40), "/images/objects/Crate.png", false));

        QuadrilateralPanel[] panels = wall.putToScreen();

        assertEquals(2, panels.length);
    }

    @Test
    void putToScreenPanelsMatchTheRoomObjCorners() {
        Wall wall = new Wall(Color.RED);
        wall.addRoomObj("solid", solidObj(10, 20, 50, 30));
        wall.addRoomObj("image", new BasicObj(corners(100, 200, 40, 60), "/images/objects/Crate.png", false));

        List<Rectangle> bounds = java.util.Arrays.stream(wall.putToScreen())
                .map(QuadrilateralPanel::getBounds)
                .toList();

        assertTrue(bounds.contains(new Rectangle(10, 20, 50, 30)));
        assertTrue(bounds.contains(new Rectangle(100, 200, 40, 60)));
    }
}
