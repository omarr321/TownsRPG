package RoomClasses.RoomParts;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightPoint;
import Helper.Point;
import RoomClasses.RoomObjects.RoomObj;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a physical wall within a room.
 * <p>
 * The {@code Wall} class extends {@code RoomComponent} and acts as a container for
 * various room objects ({@code RoomObj}), light blockers, and light points. It supports
 * rendering via solid colors or images (with optional warping) and provides functionality
 * to convert its contents into screen-ready quadrilateral panels and lighting components.
 */
public class Wall extends RoomComponent {
    private final Map<String, RoomObj> roomObjs = new HashMap<>();

    /**
     * Constructs a Wall using the specified background image path.
     * @param imagePath The path to the image for this wall.
     */
    public Wall(String imagePath) {
        super(imagePath);
    }

    /**
     * Constructs a Wall using the specified background image path and warp configuration.
     * @param imagePath The path to the image for this wall.
     * @param warped Whether the image should be warped.
     */
    public Wall(String imagePath, boolean warped) {
        super(imagePath, warped);
    }

    /**
     * Constructs a Wall using a solid fill color.
     * @param color The color of this wall.
     */
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
        ArrayList<LightBlocker> tempArr = new ArrayList<>();
        this.lightBlockers.forEach((key, value) -> tempArr.add(value));
        this.roomObjs.forEach((key, value) -> {
            tempArr.addAll(List.of(value.convertLightBlockers()));
        });
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
        this.lightPoints.forEach((key, value) -> tempArr.add(value));
        this.roomObjs.forEach((key, value) -> {
            tempArr.addAll(List.of(value.convertLightPoints()));
        });
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

    /**
     * Adds a room object to this wall's collection.
     * @param key The unique key identifier for the room object.
     * @param roomObj The RoomObj instance to add.
     */
    public void addRoomObj(String key, RoomObj roomObj) {
        //System.out.println("Adding " + key + " with value " + roomObj);
        this.roomObjs.put(key, roomObj);
    }

    /**
     * Retrieves a room object by its key.
     * @param key The key identifier of the room object.
     * @return The RoomObj instance, or null if not found.
     */
    public RoomObj getRoomObj(String key) {
        return this.roomObjs.get(key);
    }

    /**
     * Returns a list of all room objects stored on this wall.
     * @return A List containing all RoomObj instances.
     */
    public List<RoomObj> getRoomObjs() {
        return new ArrayList<>(this.roomObjs.values());
    }

    /**
     * Generates and returns an array of QuadrilateralPanels for rendering the room objects on screen.
     * @return An array of QuadrilateralPanel objects representing the room objects.
     */
    public QuadrilateralPanel[] putToScreen() {
        ArrayList<QuadrilateralPanel> qPanel = new ArrayList<>();
        this.roomObjs.forEach((key, value) -> {
            QuadrilateralPanel temp;
            if (value.getType() == RoomComponent.DrawType.IMAGE) {
                temp = new QuadrilateralPanel(value.getShapeCorners(), value.getImagePath());
                temp.setImageWarp(value.getWarped());
            } else {
                temp = new QuadrilateralPanel(value.getShapeCorners(), value.getColor());
            }
            qPanel.add(temp);
        });
        return qPanel.toArray(new QuadrilateralPanel[0]);
    }

    @Override
    public Point[] getShapeCorners() {
        return this.shapeCorners;
    }
}