package Helper;

import javax.swing.*;
import java.awt.*;

/**
 * Loads an image from disk and saves it for future use.
 */
public class ImageLoader {
    private Image currImage;
    private boolean usingDefault = false;
    private boolean loaded = false;
    private static final String DEFAULT_IMAGE = "/images/DebugImage.png";

    /**
     * Construct the ImageLoader with the image path provided.
     * @param path The path to the image to load.
     */
    public ImageLoader(String path) {
        java.net.URL userImg = null;

        if (path != null && !path.isBlank()) {
            userImg = this.getClass().getResource(path);
        }

        if (userImg != null) {
            currImage = new ImageIcon(userImg).getImage();
            this.loaded = true;
        } else {
            userImg = this.getClass().getResource(getDefaultPath());
            this.usingDefault = true;
            if  (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
                this.loaded = true;
            }
        }
    }

    /**
     * Returns the image loaded. Will return the default image if the image could not be loaded.
     * @return - The image loaded using this classes.
     */
    public Image getImage() {
        if(!isLoaded()) {
            System.err.println("There was a error loading the image and the default image!");
        } else {
            if(usingDefault) {
                System.err.println("There was a error loading the image, default image is being used instead!");
            }
            return this.currImage;
        }
        return null;
    }

    /**
     * Returns if the image has loaded or not.
     * @return Returns true if it loaded ok. Returns false if there were any issues, and it could not be loaded.
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Returns if the image is the using the default image.
     * @return Returns true if the image failed to load and fallback to the default image. returns false if the image loaded properly.
     */
    public boolean isUsingDefault() {
        return this.usingDefault;
    }

    //Methods for testing
    String getDefaultPath() {
        return DEFAULT_IMAGE;
    }
}
