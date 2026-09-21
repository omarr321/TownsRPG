package GraphicClasses.CustomPanels;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
    private final String DEFAULT_IMAGE = "/images/DebugImage.png";

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

    public RectPanel(int xCoord, int yCoord, int width, int height, Color fillColor, Color borderColor) {
        this(xCoord, yCoord, width, height, fillColor, borderColor, 1);
    }

    public RectPanel(int xCoord, int yCoord, int width, int height, Color fillColor, Color borderColor, int thickness) {
        this(xCoord, yCoord, width, height, fillColor);
        this.borderColor = borderColor;
        this.thickness = thickness;
    }

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
            Image currImage;
            java.net.URL userImg = this.getClass().getResource(imagePath);
            if (userImg != null) {
                currImage = new ImageIcon(userImg).getImage();
            } else {
                System.err.println("Image not found! Defaulting to debug image.");
                userImg = this.getClass().getResource(DEFAULT_IMAGE);
                if  (userImg == null) {
                    System.err.println("Default image not found! What did you do?!");
                    System.exit(1);
                }
                currImage = new ImageIcon(userImg).getImage();
            }
            customGraphic.drawImage(currImage, 0, 0, width, height, this);
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
}