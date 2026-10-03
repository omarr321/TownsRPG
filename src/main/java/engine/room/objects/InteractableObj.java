package engine.room.objects;

import engine.interactions.Interactable;
import helpers.Point;

import java.awt.*;

/**
 * Represents a room object that the user can interact with,
 * holding an optional entry point to trigger custom interaction logic.
 */
public class InteractableObj extends BasicObj{
    private Interactable entryPoint = null;

    /**
     * Constructs an InteractableObj with specified shape corners and a solid color.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param color The solid color to fill the object.
     */
    public InteractableObj(Point[] shapeCorners, Color color) {
        super(shapeCorners, color);
    }

    /**
     * Constructs an InteractableObj with specified shape corners and an image path.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param imagePath The path to the image to render on the object.
     */
    public InteractableObj(Point[] shapeCorners, String imagePath) {
        super(shapeCorners, imagePath);
    }

    /**
     * Constructs an InteractableObj with specified shape corners, an image path, and warp configuration.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param imagePath The path to the image to render on the object.
     * @param warped Whether the image should be warped.
     */
    public InteractableObj(Point[] shapeCorners, String imagePath, boolean warped) {
        super(shapeCorners, imagePath, warped);
    }

    /**
     * Sets the interactable entry point for this object.
     * @param entryPoint The Interactable instance to link to this object.
     */
    public void setEntryPoint(Interactable entryPoint) {
        this.entryPoint = entryPoint;
    }

    /**
     * Triggers the associated interactable entry point.
     * Prints an error message to standard error if no entry point is assigned.
     */
    public void trigger() {
        if (this.entryPoint == null) {
            System.err.println(this + " does not have a entry point.");
            return;
        }
        this.entryPoint.trigger();
    }
}