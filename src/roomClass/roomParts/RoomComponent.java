package roomClass.roomParts;

/**
 * This class is a room component which can be part of a room.
 */
public abstract class RoomComponent {
    protected Type type = Type.DEFAULT;
    protected Color color = new Color(Color.Room_Colors.NONE);
    protected String imagePath = "";

    public RoomComponent(Type type, Color color, String imagePath) {
        this.type = type;
        this.color = color;
        this.imagePath = imagePath;
    }

    public abstract Type getType();
    public abstract void setType(Type type);

    public abstract Color getColor();
    public abstract void setColor(Color color);

    public abstract String getImagePath();
    public abstract void setImagePath(String imagePath);

    //enum for the type of images you can use for the walls/ceiling/whatever
    public enum Type {
        SOLID,
        IMAGE,
        DEFAULT
    }
}
