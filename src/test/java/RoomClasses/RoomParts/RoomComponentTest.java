package RoomClasses.RoomParts;

import RoomClasses.RoomObjects.RoomObj;
import RoomClasses.RoomParts.RoomComponent.RoomPart;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoomComponent is abstract, so the class itself is tested through its concrete subclasses,
 * and the nested RoomPart enum is tested directly.
 */
public class RoomComponentTest {

    // ---- RoomPart enum ----

    @Test
    void roomPartHasSixValues() {
        assertEquals(6, RoomPart.values().length);
    }

    @Test
    void ceilingPoints() {
        assertArrayEquals(new int[]{4, 5, 1, 0}, RoomPart.CEILING.getPartPoints());
    }

    @Test
    void floorPoints() {
        assertArrayEquals(new int[]{3, 2, 6, 7}, RoomPart.FLOOR.getPartPoints());
    }

    @Test
    void backWallPoints() {
        assertArrayEquals(new int[]{0, 1, 2, 3}, RoomPart.BACK_WALL.getPartPoints());
    }

    @Test
    void leftWallPoints() {
        assertArrayEquals(new int[]{4, 0, 3, 7}, RoomPart.LEFT_WALL.getPartPoints());
    }

    @Test
    void rightWallPoints() {
        assertArrayEquals(new int[]{1, 5, 6, 2}, RoomPart.RIGHT_WALL.getPartPoints());
    }

    @Test
    void fourthWallPoints() {
        assertArrayEquals(new int[]{0, 0, 0, 0}, RoomPart.FOURTH_WALL.getPartPoints());
    }

    @Test
    void everyRoomPartHasFourIndexesInsideTheEightRoomPoints() {
        for (RoomPart part : RoomPart.values()) {
            int[] indexes = part.getPartPoints();

            assertEquals(4, indexes.length, part + " should have 4 points");
            for (int index : indexes) {
                assertTrue(index >= 0 && index <= 7, part + " has an out of range point index " + index);
            }
        }
    }

    @Test
    void roomPartValueOfWorks() {
        assertEquals(RoomPart.BACK_WALL, RoomPart.valueOf("BACK_WALL"));
        assertThrows(IllegalArgumentException.class, () -> RoomPart.valueOf("NOT_A_PART"));
    }

    // ---- RoomComponent through its subclasses ----

    @Test
    void subclassesAreRoomComponentsAndRoomObjs() {
        RoomComponent[] parts = {new Floor(Color.RED), new Ceiling(Color.RED), new Wall(Color.RED)};

        for (RoomComponent part : parts) {
            assertInstanceOf(RoomObj.class, part);
        }
    }

    @Test
    void roomPartIsNullUntilSet() {
        RoomComponent[] parts = {new Floor(Color.RED), new Ceiling(Color.RED), new Wall(Color.RED)};

        for (RoomComponent part : parts) {
            assertNull(part.getRoomPart());
        }
    }

    @Test
    void roomPartCanBeSetThroughTheBaseType() {
        RoomComponent part = new Wall(Color.RED);

        part.setRoomPart(RoomPart.LEFT_WALL);

        assertEquals(RoomPart.LEFT_WALL, part.getRoomPart());
    }

    @Test
    void colorConstructorsGiveSolidParts() {
        RoomComponent[] parts = {new Floor(Color.RED), new Ceiling(Color.RED), new Wall(Color.RED)};

        for (RoomComponent part : parts) {
            assertEquals(RoomObj.DrawType.SOLID, part.getType());
            assertEquals(Color.RED, part.getColor());
        }
    }

    @Test
    void imageConstructorsGiveImageParts() {
        RoomComponent[] parts = {new Floor("/a.png"), new Ceiling("/a.png"), new Wall("/a.png")};

        for (RoomComponent part : parts) {
            assertEquals(RoomObj.DrawType.IMAGE, part.getType());
            assertEquals("/a.png", part.getImagePath());
            assertTrue(part.getWarped());
        }
    }

    @Test
    void warpedImageConstructorsKeepWarpValue() {
        RoomComponent[] parts = {new Floor("/a.png", false), new Ceiling("/a.png", false), new Wall("/a.png", false)};

        for (RoomComponent part : parts) {
            assertFalse(part.getWarped());
        }
    }
}
