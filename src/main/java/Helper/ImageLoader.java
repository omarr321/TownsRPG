package Helper;

import javax.swing.*;
import java.awt.*;

public class ImageLoader {
    private Image currImage;
    private boolean usingDefault = false;
    private boolean loaded = false;
    private static final String DEFAULT_IMAGE = "/images/DebugImage.png";

    public ImageLoader(String path) {
        java.net.URL userImg = null;

        if (path != null && !path.isBlank()) {
            userImg = this.getClass().getResource(path);
        }

        if (userImg != null) {
            currImage = new ImageIcon(userImg).getImage();
            this.loaded = true;
        } else {
            userImg = this.getClass().getResource(DEFAULT_IMAGE);
            this.usingDefault = true;
            if  (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
                this.loaded = true;
            }
        }
    }

    // package-private, used by tests
    ImageLoader(String path, String defaultPath) {
        java.net.URL userImg = null;

        if (path != null && !path.isBlank()) {
            userImg = this.getClass().getResource(path);
        }

        if (userImg != null) {
            currImage = new ImageIcon(userImg).getImage();
            this.loaded = true;
        } else {
            this.usingDefault = true;
            userImg = this.getClass().getResource(defaultPath);
            if (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
                this.loaded = true;
            }
        }
    }

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

    public boolean isLoaded() {
        return this.loaded;
    }

    public boolean isUsingDefault() {
        return this.usingDefault;
    }
}
