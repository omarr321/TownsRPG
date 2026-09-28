package RoomClasses.RoomObjects;

import Helper.Point;
import RoomClasses.Interaction;

import java.awt.*;

public class InteractableObj extends BasicObj{
    private Interaction entryPoint = null;

    public InteractableObj(Point[] shapeCorners, Color color) {
        super(shapeCorners, color);
    }
    public InteractableObj(Point[] shapeCorners, String imagePath) {
        super(shapeCorners, imagePath);
    }
    public InteractableObj(Point[] shapeCorners, String imagePath, boolean warped) {
        super(shapeCorners, imagePath, warped);
    }

    public void setEntryPoint(Interaction entryPoint) {
        this.entryPoint = entryPoint;
    }

    public void trigger() {
        if (this.entryPoint == null) {
            System.err.println(this + " does not have a entry point.");
            return;
        }
        this.entryPoint.trigger();
    }
}