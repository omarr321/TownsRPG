package RoomClasses;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.Lighting.LightBlocker;
import GUI.Lighting.LightLayer;
import GUI.Lighting.LightMgmt;
import GUI.Lighting.LightPoint;
import Helper.GameSettings;
import RoomClasses.RoomObjects.InteractableObj;
import RoomClasses.RoomObjects.RoomObj;
import RoomClasses.roomParts.RoomComponent;
import RoomClasses.roomParts.RoomComponent.RoomPart;
import RoomClasses.roomParts.RoomPoints;
import RoomClasses.roomParts.Wall;
import RoomClasses.roomParts.Floor;
import RoomClasses.roomParts.Ceiling;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Room{
    Wall[] walls = new Wall[4];
    int lookingWall = 0;
    Floor floor = null;
    Ceiling ceiling = null;
    boolean completed = false;
    RoomPoints roomPoints;

    /**
     * Constructor that takes in the floor and ceiling and sets them.
     * @param floor The floor of the room.
     * @param ceiling The ceiling of the room.
     */
    public Room(Floor floor, Ceiling ceiling, RoomPoints roomPoints){
        this.floor = floor;
        this.ceiling = ceiling;
        this.completed = false;
        this.roomPoints = roomPoints;
    }

    /**
     * Adds walls to the room
     * <p>
     * Adds walls to the walls array via index. If the index falls outside 0-3, this method will not do anything and returns false. Otherwise, it will set the wall.
     * <p/>
     *
     * @param wall  The wall to add.
     * @param index What index to add it at.
     */
    public void setWall(Wall wall, int index){
        if (index < 0 || index > walls.length - 1){
            return;
        }
        walls[index] = wall;
    }

    public Wall getLookingWall() {
        return this.walls[lookingWall];
    }
    public Wall getLeftWall() {
        int temp = this.lookingWall-1;
        if (temp < 0){temp = 3;}
        return this.walls[temp];
    }
    public Wall getRightWall() {
        int temp = this.lookingWall+1;
        if (temp > 3){temp = 0;}
        return this.walls[temp];
    }
    public Wall getFourthWall() {
        int temp = this.lookingWall-1;
        if (temp < 0){temp = 3;}
        temp -= 1;
        if (temp < 0){temp = 3;}
        return this.walls[temp];
    }

    /**
     * Gets the visible walls of a room
     * <p>
     * This method returns the visible walls of a room in an array in the following format: [Left Wall, Center Wall, Right Wall].
     * <p/>
     * @return An array of the visible walls.
     */
    public Wall[]  getVisibleWalls() {
        Wall[] temp = new Wall[3];
        temp[0] = this.getLeftWall();
        temp[1] = this.getLookingWall();
        temp[2] = this.getRightWall();
        return temp;
    }

    /**
     * Sets the looking index.
     * <p>
     * Sets the looking index directly. If the index falls outside 0-3, this method will not do anything and returns false. Otherwise, it will set the looking wall.
     * <p/>
     * @param index The index you want to set the looking wall to.
     * @return True if the index was set, false if otherwise.
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
     * This method changes the room looking wall towards the right.
     */
    public void lookRight() {
        this.lookingWall++;
        if (this.lookingWall > walls.length - 1){
            this.lookingWall = 0;
        }
        updateRoomPart();
    }

    /**
     * This method changes the room looking wall towards the left.
     */
    public void lookLeft() {
        this.lookingWall--;
        if (this.lookingWall < 0){
            this.lookingWall = walls.length - 1;
        }
        updateRoomPart();
    }

    public JPanel putToScreen() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(GameSettings.screenWidth, GameSettings.screenHeight));
        panel.setLayout(null);

        if (!this.completed) {
            System.err.println("Can not put " + this + " to screen as it is an imcomplete room!");
            return panel;
        }

        panel.add(convertRoomComponent(this.floor));
        panel.add(convertRoomComponent(this.ceiling));
        for (RoomComponent rc : this.walls) {
            panel.add(convertRoomComponent(rc));
        }

        QuadrilateralPanel[] temp = this.getLookingWall().putToScreen();
        for(QuadrilateralPanel t : temp) {
            panel.add(t, 0);
        }

        return panel;
    }

    public void updateLightLayer(LightLayer ll) {
        if (!this.completed) {
            System.err.println("Can not update " + this + " lights as it is an imcomplete room!");
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

    public void updateLightMgmt(LightMgmt lm) {
        LightPoint[] lightPoints = this.getLookingWall().convertLightPoints();
        for (LightPoint lp : lightPoints) {
            lm.addLight(lp);
        }
    }

    private LightBlocker[] convertLightBlockers(RoomComponent roomComponent) {
        return roomComponent.convertLightBlockers();
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
}