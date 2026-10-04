package gui.hud;

import engine.interactions.Interactable;
import engine.messages.MessageData;
import helpers.FontWrapper;
import helpers.GameSettings;
import helpers.ImageLoader;
import helpers.Point;
import engine.room.Room;
import helpers.QuadShapeDrawer;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link LayerUI} overlay that draws the heads-up display on top of a room view.
 * <p>
 * The HUD consists of:
 * <ul>
 *   <li>Left and right navigation arrows that rotate the player's view.</li>
 *   <li>A semi-transparent message box along the bottom of the screen, aligned with the
 *       back wall of the current view. Text is typed out one character at a time, and an
 *       optional name plate sits on the left or right of the box.</li>
 * </ul>
 * While the message box is showing, the arrows are replaced with their "X" (disabled)
 * versions and every mouse event is consumed, so nothing underneath can be clicked.
 * Each click finishes the typing, then moves on to the next message in the chain, and the
 * box disappears after the last one is clicked away.
 * <p>
 * Show a message with {@link #displayMessage(MessageData, Supplier)}. Anything else that reacts to the mouse
 * should check {@link #isBlockingInput()} first.
 * <p>
 * Install it on a {@link JLayer} and it will receive the layer's mouse events automatically.
 */
public class HudUI extends LayerUI<JComponent> {
    //-----ADJUSTMENT VARIABLES-----//

    /** The width and height, in unscaled pixels, of the left and right arrows. */
    private static final int ARROW_WIDTH = 120;

    /** Inset, in unscaled pixels, between the message box edges and the drawn box. Also spaces the arrows from the box. */
    private static final int MESSAGE_PADDING = 10;

    /** Fraction of the screen height the message box takes up (for example, {@code .33f} is 33%). */
    private static final float SIZE_RATIO = .30f;

    /** Inducates the name plate height for either side.*/
    private static final int NAME_PLATE_HEIGHT = 15;

    /** Font size, in unscaled pixels, of the message text. */
    private static final int MESSAGE_FONT_SIZE = 36;

    /** Space, in unscaled pixels, between the message box edge and the text. */
    private static final int TEXT_PADDING = 16;

    /** Milliseconds between each typed character. */
    private static final int TYPE_DELAY_MS = 33;

    //-----FONTS-----//

    /** The font used for both the message text and the name plate. */
    private static final FontWrapper TUFFY = new FontWrapper("/fonts/Tuffy.ttf", "Tuffy");
    private static final FontWrapper TUFFY_BOLD = new FontWrapper("/fonts/Tuffy_Bold.ttf", "Tuffy Bold");

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
    private MessageData.NamePosition namePos = MessageData.NamePosition.NONE;

    private final Rectangle leftArrowBounds = new Rectangle();
    private final Rectangle rightArrowBounds = new Rectangle();

    /** The four corners of the message box, already padded and scaled. Rebuilt by {@link #calculateGeometry()}. */
    private Polygon messageBoxPoints = new Polygon();

    /** Name plate on the left edge of the message box. Rebuilt by {@link #calculateGeometry()}. */
    private Polygon namePlateLeftPoints = new Polygon();

    /** Name plate on the right edge of the message box. Rebuilt by {@link #calculateGeometry()}. */
    private Polygon namePlateRightPoints = new Polygon();

    //-----MESSAGE STATE-----//

    /** The layer this UI is installed on, kept so the typing timer can repaint it. */
    private JLayer<?> layer;

    /** The message currently on screen, or {@code null} when none is showing. */
    private MessageData currentMessage;

    /** Supplies the next message in the chain; returns {@code null} when the chain is over. May be {@code null}. */
    private Supplier<MessageData> nextMessage;

    /** How many characters of the current message are revealed so far. */
    private int visibleChars;

    /** Reveals one more character every {@link #TYPE_DELAY_MS} milliseconds. Runs on the Swing thread. */
    private final Timer typeTimer = new Timer(TYPE_DELAY_MS, _ -> typeNextChar());

    /** True if the mouse press currently in progress started while a message was showing. */
    private boolean pressArmed;

    /** True from the click that closed a message until the mouse next moves or presses. */
    private boolean swallowClick;

    /** Cached font for the message text. Rebuilt by {@link #calculateGeometry()}. */
    private Font messageFont;

    /** Cached font for the name on the plate; sized to fit the plate. Cleared whenever the message or geometry changes. */
    private Font nameFont;

    /** Cached word-wrapped lines of the current message. Cleared whenever the message or geometry changes. */
    private List<String> wrappedLines;

    /** The text width, in pixels, that {@link #wrappedLines} was wrapped for. */
    private int wrappedWidth;

    /**
     * Constructs the HUD for the given room.
     *
     * @param room the room whose view this HUD controls; used to turn the view left or right, rebuild the screen, and calculate the message box position
     */
    public HudUI(Room room) {
        this.room = room;
        calculateGeometry();
    }

    //-----MESSAGES-----//

    /**
     * Shows a single message with no follow-up. Clicking it away closes the box.
     *
     * @param message the message to show
     * @see #displayMessage(MessageData, Supplier)
     */
    public void displayMessage(MessageData message) {
        displayMessage(message, null);
    }

    /**
     * Shows a message and types it out one character at a time.
     * <p>
     * While it is showing, the arrows are disabled and all mouse input is consumed. The first click
     * during typing reveals the rest of the text. The next click asks {@code next} for the following message.
     * If it returns {@code null} (or {@code next} is {@code null}), the box closes.
     * <p>
     * Safe to call from any thread; the work is moved onto the Swing thread if needed.
     *
     * @param first the first message to show; {@code null} closes any message that is showing
     * @param next  called each time a message is clicked away; returns the next message, or {@code null} when the chain is over
     */
    public void displayMessage(MessageData first, Supplier<MessageData> next) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> displayMessage(first, next));
            return;
        }
        if (first == null) {
            closeMessage();
            return;
        }
        this.nextMessage = next;
        beginMessage(first);
    }

    /**
     * Triggers an {@link Interactable} and shows its message, then follows its {@code nextTrigger}
     * chain one message per click until the chain ends.
     * <p>
     * Each interactable's {@link Interactable#trigger()} is called when it is reached, before its message
     * is read. Its {@link Interactable#lateTrigger()} is called after its message has been displayed, which
     * is when the player clicks that message away (before the next interactable is triggered). The first one
     * is triggered by this method, so callers should not call {@code trigger()} themselves. Interactables
     * with no message are still triggered, then get their {@code lateTrigger()} straight away (there is
     * nothing to wait for) and are skipped over.
     *
     * @param start the first interactable in the chain
     */
    public void displayInteraction(Interactable start) {
        Interactable first = triggerUntilMessage(start);
        if (first == null) {
            return;
        }
        Interactable[] current = {first};
        displayMessage(first.getMessage(), () -> {
            Interactable done = current[0];
            done.lateTrigger();
            current[0] = triggerUntilMessage(done.getNextTrigger());
            current[0] = triggerUntilMessage(done.getNextTrigger());
            return current[0] == null ? null : current[0].getMessage();
        });
    }

    /**
     * Triggers interactables down the chain until one has a message to show.
     *
     * @param i where to start in the chain
     * @return the first interactable with a message (already triggered), or {@code null} if the chain ran out
     */
    private static Interactable triggerUntilMessage(Interactable i) {
        while (i != null) {
            i.trigger();
            if (i.getMessage() != null) {
                return i;
            }
            i.lateTrigger(); // nothing will be displayed, so there is nothing to wait for
            i = i.getNextTrigger();
        }
        return null;
    }

    /**
     * Whether the HUD is currently blocking mouse input.
     * <p>
     * True while a message is showing, and also for the rest of the click that closed it, so that click
     * cannot fall through to whatever is underneath. Anything else that reacts to the mouse (clickable
     * objects, hover highlights) should do nothing while this returns true.
     *
     * @return true if other mouse handlers should ignore input right now
     */
    public boolean isBlockingInput() {
        return this.showMessage || this.swallowClick;
    }

    private void beginMessage(MessageData message) {
        this.currentMessage = message;
        String name = message.getName();
        this.namePos = (name == null || name.isBlank()) ? MessageData.NamePosition.NONE : message.getNamePos();
        this.visibleChars = 0;
        this.wrappedLines = null;
        this.nameFont = null;
        this.showMessage = true;
        this.typeTimer.restart();
        repaintLayer();
    }

    private void closeMessage() {
        this.typeTimer.stop();
        this.showMessage = false;
        this.currentMessage = null;
        this.nextMessage = null;
        this.namePos = MessageData.NamePosition.NONE;
        this.wrappedLines = null;
        this.nameFont = null;
        repaintLayer();
    }

    /** Called on a click: finish typing if still typing, otherwise move to the next message or close. */
    private void advanceMessage() {
        if (isTyping()) {
            this.visibleChars = currentText().length();
            this.typeTimer.stop();
            repaintLayer();
            return;
        }
        MessageData next = (this.nextMessage == null) ? null : this.nextMessage.get();
        if (next == null) {
            closeMessage();
        } else {
            beginMessage(next);
        }
    }

    private void typeNextChar() {
        this.visibleChars = Math.min(this.visibleChars + 1, currentText().length());
        if (this.visibleChars >= currentText().length()) {
            this.typeTimer.stop();
        }
        repaintLayer();
    }

    private boolean isTyping() {
        return this.currentMessage != null && this.visibleChars < currentText().length();
    }

    private String currentText() {
        String text = (this.currentMessage == null) ? null : this.currentMessage.getMessage();
        return (text == null) ? "" : text;
    }

    private void repaintLayer() {
        if (this.layer != null) {
            this.layer.repaint();
        }
    }

    //-----GEOMETRY-----//

    /**
     * Works out the message box corners and both name plates from the room's current view.
     * <p>
     * Call this whenever the view or the screen scale changes, so {@link #paint(Graphics, JComponent)}
     * does not have to repeat the math on every frame.
     */
    private void calculateGeometry() {
        Point[] boxPoints = room.getMessageBoxPoints(HudUI.SIZE_RATIO);

        int[] xPoints = getXPoints(boxPoints);
        int[] yPoints = getYPoints(boxPoints);

        int pad = GameSettings.scale(HudUI.MESSAGE_PADDING);
        xPoints[0] += pad;  yPoints[0] += pad;
        xPoints[1] -= pad;  yPoints[1] += pad;
        xPoints[2] -= pad;  yPoints[2] -= pad;
        xPoints[3] += pad;  yPoints[3] -= pad;

        messageBoxPoints = new Polygon(xPoints, yPoints, 4);

        int plate = GameSettings.scale(HudUI.NAME_PLATE_HEIGHT);
        int plateLong = GameSettings.scale(HudUI.NAME_PLATE_HEIGHT * 3);

        QuadShapeDrawer namePlateLeft = new QuadShapeDrawer(new Point(xPoints[0] - plate, yPoints[0]));
        namePlateLeft.drawLine(0, plateLong);
        namePlateLeft.drawLine(90, plate);
        namePlateLeft.drawLine(180, plateLong);
        Point[] arr = namePlateLeft.getPoints();
        this.namePlateLeftPoints = new Polygon(this.getXPoints(arr), this.getYPoints(arr), 4);

        QuadShapeDrawer namePlateRight = new QuadShapeDrawer(new Point(xPoints[1] - plate, yPoints[1] - plateLong));
        namePlateRight.drawLine(0, plateLong);
        namePlateRight.drawLine(90, plate);
        namePlateRight.drawLine(180, plateLong);
        arr = namePlateRight.getPoints();
        this.namePlateRightPoints = new Polygon(this.getXPoints(arr), this.getYPoints(arr), 4);

        // Text depends on the box size, so drop everything cached from the old geometry
        this.messageFont = TUFFY.getFont(GameSettings.scale(HudUI.MESSAGE_FONT_SIZE));
        this.nameFont = null;
        this.wrappedLines = null;
    }

    private int[] getXPoints(Point[] points) {
        int[] xPoints = new int[4];
        for (int i = 0; i < 4; i++) {
            xPoints[i] = GameSettings.descale(points[i].getX());
        }
        return xPoints;
    }

    private int[] getYPoints(Point[] points) {
        int[] yPoints = new int[4];
        for (int i = 0; i < 4; i++) {
            yPoints[i] = GameSettings.descale(points[i].getY());
        }
        return yPoints;
    }

    //-----LAYER UI-----//

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        this.layer = (JLayer<?>) c;
        // Tell the JLayer to deliver mouse events (clicks and movement) to this UI
        this.layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        this.typeTimer.stop();
        ((JLayer<?>) c).setLayerEventMask(0);
        this.layer = null;
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

        // Message box, name plate and text: only while a message is showing
        if (this.showMessage) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(GameSettings.scale(4)));

            drawShape(g2d, this.messageBoxPoints);

            Polygon plate = activeNamePlate();
            if (plate != null) {
                drawShape(g2d, plate);
                drawName(g2d, plate);
            }

            drawMessageText(g2d);
            g2d.dispose();
        }

        // Arrows
        int[] xPoints = this.messageBoxPoints.xpoints;
        int[] yPoints = this.messageBoxPoints.ypoints;
        int arrowSize = GameSettings.scale(HudUI.ARROW_WIDTH);
        int arrowGap = GameSettings.scale(HudUI.MESSAGE_PADDING * 2);

        // The arrows sit in the same spot whether or not a message is showing
        int leftX = xPoints[0] - arrowGap - arrowSize;
        int rightX = xPoints[1] + arrowGap;
        int arrowY = yPoints[0];

        Graphics2D g2d = (Graphics2D) g.create();
        if (this.showMessage) {
            // Disabled: draw the X versions, and leave the click areas empty so they can't be hit
            leftArrowBounds.setBounds(0, 0, 0, 0);
            rightArrowBounds.setBounds(0, 0, 0, 0);
            g2d.drawImage(HudUI.LEFT_ARROW_X.getImage(), leftX, arrowY, arrowSize, arrowSize, null);
            g2d.drawImage(HudUI.RIGHT_ARROW_X.getImage(), rightX, arrowY, arrowSize, arrowSize, null);
        } else {
            leftArrowBounds.setBounds(leftX, arrowY, arrowSize, arrowSize);
            rightArrowBounds.setBounds(rightX, arrowY, arrowSize, arrowSize);
            g2d.drawImage(HudUI.LEFT_ARROW.getImage(), leftX, arrowY, arrowSize, arrowSize, null);
            g2d.drawImage(HudUI.RIGHT_ARROW.getImage(), rightX, arrowY, arrowSize, arrowSize, null);
        }
        g2d.dispose();
    }

    /**
     * Fills a shape with the HUD's semi-transparent white and outlines it in black.
     * The caller sets the stroke width and anti-aliasing beforehand.
     *
     * @param g2d the graphics context to draw with
     * @param shape the polygon to fill and outline
     */
    private void drawShape(Graphics2D g2d, Polygon shape) {
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fill(shape);
        g2d.setColor(Color.BLACK);
        g2d.draw(shape);
    }

    /** The name plate the current message uses, or {@code null} if it has none. */
    private Polygon activeNamePlate() {
        return switch (this.namePos) {
            case LEFT -> this.namePlateLeftPoints;
            case RIGHT -> this.namePlateRightPoints;
            case NONE -> null;
        };
    }

    /**
     * Draws the sender's name centered on the plate, shrinking the font until it fits.
     *
     * @param g2d the graphics context to draw with
     * @param plate the name plate polygon to center the name in
     */
    private void drawName(Graphics2D g2d, Polygon plate) {
        String name = this.currentMessage.getName();
        Rectangle b = plate.getBounds();
        int inset = GameSettings.scale(4);
        int maxWidth = b.width - inset * 2;
        int maxHeight = b.height - inset * 2;
        if (maxWidth <= 0 || maxHeight <= 0) {
            return;
        }

        if (this.nameFont == null) {
            int size = maxHeight;
            Font font = TUFFY_BOLD.getFont(size);
            FontMetrics fm = g2d.getFontMetrics(font);
            while (size > 6 && (fm.stringWidth(name) > maxWidth || fm.getHeight() > maxHeight)) {
                size--;
                font = TUFFY_BOLD.getFont(size);
                fm = g2d.getFontMetrics(font);
            }
            this.nameFont = font;
        }

        g2d.setFont(this.nameFont);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        int x = b.x + (b.width - fm.stringWidth(name)) / 2;
        int y = b.y + (b.height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(name, x, y);
    }

    /**
     * Draws the revealed part of the current message, word-wrapped to fit inside the message box.
     * Lines that would not fit inside the box are not drawn.
     *
     * @param g2d the graphics context to draw with
     */
    private void drawMessageText(Graphics2D g2d) {
        int[] xs = this.messageBoxPoints.xpoints;
        int[] ys = this.messageBoxPoints.ypoints;
        int pad = GameSettings.scale(HudUI.TEXT_PADDING);

        // Largest upright rectangle inside the (possibly slanted) box
        int left = Math.max(xs[0], xs[3]) + pad;
        int right = Math.min(xs[1], xs[2]) - pad;
        int top = Math.max(ys[0], ys[1]) + pad;
        int bottom = Math.min(ys[2], ys[3]) - pad;
        int maxWidth = right - left;
        if (maxWidth <= 0 || bottom <= top) {
            return;
        }

        g2d.setFont(this.messageFont);
        FontMetrics fm = g2d.getFontMetrics();

        if (this.wrappedLines == null || this.wrappedWidth != maxWidth) {
            this.wrappedLines = wrap(currentText(), fm, maxWidth);
            this.wrappedWidth = maxWidth;
        }

        g2d.setColor(Color.BLACK);
        int lineHeight = fm.getHeight();
        int y = top + fm.getAscent();
        int remaining = this.visibleChars;
        for (String line : this.wrappedLines) {
            if (remaining <= 0 || y + fm.getDescent() > bottom) {
                break;
            }
            g2d.drawString(line.substring(0, Math.min(line.length(), remaining)), left, y);
            // +1 for the space or newline that was removed where the text wrapped
            remaining -= line.length() + 1;
            y += lineHeight;
        }
    }

    /**
     * Splits text into lines no wider than {@code maxWidth}, breaking at spaces and at newlines.
     * Wrapping the whole message up front means words never jump lines while they are being typed.
     *
     * @param text the full message
     * @param fm the metrics of the font the text will be drawn in
     * @param maxWidth the widest a line may be, in pixels
     * @return the wrapped lines, in order
     */
    private static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ", -1)) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (line.isEmpty() || fm.stringWidth(candidate) <= maxWidth) {
                    line = new StringBuilder(candidate);
                } else {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                }
            }
            lines.add(line.toString());
        }
        return lines;
    }

    //-----MOUSE-----//

    @Override
    protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> layer) {
        int id = e.getID();

        if (id == MouseEvent.MOUSE_PRESSED) {
            // Only a press that started while the message was up may advance it. This stops
            // the same click that opened a message from also closing it.
            this.pressArmed = this.showMessage;
            this.swallowClick = false;
        }

        if (this.showMessage) {
            if (id == MouseEvent.MOUSE_RELEASED && this.pressArmed) {
                this.pressArmed = false;
                this.swallowClick = true; // the click that follows this release must not reach the arrows
                advanceMessage();
            }
            e.consume();
            return;
        }

        if (id != MouseEvent.MOUSE_CLICKED) {
            return;
        }
        if (this.swallowClick) {
            e.consume();
            return;
        }

        // Event coordinates are relative to whichever child got the event,
        // so convert them to the layer's coordinate space
        java.awt.Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), layer);

        if (leftArrowBounds.contains(p)) {
            this.room.lookLeft();
            this.room.rebuildScreen();
            calculateGeometry();
            layer.repaint();
            e.consume();
        } else if (rightArrowBounds.contains(p)) {
            this.room.lookRight();
            this.room.rebuildScreen();
            calculateGeometry();
            layer.repaint();
            e.consume();
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> layer) {
        if (this.showMessage) {
            e.consume(); // no hover effects underneath while a message is up
        } else {
            this.swallowClick = false; // the closing click is over once the mouse moves
        }
    }

}