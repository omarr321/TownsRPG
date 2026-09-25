package roomClass.roomParts;

import GUI.Lighting.LightBlocker;

import java.awt.*;

public class Wall extends RoomComponent {
    public Wall(String imagePath) {
        super(imagePath);
    }
    public Wall(String imagePath, boolean warped) {
        super(imagePath, warped);
    }
    public Wall(Color color) {
        super(color);
    }

    @Override
    public DrawType getType() {
        return this.type;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String getImagePath() {
        return this.imagePath;
    }

    @Override
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public RoomPart getRoomPart() {
        return this.roomPart;
    }

    @Override
    public void setRoomPart(RoomPart roomPart) {
        this.roomPart = roomPart;
    }

    @Override
    public boolean getWarped() {
        return this.warped;
    }

    @Override
    public LightBlocker[] convertLightBlockers() {
        return new LightBlocker[0];
    }
}
