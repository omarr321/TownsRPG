package RoomClass;

import RoomClass.RoomParts.RoomComponent;

import java.util.ArrayList;

public class Room<T extends RoomComponent>{
    @SuppressWarnings("unchecked") // Suppresses the compiler warning about the cast
    T[] walls = (T[]) new Object[4];
    int lookingWall = 0;
    T floor = null;
    T ceiling = null;

    /**
     * Empty Constructor
     */
    public Room(){
    }

    /**
     * Constructor that takes in the floor and ceiling and sets them.
     * @param floor The floor of the room.
     * @param ceiling The ceiling of the room.
     */
    public Room(T floor, T ceiling){
        this.floor = floor;
        this.ceiling = ceiling;
    }

    /**
     * Adds walls to the room
     * <p>
     * Adds walls to the walls array via index. If the index falls outside 0-3, this method will not do anything and returns false. Otherwise, it will set the wall.
     * <p/>
     * @param wall The wall to add.
     * @param index What index to add it at.
     * @return True if the wall was added, false if not.
     */
    public boolean addWall(T wall, int index){
        if (index < 0 || index > walls.length - 1){
            return false;
        }
        walls[index] = wall;
        return true;
    }

    public T getLookingWall() {
        return this.walls[lookingWall];
    }
    public T getLeftWall() {
        int temp = this.lookingWall-1;
        if (temp < 0){temp = 3;}
        return this.walls[temp];
    }
    public T getRightWall() {
        int temp = this.lookingWall+1;
        if (temp > 3){temp = 0;}
        return this.walls[temp];
    }

    /**
     * Gets the visible walls of a room
     * <p>
     * This method returns the visible walls of a room in an array in the following format: [Left Wall, Center Wall, Right Wall].
     * <p/>
     * @return An array of the visible walls.
     */
    public T[]  getVisibleWalls() {
        @SuppressWarnings("unchecked")
        T[] temp = (T[]) new Object[3];
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
        return true;
    }

    /**
     * This method changes the room looking wall towards the right.
     */
    public void lookRight() {
        this.lookingWall++;
        if (this.lookingWall > walls.length - 1){
            this.lookingWall = 0;
        }
    }

    /**
     * This method changes the room looking wall towards the left.
     */
    public void lookLeft() {
        this.lookingWall--;
        if (this.lookingWall < 0){
            this.lookingWall = walls.length - 1;
        }
    }
}