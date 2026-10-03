package gui.hud;

import helpers.GameSettings;
import helpers.ImageLoader;
import helpers.Point;
import engine.room.Room;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * A {@link LayerUI} overlay that draws the heads-up display on top of a room view.
 * <p>
 * The HUD consists of:
 * <ul>
 *   <li>Left and right navigation arrows that rotate the player's view.</li>
 *   <li>A semi-transparent message box along the bottom of the screen, aligned with the
 *       back wall of the current view.</li>
 * </ul>
 * While the message box is showing, the arrows are replaced with their "X" (disabled)
 * versions and clicking them does nothing.
 * <p>
 * Install it on a {@link JLayer} and it will receive the layer's mouse events automatically.
 */
public class HudUI extends LayerUI<JComponent> {
    ///-----ADJUSTMENT VARIABLES-----//

    /** The width and height, in unscaled pixels, of the left and right arrows. */
    private static final int ARROW_WIDTH = 120;

    /** Inset, in unscaled pixels, between the message box edges and the drawn box. Also spaces the arrows from the box. */
    private static final int MESSAGE_PADDING = 10;

    /** Fraction of the screen height the message box takes up (for example, {@code .33f} is 33%). */
    private static final float SIZE_RATIO = .30f;

    //-----UI IMAGES-----//

    /** Right arrow shown when navigation is available. */
    private static final ImageLoader RIGHT_ARROW = new ImageLoader("/images/UI/arrow_right.png");
    /** Left arrow shown when navigation is available. */
    private static final ImageLoader LEFT_ARROW = new ImageLoader("/images/UI/arrow_left.png");
    /** Right arrow shown when navigation is disabled (message showing). */
    private static final ImageLoader RIGHT_ARROW_X = new ImageLoader("/images/UI/arrow_right_x.png");
    /** Left arrow shown when navigation is disabled (message showing). */
    private static final ImageLoader LEFT_ARROW_X = new ImageLoader("/images/UI/arrow_left_x.png");

    private final Room room;
    private boolean showMessage = false;

    private final Rectangle leftArrowBounds = new Rectangle();
    private final Rectangle rightArrowBounds = new Rectangle();

    /**
     * Constructs the HUD for the given room.
     *
     * @param room the room whose view this HUD controls; used to turn the view left or right, rebuild the screen, and calculate the message box position
     */
    public HudUI(Room room) {
        this.room = room;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        // Tell the JLayer to deliver mouse events to this UI
        ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        ((JLayer<?>) c).setLayerEventMask(0);
        super.uninstallUI(c);
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
        xPoints[0] = xPoints[0]+GameSettings.scale(HudUI.MESSAGE_PADDING);
        yPoints[0] = yPoints[0]+GameSettings.scale(HudUI.MESSAGE_PADDING);
        xPoints[1] = xPoints[1]-GameSettings.scale(HudUI.MESSAGE_PADDING);
        yPoints[1] = yPoints[1]+GameSettings.scale(HudUI.MESSAGE_PADDING);
        xPoints[2] = xPoints[2]-GameSettings.scale(HudUI.MESSAGE_PADDING);
        yPoints[2] = yPoints[2]-GameSettings.scale(HudUI.MESSAGE_PADDING);
        xPoints[3] = xPoints[3]+GameSettings.scale(HudUI.MESSAGE_PADDING);
        yPoints[3] = yPoints[3]-GameSettings.scale(HudUI.MESSAGE_PADDING);

        // Create a graphics context copy for drawing UI customizations safely
        Graphics2D g2d = (Graphics2D) g.create();

        if (this.showMessage) {
            Polygon messageBox = new Polygon(xPoints, yPoints, 4);
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

        g2d = (Graphics2D) g.create();
        Point LeftArrowStartPoint = new Point(xPoints[0]-GameSettings.scale(HudUI.MESSAGE_PADDING*2), yPoints[0]);
        int arrowSize = GameSettings.scale(HudUI.ARROW_WIDTH);
        if (this.showMessage) {
            leftArrowBounds.setBounds(0, 0, 0, 0);
            g2d.drawImage(HudUI.LEFT_ARROW_X.getImage(),
                    leftArrowBounds.x, leftArrowBounds.y, arrowSize, arrowSize, null);
        } else {
            leftArrowBounds.setBounds(LeftArrowStartPoint.getX() - arrowSize, LeftArrowStartPoint.getY(), arrowSize, arrowSize);
            g2d.drawImage(HudUI.LEFT_ARROW.getImage(),
                    leftArrowBounds.x, leftArrowBounds.y, arrowSize, arrowSize, null);
        }
        g2d.dispose();

        g2d = (Graphics2D) g.create();
        Point RightArrowStartPoint = new Point(xPoints[1]+GameSettings.scale(HudUI.MESSAGE_PADDING*2), yPoints[0]);
        if (this.showMessage) {
            rightArrowBounds.setBounds(0, 0, 0, 0);
            g2d.drawImage(HudUI.RIGHT_ARROW_X.getImage(),
                    rightArrowBounds.x, rightArrowBounds.y, arrowSize, arrowSize, null);
        } else {
            rightArrowBounds.setBounds(RightArrowStartPoint.getX(), RightArrowStartPoint.getY(), arrowSize, arrowSize);
            g2d.drawImage(HudUI.RIGHT_ARROW.getImage(),
                    rightArrowBounds.x, rightArrowBounds.y, arrowSize, arrowSize, null);
        }
        g2d.dispose();
    }

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> layer) {
        if (e.getID() != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        // Event coordinates are relative to whichever child got the event,
        // so convert them to the layer's coordinate space
        java.awt.Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), layer);

        if (leftArrowBounds.contains(p)) {
            this.room.lookLeft();
            this.room.rebuildScreen();
            layer.repaint();
            e.consume();
        } else if (rightArrowBounds.contains(p)) {
            this.room.lookRight();
            this.room.rebuildScreen();
            layer.repaint();
            e.consume();
        }
    }

}
