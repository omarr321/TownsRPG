package RoomClasses.RoomObjects;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.Point;

import java.awt.*;
import java.util.ArrayList;

public class BasicObj extends RoomObj{
    private Point[] shapeCorners;

    public BasicObj(Point[] shapeCorners, Color color) {
        super(shapeCorners, color);
        this.shapeCorners = shapeCorners;
    }
    public BasicObj(Point[] shapeCorners, String imagePath) {
        super(shapeCorners, imagePath);
        this.shapeCorners = shapeCorners;
    }
    public BasicObj(Point[] shapeCorners, String imagePath, boolean warped) {
        super(shapeCorners, imagePath, warped);
        this.shapeCorners = shapeCorners;
    }

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
    public boolean getWarped() {
        return this.warped;
    }

    @Override
    public LightBlocker[] convertLightBlockers() {
        ArrayList<LightBlocker> tempArr = new ArrayList<>();
        this.lightBlockers.forEach((_, value) -> tempArr.add(value));
        return tempArr.toArray(new LightBlocker[0]);
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
        ArrayList<LightPoint> tempArr = new ArrayList<>();
        this.lightPoints.forEach((_, value) -> tempArr.add(value));
        return tempArr.toArray(new LightPoint[0]);
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

    @Override
    public QuadrilateralPanel[] putToScreen() {
        return new QuadrilateralPanel[0];
    }

    public Point[] getShapeCorners() {
        return shapeCorners;
    }

    public void setShapeCorners(Point[] shapeCorners) {
        this.shapeCorners = shapeCorners;
    }
}
