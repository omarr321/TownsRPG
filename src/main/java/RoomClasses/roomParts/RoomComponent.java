package RoomClasses.roomParts;

import RoomClasses.RoomObjects.RoomObj;

import java.awt.*;

/**
 * This class is a room component which can be part of a room.
 */
public abstract class RoomComponent extends RoomObj {
    protected RoomComponent.RoomPart roomPart;

    public RoomComponent(Color color) {
        super(null, color);
    }

    public RoomComponent(String imagePath) {
        super(null, imagePath);
    }

    public RoomComponent(String imagePath, boolean warped) {
        super(null, imagePath, warped);
    }

    public abstract RoomComponent.RoomPart getRoomPart();
    public abstract void setRoomPart(RoomComponent.RoomPart roomPart);

    public enum RoomPart {
        CEILING(new int[]{4, 5, 1, 0}),
        FLOOR(new int[]{3, 2, 6, 7}),
        BACK_WALL(new int[]{0, 1, 2, 3}),
        LEFT_WALL(new int[]{4, 0, 3, 7}),
        RIGHT_WALL(new int[]{1, 5, 6, 2}),
        FOURTH_WALL(new int[]{0, 0, 0, 0});

        private final int[] arr;
        RoomPart(int[] arr) {
           this.arr = arr;
        }

        public int[] getPartPoints(){
            return this.arr;
        }
    }
}
