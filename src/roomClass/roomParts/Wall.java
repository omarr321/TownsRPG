package roomClass.roomParts;

import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;

import java.awt.*;
import java.util.ArrayList;

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
        return this.lightBlockers.values().toArray(new LightBlocker[]{});
    }

    @Override
    public void addLightBlocker(String name, LightBlocker lightBlocker) {
        this.lightBlockers.put(name, lightBlocker);
    }

    @Override
    public boolean removeLightBlocker(String name) {
        if (this.lightBlockers.containsKey(name)) {
            this.lightBlockers.remove(name);
            return true;
        }
        return false;
    }

    @Override
    public LightBlocker getLightBlocker(String name) {
        return this.lightBlockers.get(name);
    }

    @Override
    public LightPoint[] convertLightPoints() {
        return this.lightPoints.values().toArray(new LightPoint[]{});
    }

    @Override
    public void addLightPoint(String name, LightPoint lightPoint) {
        this.lightPoints.put(name, lightPoint);
    }

    @Override
    public boolean removeLightPoint(String name) {
        if (this.lightPoints.containsKey(name)) {
            this.lightPoints.remove(name);
            return true;
        }
        return false;
    }

    @Override
    public LightPoint getLightPoint(String name) {
        return this.lightPoints.get(name);
    }
}
