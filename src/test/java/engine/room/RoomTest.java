package engine.room;

import gui.lighting.LightBlocker;
import gui.lighting.LightLayer;
import gui.lighting.LightMgmt;
import gui.lighting.LightPoint;
import helpers.GameSettings;
import helpers.Point;
import engine.room.objects.BasicObj;
import engine.room.objects.InteractableObj;
import engine.room.parts.Ceiling;
import engine.room.parts.Floor;
import engine.room.parts.RoomComponent.RoomPart;
import engine.room.parts.RoomPoints;
import engine.room.parts.Wall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test lives in the RoomClasses package so it can look at Room's package-private fields.
 */
public class RoomTest {
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream err;

    private Floor floor;
    private Ceiling ceiling;
    private Wall[] walls;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 1920;
        GameSettings.screenHeight = 1080;
        err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err, true));

        floor = new Floor(Color.GRAY);
        ceiling = new Ceiling(Color.WHITE);
        walls = new Wall[]{new Wall(Color.RED), new Wall(Color.GREEN), new Wall(Color.BLUE), new Wall(Color.YELLOW)};
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private Room emptyRoom() {
        return new Room(floor, ceiling, new RoomPoints(0.5, 0.5, 30, 2));
    }

    /** A room with all four walls set, looking at wall 1 (so the room is "completed"). */
    private Room completeRoom() {
        Room room = emptyRoom();
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }
        room.setLookingIndex(1);
        return room;
    }

    private BasicObj solidObj(int x, int y) {
        Point[] corners = {new Point(x, y), new Point(x + 50, y), new Point(x + 50, y + 50), new Point(x, y + 50)};
        return new BasicObj(corners, Color.RED);
    }

    private InteractableObj interactableObj(int x, int y) {
        Point[] corners = {new Point(x, y), new Point(x + 50, y), new Point(x + 50, y + 50), new Point(x, y + 50)};
        return new InteractableObj(corners, Color.ORANGE);
    }

    // ---- constructor ----

    @Test
    void constructorStartsIncompleteAndLookingAtWallZero() {
        Room room = emptyRoom();

        assertFalse(room.completed);
        assertEquals(0, room.lookingWall);
        assertSame(floor, room.floor);
        assertSame(ceiling, room.ceiling);
        assertNotNull(room.roomPoints);
    }

    @Test
    void wallsStartEmpty() {
        Room room = emptyRoom();

        assertEquals(4, room.walls.length);
        for (Wall wall : room.walls) {
            assertNull(wall);
        }
    }

    // ---- setWall ----

    @Test
    void setWallPlacesWallAtIndex() {
        Room room = emptyRoom();

        room.setWall(walls[2], 2);

        assertSame(walls[2], room.walls[2]);
    }

    @Test
    void setWallWithIndexBelowZeroIsIgnored() {
        Room room = emptyRoom();

        assertDoesNotThrow(() -> room.setWall(walls[0], -1));

        for (Wall wall : room.walls) {
            assertNull(wall);
        }
    }

    @Test
    void setWallWithIndexAboveThreeIsIgnored() {
        Room room = emptyRoom();

        assertDoesNotThrow(() -> room.setWall(walls[0], 4));

        for (Wall wall : room.walls) {
            assertNull(wall);
        }
    }

    @Test
    void setWallReplacesExistingWall() {
        Room room = emptyRoom();

        room.setWall(walls[0], 0);
        room.setWall(walls[1], 0);

        assertSame(walls[1], room.walls[0]);
    }

    // ---- setLookingIndex ----

    @Test
    void setLookingIndexAcceptsValidIndexes() {
        Room room = completeRoom();

        for (int i = 0; i < 4; i++) {
            assertTrue(room.setLookingIndex(i));
            assertEquals(i, room.lookingWall);
        }
    }

    @Test
    void setLookingIndexRejectsInvalidIndexes() {
        Room room = completeRoom();

        assertFalse(room.setLookingIndex(-1));
        assertFalse(room.setLookingIndex(4));
        assertEquals(1, room.lookingWall);
    }

    @Test
    void setLookingIndexAssignsRoomPartsToEveryPiece() {
        Room room = completeRoom(); // looking at wall 1

        assertTrue(room.completed);
        assertEquals(RoomPart.BACK_WALL, walls[1].getRoomPart());
        assertEquals(RoomPart.LEFT_WALL, walls[0].getRoomPart());
        assertEquals(RoomPart.RIGHT_WALL, walls[2].getRoomPart());
        assertEquals(RoomPart.FOURTH_WALL, walls[3].getRoomPart());
        assertEquals(RoomPart.FLOOR, floor.getRoomPart());
        assertEquals(RoomPart.CEILING, ceiling.getRoomPart());
    }

    @Test
    void roomPartsMoveWhenLookingIndexChanges() {
        Room room = completeRoom();

        room.setLookingIndex(3);

        assertEquals(RoomPart.BACK_WALL, walls[3].getRoomPart());
        assertEquals(RoomPart.LEFT_WALL, walls[2].getRoomPart());
        assertEquals(RoomPart.RIGHT_WALL, walls[0].getRoomPart());
        assertEquals(RoomPart.FOURTH_WALL, walls[1].getRoomPart());
    }

    @Test
    void roomStaysIncompleteWhenWallsAreMissing() {
        Room room = emptyRoom();
        room.setWall(walls[0], 0);

        boolean result = room.setLookingIndex(0);

        assertTrue(result);
        assertFalse(room.completed);
        assertFalse(err.toString().isEmpty());
    }

    @Test
    void roomStaysIncompleteWhenFloorOrCeilingIsMissing() {
        Room room = new Room(null, null, new RoomPoints(0.5, 0.5, 30, 2));
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }

        assertDoesNotThrow(() -> room.setLookingIndex(0));

        assertFalse(room.completed);
    }

    // ---- wall getters ----

    @Test
    void wallGettersWhenLookingAtWallZero() {
        Room room = completeRoom();
        room.setLookingIndex(0);

        assertSame(walls[0], room.getLookingWall());
        assertSame(walls[3], room.getLeftWall());
        assertSame(walls[1], room.getRightWall());
        assertSame(walls[2], room.getFourthWall());
    }

    @Test
    void wallGettersWhenLookingAtWallOne() {
        Room room = completeRoom();

        assertSame(walls[1], room.getLookingWall());
        assertSame(walls[0], room.getLeftWall());
        assertSame(walls[2], room.getRightWall());
        assertSame(walls[3], room.getFourthWall());
    }

    @Test
    void wallGettersWhenLookingAtWallTwo() {
        Room room = completeRoom();
        room.setLookingIndex(2);

        assertSame(walls[2], room.getLookingWall());
        assertSame(walls[1], room.getLeftWall());
        assertSame(walls[3], room.getRightWall());
        assertSame(walls[0], room.getFourthWall());
    }

    @Test
    void wallGettersWhenLookingAtWallThree() {
        Room room = completeRoom();
        room.setLookingIndex(3);

        assertSame(walls[3], room.getLookingWall());
        assertSame(walls[2], room.getLeftWall());
        assertSame(walls[0], room.getRightWall());
        assertSame(walls[1], room.getFourthWall());
    }

    @Test
    void getVisibleWallsIsLeftCenterRight() {
        Room room = completeRoom();

        Wall[] visible = room.getVisibleWalls();

        assertEquals(3, visible.length);
        assertSame(room.getLeftWall(), visible[0]);
        assertSame(room.getLookingWall(), visible[1]);
        assertSame(room.getRightWall(), visible[2]);
    }

    @Test
    void getVisibleWallsWrapsAroundTheEnds() {
        Room room = completeRoom();
        room.setLookingIndex(0);

        Wall[] visible = room.getVisibleWalls();

        assertSame(walls[3], visible[0]);
        assertSame(walls[0], visible[1]);
        assertSame(walls[1], visible[2]);
    }

    @Test
    void wallGettersReturnNullWhenWallsAreNotSet() {
        Room room = emptyRoom();

        assertNull(room.getLookingWall());
        assertNull(room.getLeftWall());
        assertNull(room.getRightWall());
        assertNull(room.getFourthWall());
    }

    @Test
    void gettersReturnNullUntilTheRoomIsCompleted() {
        Room room = emptyRoom();
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }

        assertNull(room.getLookingWall());
        assertNull(room.getLeftWall());
        assertNull(room.getRightWall());
        assertNull(room.getFourthWall());
        assertNull(room.getVisibleWalls());
    }

    // ---- lookRight / lookLeft ----

    @Test
    void lookRightMovesToNextWall() {
        Room room = completeRoom(); // index 1

        room.lookRight();

        assertEquals(2, room.lookingWall);
        assertSame(walls[2], room.getLookingWall());
    }

    @Test
    void lookRightWrapsFromLastWallToFirst() {
        Room room = completeRoom();
        room.setLookingIndex(3);

        room.lookRight();

        assertEquals(0, room.lookingWall);
    }

    @Test
    void lookLeftMovesToPreviousWall() {
        Room room = completeRoom(); // index 1

        room.lookLeft();

        assertEquals(0, room.lookingWall);
        assertSame(walls[0], room.getLookingWall());
    }

    @Test
    void lookLeftWrapsFromFirstWallToLast() {
        Room room = completeRoom();
        room.setLookingIndex(0);

        room.lookLeft();

        assertEquals(3, room.lookingWall);
    }

    @Test
    void lookingAroundAFullCircleReturnsToStart() {
        Room room = completeRoom();

        for (int i = 0; i < 4; i++) {
            room.lookRight();
        }
        assertEquals(1, room.lookingWall);

        for (int i = 0; i < 4; i++) {
            room.lookLeft();
        }
        assertEquals(1, room.lookingWall);
    }

    @Test
    void lookRightAndLeftUpdateRoomParts() {
        Room room = completeRoom();

        room.lookRight();
        assertEquals(RoomPart.BACK_WALL, walls[2].getRoomPart());
        assertEquals(RoomPart.LEFT_WALL, walls[1].getRoomPart());

        room.lookLeft();
        room.lookLeft();
        assertEquals(RoomPart.BACK_WALL, walls[0].getRoomPart());
        assertEquals(RoomPart.RIGHT_WALL, walls[1].getRoomPart());
    }

    @Test
    void settingLookingIndexCompletesARoomThatHasAllItsPieces() {
        Room room = emptyRoom();
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }
        assertFalse(room.completed);

        assertTrue(room.setLookingIndex(1));

        assertTrue(room.completed);
    }

    @Test
    void lookRightAndLeftDoNothingOnAnIncompleteRoom() {
        Room room = emptyRoom();
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }

        room.lookRight();
        room.lookLeft();

        assertEquals(0, room.lookingWall);
        assertFalse(room.completed);
        assertFalse(err.toString().isEmpty());
    }

    // ---- putToScreen ----

    @Test
    void putToScreenOfIncompleteRoomReturnsNull() {
        Room room = emptyRoom();

        JPanel panel = room.putToScreen();

        assertNull(panel);
        assertFalse(err.toString().isEmpty());
    }

    @Test
    void putToScreenPanelHasScreenSize() {
        JPanel panel = completeRoom().putToScreen();

        assertEquals(new Dimension(1920, 1080), panel.getPreferredSize());
        assertNull(panel.getLayout());
    }

    @Test
    void putToScreenOfIncompleteRoomDoesNotCreateAScreenPanel() {
        Room room = emptyRoom();

        room.putToScreen();

        assertNull(room.getScreenPanel());
    }

    @Test
    void putToScreenDrawsFloorCeilingAndFourWalls() {
        JPanel panel = completeRoom().putToScreen();

        assertEquals(6, panel.getComponentCount());
    }

    @Test
    void putToScreenAddsLookingWallObjectsOnTop() {
        Room room = completeRoom();
        room.getLookingWall().addRoomObj("crate", solidObj(100, 100));

        JPanel panel = room.putToScreen();

        assertEquals(7, panel.getComponentCount());
        // objects are inserted at index 0 so they are drawn on top of the room
        assertEquals(100, panel.getComponent(0).getX());
        assertEquals(100, panel.getComponent(0).getY());
    }

    @Test
    void putToScreenIgnoresObjectsOnOtherWalls() {
        Room room = completeRoom(); // looking at wall 1
        walls[3].addRoomObj("hidden", solidObj(100, 100));

        JPanel panel = room.putToScreen();

        assertEquals(6, panel.getComponentCount());
    }

    @Test
    void putToScreenWorksWithImageParts() {
        Room room = new Room(new Floor("/images/floors/WoodFloor.png"), new Ceiling("/images/floors/WoodFloor.png"), new RoomPoints(0.5, 0.5, 30, 2));
        for (int i = 0; i < 4; i++) {
            room.setWall(new Wall("/images/walls/BrickWall.png"), i);
        }
        room.setLookingIndex(1);

        JPanel panel = assertDoesNotThrow(room::putToScreen);

        assertEquals(6, panel.getComponentCount());
    }

    // ---- updateLightLayer ----

    @Test
    void updateLightLayerOfIncompleteRoomAddsNothing() {
        Room room = emptyRoom();
        LightLayer layer = new LightLayer(1920, 1080);

        room.updateLightLayer(layer);

        assertTrue(layer.getBlockers().isEmpty());
        assertFalse(err.toString().isEmpty());
    }

    @Test
    void updateLightLayerAddsFloorCeilingAndSideWallsAsReflectors() {
        Room room = completeRoom();
        LightLayer layer = new LightLayer(1920, 1080);

        room.updateLightLayer(layer);

        List<LightBlocker> blockers = layer.getBlockers();
        assertEquals(4, blockers.size());
        for (LightBlocker blocker : blockers) {
            assertEquals(LightBlocker.LightTag.REFLECT, blocker.getTag());
        }
    }

    @Test
    void updateLightLayerAddsLookingWallBlockers() {
        Room room = completeRoom();
        LightBlocker wallBlocker = new LightBlocker(0, 0, 10, 10, LightBlocker.LightTag.BLOCK);
        room.getLookingWall().addLightBlocker("shadow", wallBlocker);
        LightLayer layer = new LightLayer(1920, 1080);

        room.updateLightLayer(layer);

        assertEquals(5, layer.getBlockers().size());
        assertTrue(layer.getBlockers().contains(wallBlocker));
    }

    @Test
    void updateLightLayerAddsLookingWallObjectBlockers() {
        Room room = completeRoom();
        BasicObj obj = solidObj(100, 100);
        LightBlocker objBlocker = new LightBlocker(obj, LightBlocker.LightTag.BLOCK);
        obj.addLightBlocker("obj", objBlocker);
        room.getLookingWall().addRoomObj("crate", obj);
        LightLayer layer = new LightLayer(1920, 1080);

        room.updateLightLayer(layer);

        assertTrue(layer.getBlockers().contains(objBlocker));
    }

    // ---- updateLightMgmt ----

    @Test
    void updateLightMgmtAddsLookingWallLights() {
        Room room = completeRoom();
        LightPoint light = new LightPoint(new Point(10, 10), LightPoint.LightShape.CIRCLE, 100, 0, 0, .5f, Color.WHITE);
        room.getLookingWall().addLightPoint("lamp", light);
        LightMgmt mgmt = new LightMgmt();

        room.updateLightMgmt(mgmt);

        assertEquals(1, mgmt.getLights().size());
        assertSame(light, mgmt.getLights().get(0));
    }

    @Test
    void updateLightMgmtAddsLightsFromRoomObjects() {
        Room room = completeRoom();
        BasicObj obj = solidObj(100, 100);
        LightPoint light = new LightPoint(new Point(10, 10), LightPoint.LightShape.SQUARE, 100, 0, 0, .5f, Color.WHITE);
        obj.addLightPoint("lamp", light);
        room.getLookingWall().addRoomObj("lampObj", obj);
        LightMgmt mgmt = new LightMgmt();

        room.updateLightMgmt(mgmt);

        assertTrue(mgmt.getLights().contains(light));
    }

    @Test
    void updateLightMgmtWithNoLightsAddsNothing() {
        Room room = completeRoom();
        LightMgmt mgmt = new LightMgmt();

        room.updateLightMgmt(mgmt);

        assertTrue(mgmt.getLights().isEmpty());
    }

    @Test
    void updateLightMgmtOnlyUsesTheLookingWall() {
        Room room = completeRoom(); // looking at wall 1
        walls[2].addLightPoint("other", new LightPoint(new Point(0, 0), LightPoint.LightShape.CIRCLE, 50, 0, 0, .5f, Color.WHITE));
        LightMgmt mgmt = new LightMgmt();

        room.updateLightMgmt(mgmt);

        assertTrue(mgmt.getLights().isEmpty());
    }

    // ---- getInteractable ----

    @Test
    void getInteractableIsEmptyWhenWallsAreNotSet() {
        assertTrue(emptyRoom().getInteractable().isEmpty());
    }

    @Test
    void getInteractableIsEmptyWhenWallHasNoObjects() {
        assertTrue(completeRoom().getInteractable().isEmpty());
    }

    @Test
    void getInteractableOnlyReturnsInteractableObjects() {
        Room room = completeRoom();
        InteractableObj interactable = interactableObj(10, 10);
        room.getLookingWall().addRoomObj("plain", solidObj(200, 200));
        room.getLookingWall().addRoomObj("clickable", interactable);

        List<InteractableObj> result = room.getInteractable();

        assertEquals(1, result.size());
        assertSame(interactable, result.get(0));
    }

    @Test
    void getInteractableReturnsEveryInteractableObject() {
        Room room = completeRoom();
        InteractableObj a = interactableObj(10, 10);
        InteractableObj b = interactableObj(300, 300);
        room.getLookingWall().addRoomObj("a", a);
        room.getLookingWall().addRoomObj("b", b);

        List<InteractableObj> result = room.getInteractable();

        assertEquals(2, result.size());
        assertTrue(result.contains(a));
        assertTrue(result.contains(b));
    }

    @Test
    void getInteractableOnlyLooksAtTheLookingWall() {
        Room room = completeRoom(); // wall 1
        walls[2].addRoomObj("clickable", interactableObj(10, 10));

        assertTrue(room.getInteractable().isEmpty());

        room.lookRight(); // now wall 2

        assertEquals(1, room.getInteractable().size());
    }

    @Test
    void testScreenPanel() {
        Room room = completeRoom();
        assertNull(room.getScreenPanel());
        room.rebuildScreen();
        assertNull(room.getScreenPanel());
        room.putToScreen();
        assertNotNull(room.getScreenPanel());
    }

    @Test
    void testBindLightingAndRefreshLighting() {
        Room room = completeRoom();
        assertNull(room.getBoundLightLayer());
        assertNull(room.getBoundLightMgmt());
        room.refreshLighting();
        assertNull(room.getBoundLightLayer());
        assertNull(room.getBoundLightMgmt());

        LightMgmt lm = new LightMgmt();
        LightLayer ll = new LightLayer();
        room.bindLighting(ll, lm);
        assertNotNull(room.getBoundLightLayer());
        assertNotNull(room.getBoundLightMgmt());
        room.refreshLighting();
        assertNotNull(room.getBoundLightLayer());
        assertNotNull(room.getBoundLightMgmt());
    }

    @Test
    void refreshLightingOnIncompleteRoomDoesNotTouchBoundLighting() {
        Room room = emptyRoom();
        LightLayer ll = new LightLayer(1920, 1080);
        LightMgmt lm = new LightMgmt();

        room.bindLighting(ll, lm);

        assertSame(ll, room.getBoundLightLayer());
        assertSame(lm, room.getBoundLightMgmt());
        assertTrue(ll.getBlockers().isEmpty());
        assertTrue(lm.getLights().isEmpty());
    }

    @Test
    void testMessageBoxPoints() {
        Room room = completeRoom();
        Point[] messageBoxPoints = room.getMessageBoxPoints(.33f);
        Point[] expected = new Point[]{new Point(480, 724), new Point(1440, 724), new Point(1440, 1080), new Point(480, 1080)};
        assertEquals(4, messageBoxPoints.length);
        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i].getX(), messageBoxPoints[i].getX(), "x of point " + i);
            assertEquals(expected[i].getY(), messageBoxPoints[i].getY(), "y of point " + i);
        }
    }

    @Test
    void messageBoxPointsOfIncompleteRoomIsNull() {
        assertNull(emptyRoom().getMessageBoxPoints(.33f));
        assertFalse(err.toString().isEmpty());
    }
}