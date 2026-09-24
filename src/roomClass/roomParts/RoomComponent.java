package roomClass.roomParts;

import java.awt.*;

/**
 * This class is a room component which can be part of a room.
 */
public abstract class RoomComponent {
    protected DrawType type;
    protected Color color;
    protected String imagePath = "";
    protected RoomPart roomPart;
    protected boolean warped = true;

    public RoomComponent(Color color) {
        this.type = DrawType.SOLID;
        this.color = color;
        this.imagePath = "";
    }

    public RoomComponent(String imagePath) {
        this.type = DrawType.IMAGE;
        this.color = null;
        this.imagePath = imagePath;
    }

    public RoomComponent(String imagePath, boolean warpped) {
        this(imagePath);
        this.warped = warpped;
    }

    public abstract DrawType getType();

    public abstract Color getColor();
    public abstract void setColor(Color color);

    public abstract String getImagePath();
    public abstract void setImagePath(String imagePath);

    public abstract RoomPart getRoomPart();
    public abstract void setRoomPart(RoomPart roomPart);

    public abstract boolean getWarped();

    //enum for the type of images you can use for the walls/ceiling/whatever
    public enum DrawType {
        SOLID,
        IMAGE,
        DEFAULT
    }

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
