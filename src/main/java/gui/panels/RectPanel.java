package gui.panels;

import helpers.ImageLoader;

import javax.swing.*;
import java.awt.*;

/**
 * A basic rectangular panel to draw to the screen.
 */
public class RectPanel extends JPanel {
    private int xCoord;
    private int yCoord;
    private int width;
    private int height;
    private Color borderColor;
    private Color fillColor;
    private int thickness;

    private String imagePath = "";
    private boolean usesImage = false;
    private static final String DEFAULT_IMAGE = "/images/debugging/DebugImage.png";

    /**
     * Constructs the rectangular panel using the values provided.
     * @param xCoord The x of the top left of the panel.
     * @param yCoord The y of the top left of the panel.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param fillColor The color to fill the shape.
     */
    public RectPanel(int xCoord, int yCoord, int width, int height, Color fillColor) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.width = width;
        this.height = height;
        this.fillColor = fillColor;
        this.borderColor = null;
        this.thickness = 0;

        this.setBounds(xCoord, yCoord, width, height);
        this.setOpaque(false);
    }

    /**
     * Constructs the rectangular panel using the values provided. The border will be drawn with a thickness of 1.
     * @param xCoord The x of the top left of the panel.
     * @param yCoord The y of the top left of the panel.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param fillColor The color to fill the shape.
     * @param borderColor The color of the border.
     */
    public RectPanel(int xCoord, int yCoord, int width, int height, Color fillColor, Color borderColor) {
        this(xCoord, yCoord, width, height, fillColor, borderColor, 1);
    }

    /**
     * Constructs the rectangular panel using the values provided, including border color and custom thickness.
     * @param xCoord The x of the top left of the panel.
     * @param yCoord The y of the top left of the panel.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param fillColor The color to fill the shape.
     * @param borderColor The color of the border.
     * @param thickness The thickness of the border.
     */
    public RectPanel(int xCoord, int yCoord, int width, int height, Color fillColor, Color borderColor, int thickness) {
        this(xCoord, yCoord, width, height, fillColor);
        this.borderColor = borderColor;
        this.thickness = thickness;
    }

    /**
     * Constructs the rectangular panel using an image loaded from the specified path.
     * @param xCoord The x of the top left of the panel.
     * @param yCoord The y of the top left of the panel.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param imagePath The path to the image to display.
     */
    public RectPanel(int xCoord, int yCoord, int width, int height, String imagePath) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
        this.usesImage = true;

        this.setBounds(xCoord, yCoord, width, height);
        this.setOpaque(false);
    }

    /**
     * Constructs the rectangular panel using an image loaded from the specified path, along with a border color and thickness.
     * @param xCoord The x of the top left of the panel.
     * @param yCoord The y of the top left of the panel.
     * @param width The width of the shape.
     * @param height The height of the shape.
     * @param imagePath The path to the image to display.
     * @param borderColor The color of the border.
     * @param thickness The thickness of the border.
     */
    public RectPanel(int xCoord, int yCoord, int width, int height, String imagePath, Color borderColor, int thickness) {
        this(xCoord, yCoord, width, height, imagePath);
        this.borderColor = borderColor;
        this.thickness = thickness;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D customGraphic = (Graphics2D) g;

        if (usesImage) {
            ImageLoader image = createImageLoader(this.imagePath);
            Image currImage = image.getImage();
            if (image.isLoaded()) {
                customGraphic.drawImage(currImage, 0, 0, width, height, this);
            } else {
                customGraphic.setColor(Color.MAGENTA);
                customGraphic.fillRect(0, 0, this.width, this.height);
            }
        } else {
            customGraphic.setColor(this.fillColor);
            customGraphic.fillRect(0, 0, this.width, this.height);
        }

        if (thickness > 0) {
            drawBorder(customGraphic, 0, 0, this.width, this.height, this.borderColor, this.thickness);
        }
    }

    private void drawBorder(Graphics2D item, int xCoord, int yCoord, int width, int height, Color color, int thickness) {
        item.setColor(color);
        item.drawRect(xCoord, yCoord, width-1, height-1);

        int temp = thickness-1;
        if (temp == 0) {
            return;
        }
        drawBorder(item, xCoord+1, yCoord+1, width-2, height-2, color, temp);
    }

    ImageLoader createImageLoader(String path) {
        return new ImageLoader(path);
    }
}