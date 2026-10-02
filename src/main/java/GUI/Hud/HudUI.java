package GUI.Hud;

import GUI.CustomPanels.QuadrilateralPanel;
import GUI.CustomPanels.RectPanel;
import Helper.GameSettings;
import Helper.ImageLoader;
import Helper.Point;
import RoomClasses.Room;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

/**
 * The HudUI class draws the UI elements to the screen. This includes: arrows for navigation, message box.
 */
public class HudUI extends LayerUI<JComponent> {
    //-----ADJUSTMENT VARIABLES-----//
    //The width of the left and right arrows.
    private static final int ARROW_WIDTH = 50;
    //How much space the message box will take up the screen. (ex: .33f is %33).
    private static final float SIZE_RATIO = .25f;

    //-----UI IMAGES-----//
    private static final ImageLoader RIGHT_ARROW = new ImageLoader("/images/UI/arrow_right.png");
    private static final ImageLoader LEFT_ARROW = new ImageLoader("/images/UI/arrow_left.png");
    private static final ImageLoader RIGHT_ARROW_X = new ImageLoader("/images/UI/arrow_right_x.png");
    private static final ImageLoader LEFT_ARROW_X = new ImageLoader("/images/UI/arrow_left_x.png");

    private final Room room;

    /**
     * Constructs the hud.
     */
    public HudUI(Room room) {
        this.room = room;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        int width = c.getWidth();
        int height = c.getHeight();
        super.paint(g, c);
        if (width <= 0 || height <= 0) {
            return;
        }

        Point[] messageBoxPoints = room.getMessageBoxPoints(HudUI.SIZE_RATIO);

        int[] xPoints = new int[4];
        int[] yPoints = new int[4];

        for(int i = 0; i < 4; i++) {
            xPoints[i] = GameSettings.descale(messageBoxPoints[i].getX());
            yPoints[i] = GameSettings.descale(messageBoxPoints[i].getY());
        }
        xPoints[0] = xPoints[0]+GameSettings.scale(10);
        yPoints[0] = yPoints[0]+GameSettings.scale(10);
        xPoints[1] = xPoints[1]-GameSettings.scale(10);
        yPoints[1] = yPoints[1]+GameSettings.scale(10);
        xPoints[2] = xPoints[2]-GameSettings.scale(10);
        yPoints[2] = yPoints[2]-GameSettings.scale(10);
        xPoints[3] = xPoints[3]+GameSettings.scale(10);
        yPoints[3] = yPoints[3]-GameSettings.scale(10);

        Polygon messageBox = new Polygon(xPoints, yPoints, 4);

        // Create a graphics context copy for drawing UI customizations safely
        Graphics2D g2d = (Graphics2D) g.create();
        // Enable anti-aliasing for smoother polygon edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Fill with transparent white (RGB: 255, 255, 255 with an Alpha of 150 out of 255)
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fill(messageBox);

        // 2. Draw the black outline
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(GameSettings.scale(4))); // Change this number to make it thicker or thinner
        g2d.draw(messageBox);

        g2d.dispose();
    }
}
