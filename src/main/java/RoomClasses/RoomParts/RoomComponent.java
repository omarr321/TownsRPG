package RoomClasses.RoomParts;

import RoomClasses.RoomObjects.RoomObj;

import java.awt.*;

/**
 * Represents an abstract component of a room that extends {@code RoomObj}.
 * <p>
 * A {@code RoomComponent} serves as a base for structural parts of a room
 * (such as walls, ceilings, and floors). It links objects to a specific
 * {@link RoomPart} designation and manages properties like color, images, and warping.
 */
public abstract class RoomComponent extends RoomObj {
    /** The part of the room (ceiling, floor, or a wall) that this component represents. */
    protected RoomComponent.RoomPart roomPart;

    /**
     * Constructs a RoomComponent with a solid fill color.
     * @param color The color of the component.
     */
    public RoomComponent(Color color) {
        super(null, color);
    }

    /**
     * Constructs a RoomComponent with an image path.
     * @param imagePath The path to the image for this component.
     */
    public RoomComponent(String imagePath) {
        super(null, imagePath);
    }

    /**
     * Constructs a RoomComponent with an image path and a warp configuration.
     * @param imagePath The path to the image for this component.
     * @param warped Whether the image should be warped.
     */
    public RoomComponent(String imagePath, boolean warped) {
        super(null, imagePath, warped);
    }

    /**
     * Gets the specific room part classification of this component.
     * @return The RoomPart associated with this component.
     */
    public abstract RoomComponent.RoomPart getRoomPart();

    /**
     * Sets the specific room part classification of this component.
     * @param roomPart The RoomPart to set.
     */
    public abstract void setRoomPart(RoomComponent.RoomPart roomPart);

    /**
     * Defines the standard structural parts of a room and their corresponding coordinate indices.
     */
    public enum RoomPart {
        /** The top surface of the room. */
        CEILING(new int[]{4, 5, 1, 0}),
        /** The bottom surface of the room that the player walks on. */
        FLOOR(new int[]{3, 2, 6, 7}),
        /** The wall at the far end of the room, facing the viewer. */
        BACK_WALL(new int[]{0, 1, 2, 3}),
        /** The wall on the left side of the room. */
        LEFT_WALL(new int[]{4, 0, 3, 7}),
        /** The wall on the right side of the room. */
        RIGHT_WALL(new int[]{1, 5, 6, 2}),
        /** The wall on the viewer's side of the room, with all indices set to 0 so that nothing is drawn. */
        FOURTH_WALL(new int[]{0, 0, 0, 0});

        private final int[] arr;
        RoomPart(int[] arr) {
           this.arr = arr;
        }

        /**
         * Gets the point indices array associated with this room part.
         * @return An array of integers representing the part points.
         */
        public int[] getPartPoints(){
            return this.arr;
        }
    }
}
