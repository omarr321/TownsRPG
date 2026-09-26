package roomClass.roomParts;

import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;

import java.awt.*;

public class Floor extends RoomComponent {
    public Floor(String imagePath) {
        super(imagePath);
    }
    public Floor(String imagePath, boolean warped) {
        super(imagePath, warped);
    }
    public Floor(Color color) {
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
        System.err.println("The floor can not contain light blockers!");
        return new LightBlocker[0];
    }

    @Override
    public void addLightBlocker(String name, LightBlocker lightBlocker) {
        System.err.println("The floor can not contain light blockers!");
    }

    @Override
    public boolean removeLightBlocker(String name) {
        System.err.println("The floor can not contain light blockers!");
        return false;
    }

    @Override
    public LightBlocker getLightBlocker(String name) {
        System.err.println("The floor can not contain light blockers!");
        return null;
    }

    @Override
    public LightPoint[] convertLightPoints() {
        System.err.println("The floor can not contain lights!");
        return new LightPoint[0];
    }

    @Override
    public void addLightPoint(String name, LightPoint lightPoint) {
        System.err.println("The floor can not contain lights!");
    }

    @Override
    public boolean removeLightPoint(String name) {
        System.err.println("The floor can not contain lights!");
        return false;
    }

    @Override
    public LightPoint getLightPoint(String name) {
        System.err.println("The floor can not contain lights!");
        return null;
    }
}
