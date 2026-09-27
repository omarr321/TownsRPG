package RoomClasses.RoomObjects;

import RoomClasses.roomParts.RoomComponent;

import java.awt.*;

public abstract class RoomObj {
    protected boolean warped = true;
    protected RoomComponent.DrawType type;
    protected Color color;
    protected String imagePath = "";

    public RoomObj(Color color) {
        this.type = RoomComponent.DrawType.SOLID;
        this.color = color;
        this.imagePath = "";
    }

    public RoomObj(String imagePath) {
        this.type = RoomComponent.DrawType.IMAGE;
        this.color = null;
        this.imagePath = imagePath;
    }

    public RoomObj(String imagePath, boolean warped) {
        this(imagePath);
        this.warped = warped;
    }

    public abstract DrawType getType();

    public abstract Color getColor();
    public abstract void setColor(Color color);

    public abstract String getImagePath();
    public abstract void setImagePath(String imagePath);

    public abstract RoomComponent.RoomPart getRoomPart();
    public abstract void setRoomPart(RoomComponent.RoomPart roomPart);

    public abstract boolean getWarped();

    //enum for the type of images you can use for the walls/ceiling/whatever
    public enum DrawType {
        SOLID,
        IMAGE,
        DEFAULT
    }
}
