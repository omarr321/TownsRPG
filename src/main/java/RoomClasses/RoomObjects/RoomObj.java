package RoomClasses.RoomObjects;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.Point;
import RoomClasses.roomParts.RoomComponent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class RoomObj {
    protected boolean warped = true;
    protected RoomComponent.DrawType type;
    protected Color color;
    protected String imagePath;
    protected Map<String, LightBlocker> lightBlockers = new HashMap<>();
    protected Map<String, LightPoint> lightPoints = new HashMap<>();
    protected Helper.Point[] shapeCorners = new Point[4];

    public RoomObj(Point[] shapeCorners, Color color) {
        this.type = RoomComponent.DrawType.SOLID;
        this.color = color;
        this.imagePath = "";
        this.shapeCorners = shapeCorners;
    }

    public RoomObj(Point[] shapeCorners, String imagePath) {
        this.type = RoomComponent.DrawType.IMAGE;
        this.color = null;
        this.imagePath = imagePath;
        this.shapeCorners = shapeCorners;
    }

    public RoomObj(Point[] shapeCorners, String imagePath, boolean warped) {
        this(shapeCorners, imagePath);
        this.warped = warped;
    }

    public abstract DrawType getType();

    public abstract Color getColor();
    public abstract void setColor(Color color);

    public abstract String getImagePath();
    public abstract void setImagePath(String imagePath);

    public abstract boolean getWarped();

    public abstract LightBlocker[] convertLightBlockers();
    public abstract void addLightBlocker(String name, LightBlocker lightBlocker);
    public abstract boolean removeLightBlocker(String name);
    public abstract LightBlocker getLightBlocker(String name);

    public abstract LightPoint[] convertLightPoints();
    public abstract void addLightPoint(String name, LightPoint lightPoint);
    public abstract boolean removeLightPoint(String name);
    public abstract LightPoint getLightPoint(String name);

    public abstract QuadrilateralPanel[] putToScreen();

    public abstract Point[] getShapeCorners();

    //enum for the type of images you can use for the walls/ceiling/whatever
    public enum DrawType {
        SOLID,
        IMAGE,
        DEFAULT
    }
}
