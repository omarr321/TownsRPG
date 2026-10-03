package RoomClasses;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightLayer;
import GUI.Lighting.LightMgmt;
import GUI.Lighting.LightPoint;
import Helper.GameSettings;
import Helper.QuadShapeDrawer;
import RoomClasses.RoomObjects.InteractableObj;
import RoomClasses.RoomObjects.RoomObj;
import RoomClasses.RoomParts.RoomComponent;
import RoomClasses.RoomParts.RoomComponent.RoomPart;
import RoomClasses.RoomParts.RoomPoints;
import RoomClasses.RoomParts.Wall;
import RoomClasses.RoomParts.Floor;
import RoomClasses.RoomParts.Ceiling;
import Helper.Point;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D-styled room consisting of four walls, a floor, a ceiling,
 * and associated room geometry points. Manages view navigation (looking left/right),
 * screen rendering, and lighting layers.
 */
public class Room{
    Wall[] walls = new Wall[4];
    int lookingWall = 0;
    Floor floor;
    Ceiling ceiling;
    boolean completed;
    RoomPoints roomPoints;
    private JPanel screenPanel;
    private LightLayer boundLightLayer;
    private LightMgmt boundLightMgmt;

    /**
     * Constructs a Room with the specified floor, ceiling, and room coordinate points.
     * @param floor The floor of the room.
     * @param ceiling The ceiling of the room.
     * @param roomPoints The coordinate points defining the structure of the room parts.
     */
    public Room(Floor floor, Ceiling ceiling, RoomPoints roomPoints){
        this.floor = floor;
        this.ceiling = ceiling;
        this.completed = false;
        this.roomPoints = roomPoints;
    }

    /**
     * Adds a wall to the room at the specified index.
     * <p>
     * If the index falls outside the range 0-3, this method does not do anything.
     *
     * @param wall The wall to add.
     * @param index The index (0-3) to add the wall at.
     */
    public void setWall(Wall wall, int index){
        if (index < 0 || index > walls.length - 1){
            return;
        }
        walls[index] = wall;
    }

    /**
     * Gets the wall the player is currently looking at.
     * @return The active looking Wall.
     */
    public Wall getLookingWall() {
        return this.walls[lookingWall];
    }

    /**
     * Gets the wall to the left of the current looking wall.
     * @return The left Wall.
     */
    public Wall getLeftWall() {
        int temp = this.lookingWall-1;
        if (temp < 0){temp = 3;}
        return this.walls[temp];
    }

    /**
     * Gets the wall to the right of the current looking wall.
     * @return The right Wall.
     */
    public Wall getRightWall() {
        int temp = this.lookingWall+1;
        if (temp > 3){temp = 0;}
        return this.walls[temp];
    }

    /**
     * Gets the fourth wall (opposite) relative to the current looking wall.
     * @return The fourth Wall.
     */
    public Wall getFourthWall() {
        int temp = this.lookingWall-1;
        if (temp < 0){temp = 3;}
        temp -= 1;
        if (temp < 0){temp = 3;}
        return this.walls[temp];
    }

    /**
     * Gets the visible walls of the room in the format: [Left Wall, Center/Looking Wall, Right Wall].
     * @return An array of the visible Wall components.
     */
    public Wall[]  getVisibleWalls() {
        Wall[] temp = new Wall[3];
        temp[0] = this.getLeftWall();
        temp[1] = this.getLookingWall();
        temp[2] = this.getRightWall();
        return temp;
    }

    /**
     * Sets the looking wall index directly.
     * <p>
     * If the index falls outside 0-3, this method does nothing and returns false.
     * Otherwise, it updates the looking wall index and room parts.
     *
     * @param index The index (0-3) you want to set the looking wall to.
     * @return True if the index was successfully set, false otherwise.
     */
    public boolean setLookingIndex(int index){
        if (index < 0 || index > walls.length - 1){
            return false;
        }
        this.lookingWall = index;
        this.updateRoomPart();
        return true;
    }

    private void updateRoomPart() {
        try {
            this.getLookingWall().setRoomPart(RoomPart.BACK_WALL);
            this.getLeftWall().setRoomPart(RoomPart.LEFT_WALL);
            this.getRightWall().setRoomPart(RoomPart.RIGHT_WALL);
            this.getFourthWall().setRoomPart(RoomPart.FOURTH_WALL);
            this.ceiling.setRoomPart(RoomPart.CEILING);
            this.floor.setRoomPart(RoomPart.FLOOR);
            this.completed = true;
        } catch (NullPointerException e) {
            System.err.println("Attempted to update the room parts but fail as there are null values!");
        }
    }

    /**
     * Rotates the player's view to the right, updating the active looking wall.
     */
    public void lookRight() {
        this.lookingWall++;
        if (this.lookingWall > walls.length - 1){
            this.lookingWall = 0;
        }
        updateRoomPart();
    }

    /**
     * Rotates the player's view to the left, updating the active looking wall.
     */
    public void lookLeft() {
        this.lookingWall--;
        if (this.lookingWall < 0){
            this.lookingWall = walls.length - 1;
        }
        updateRoomPart();
    }

    /**
     * Renders the room and its contents into a displayable JPanel.
     * @return A JPanel configured with all room components and interactive objects.
     */
    public JPanel putToScreen() {
        screenPanel = new JPanel();
        screenPanel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
        screenPanel.setLayout(null);
        rebuildScreen();
        return screenPanel;
    }

    /** Clears and re-adds everything based on the current looking wall. */
    public void rebuildScreen() {
        if (screenPanel == null) {
            return;
        }
        screenPanel.removeAll();

        if (!this.completed) {
            System.err.println("Can not put " + this + " to screen as it is an incomplete room!");
            return;
        }

        screenPanel.add(convertRoomComponent(this.floor));
        screenPanel.add(convertRoomComponent(this.ceiling));
        for (RoomComponent rc : this.walls) {
            screenPanel.add(convertRoomComponent(rc));
        }

        for (QuadrilateralPanel t : this.getLookingWall().putToScreen()) {
            screenPanel.add(t, 0);
        }

        refreshLighting();

        screenPanel.revalidate();
        screenPanel.repaint();
    }

    /**
     * Remembers the light layer and light manager so the lighting can be rebuilt automatically
     * whenever the view changes (see {@link #rebuildScreen()}).
     * @param ll The LightLayer holding the blockers.
     * @param lm The LightMgmt holding the light points.
     */
    public void bindLighting(LightLayer ll, LightMgmt lm) {
        this.boundLightLayer = ll;
        this.boundLightMgmt = lm;
        refreshLighting();
    }

    /**
     * Throws away the old blockers and light points and loads the ones for the current looking wall.
     * Does nothing until {@link #bindLighting(LightLayer, LightMgmt)} has been called.
     */
    public void refreshLighting() {
        if (boundLightLayer == null || boundLightMgmt == null || !this.completed) {
            return;
        }
        boundLightLayer.clearBlockers();
        boundLightMgmt.clearLights();
        updateLightLayer(boundLightLayer);
        updateLightMgmt(boundLightMgmt);
    }

    /**
     * Updates the lighting layer with light blockers from the floor, ceiling, and visible/looking walls.
     * @param ll The LightLayer to update.
     */
    public void updateLightLayer(LightLayer ll) {
        if (!this.completed) {
            System.err.println("Can not update " + this + " lights as it is an incomplete room!");
            return;
        }

        ArrayList<LightBlocker> lightBlockers = new ArrayList<>();
        lightBlockers.add(new LightBlocker(convertRoomComponent(this.floor), LightBlocker.LightTag.REFLECT));
        lightBlockers.add(new LightBlocker(convertRoomComponent(this.ceiling), LightBlocker.LightTag.REFLECT));

        lightBlockers.add(new LightBlocker(convertRoomComponent(this.getLeftWall()), LightBlocker.LightTag.REFLECT));
        lightBlockers.add(new LightBlocker(convertRoomComponent(this.getRightWall()), LightBlocker.LightTag.REFLECT));

        lightBlockers.addAll(List.of(this.getLookingWall().convertLightBlockers()));

        for (LightBlocker lb : lightBlockers) {
            ll.addBlocker(lb);
        }
    }

    /**
     * Updates lighting management with light points located on the current looking wall.
     * @param lm The LightMgmt instance to update.
     */
    public void updateLightMgmt(LightMgmt lm) {
        LightPoint[] lightPoints = this.getLookingWall().convertLightPoints();
        for (LightPoint lp : lightPoints) {
            lm.addLight(lp);
        }
    }

    private QuadrilateralPanel convertRoomComponent(RoomComponent roomComp) {
        QuadrilateralPanel temp;
        if (roomComp.getType() == RoomComponent.DrawType.IMAGE) {
            temp = new QuadrilateralPanel(this.roomPoints.getPartPoints(roomComp.getRoomPart()), roomComp.getImagePath(), Color.BLACK, GameSettings.scale(3));
            temp.setImageWarp(roomComp.getWarped());
        } else {
            temp = new QuadrilateralPanel(this.roomPoints.getPartPoints(roomComp.getRoomPart()), roomComp.getColor(), Color.BLACK, GameSettings.scale(3));
        }
        return temp;
    }

    /**
     * Retrieves a list of all interactable objects present on the current looking wall.
     * @return A List of InteractableObj instances.
     */
    public List<InteractableObj> getInteractable() {
        List<InteractableObj> result = new ArrayList<>();
        Wall wall = this.getLookingWall();
        if (wall != null) {
            for (RoomObj obj : wall.getRoomObjs()) {
                if (obj instanceof InteractableObj interactable) {
                    result.add(interactable);
                }
            }
        }
        return result;
    }

    public Point[] getMessageBoxPoints(float scale) {
        Point[] backwallPoints = this.roomPoints.getPartPoints(this.getLookingWall().getRoomPart());

        int height = (int)(GameSettings.screenHeight * scale);
        int length = QuadShapeDrawer.calcDist(backwallPoints[0], backwallPoints[1]);

        Point screenTop = new Point(backwallPoints[0].getX(), GameSettings.screenHeight - height);
        QuadShapeDrawer messageBox = new QuadShapeDrawer(screenTop);
        messageBox.drawLine(0, length);
        messageBox.drawLine(90, height);
        messageBox.drawLine(180, length);

        return messageBox.getPoints();
    }
}