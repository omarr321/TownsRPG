package RoomClasses.RoomObjects;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.Point;
import RoomClasses.RoomParts.RoomComponent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an abstract object within a room, defining geometric shape corners,
 * rendering properties (solid color or image with optional warping), and lighting capabilities.
 */
public abstract class RoomObj {
    /** Whether the image is warped to fit the shape's corners. Defaults to {@code true}. */
    protected boolean warped = true;
    /** How this object is drawn: a solid color, an image, or the default. */
    protected RoomComponent.DrawType type;
    /** The solid fill color, or {@code null} if the object is drawn with an image. */
    protected Color color;
    /** The path to the image drawn on this object, or an empty string if it uses a solid color. */
    protected String imagePath;
    /** The light blockers attached to this object, keyed by name. */
    protected Map<String, LightBlocker> lightBlockers = new HashMap<>();
    /** The light points attached to this object, keyed by name. */
    protected Map<String, LightPoint> lightPoints = new HashMap<>();
    /** The four corner points that define this object's shape. */
    protected Helper.Point[] shapeCorners;

    /**
     * Constructs a RoomObj with a solid fill color and specified shape corners.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param color The solid color to fill the object.
     */
    public RoomObj(Point[] shapeCorners, Color color) {
        this.type = RoomComponent.DrawType.SOLID;
        this.color = color;
        this.imagePath = "";
        this.shapeCorners = shapeCorners;
    }

    /**
     * Constructs a RoomObj with an image path and specified shape corners.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param imagePath The path to the image to render on the object.
     */
    public RoomObj(Point[] shapeCorners, String imagePath) {
        this.type = RoomComponent.DrawType.IMAGE;
        this.color = null;
        this.imagePath = imagePath;
        this.shapeCorners = shapeCorners;
    }

    /**
     * Constructs a RoomObj with an image path, warp configuration, and specified shape corners.
     * @param shapeCorners An array of Point objects representing the corners of the object's shape.
     * @param imagePath The path to the image to render on the object.
     * @param warped Whether the image should be warped.
     */
    public RoomObj(Point[] shapeCorners, String imagePath, boolean warped) {
        this(shapeCorners, imagePath);
        this.warped = warped;
    }

    /**
     * Gets the draw type of this room object.
     * @return The DrawType of the object.
     */
    public abstract DrawType getType();

    /**
     * Gets the solid fill color of this room object.
     * @return The Color of the object, or null if it uses an image.
     */
    public abstract Color getColor();

    /**
     * Sets the solid fill color of this room object.
     * @param color The new Color to set.
     */
    public abstract void setColor(Color color);

    /**
     * Gets the image path of this room object.
     * @return The image path as a String.
     */
    public abstract String getImagePath();

    /**
     * Sets the image path of this room object.
     * @param imagePath The new image path to set.
     */
    public abstract void setImagePath(String imagePath);

    /**
     * Checks if this room object's image is configured to warp.
     * @return True if warped, false otherwise.
     */
    public abstract boolean getWarped();

    /**
     * Converts and retrieves all light blockers associated with this room object.
     * @return An array of LightBlocker objects.
     */
    public abstract LightBlocker[] convertLightBlockers();

    /**
     * Adds a light blocker to this room object with the specified name.
     * @param name The unique identifier for the light blocker.
     * @param lightBlocker The LightBlocker instance to add.
     */
    public abstract void addLightBlocker(String name, LightBlocker lightBlocker);

    /**
     * Removes a light blocker from this room object by its name.
     * @param name The name of the light blocker to remove.
     * @return True if the light blocker was found and removed, false otherwise.
     */
    public abstract boolean removeLightBlocker(String name);

    /**
     * Retrieves a light blocker by its name.
     * @param name The name of the light blocker.
     * @return The LightBlocker instance, or null if not found.
     */
    public abstract LightBlocker getLightBlocker(String name);

    /**
     * Converts and retrieves all light points associated with this room object.
     * @return An array of LightPoint objects.
     */
    public abstract LightPoint[] convertLightPoints();

    /**
     * Adds a light point to this room object with the specified name.
     * @param name The unique identifier for the light point.
     * @param lightPoint The LightPoint instance to add.
     */
    public abstract void addLightPoint(String name, LightPoint lightPoint);

    /**
     * Removes a light point from this room object by its name.
     * @param name The name of the light point to remove.
     * @return True if the light point was found and removed, false otherwise.
     */
    public abstract boolean removeLightPoint(String name);

    /**
     * Retrieves a light point by its name.
     * @param name The name of the light point.
     * @return The LightPoint instance, or null if not found.
     */
    public abstract LightPoint getLightPoint(String name);

    /**
     * Generates and returns an array of QuadrilateralPanels for rendering this object on screen.
     * @return An array of QuadrilateralPanel objects.
     */
    public abstract QuadrilateralPanel[] putToScreen();

    /**
     * Gets the corner points defining the geometric shape of this room object.
     * @return An array of Point objects representing the shape corners.
     */
    public abstract Point[] getShapeCorners();

    /**
     * Defines the types of rendering methods available for walls, ceilings, and other room components.
     */
    public enum DrawType {
        /** The object is filled with a single solid color. */
        SOLID,
        /** The object is drawn using an image. */
        IMAGE
    }
}
