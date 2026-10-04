package helpers;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Static setting of the game that is shared across the engine.
 */
public class GameSettings {
    private static final ImageLoader CURSOR = new ImageLoader("/images/UI/cursors/cursor.png");
    private static final ImageLoader CURSOR_CLICKED = new ImageLoader("/images/UI/cursors/cursor-clicked.png");
    private static final ImageLoader CURSOR_HIGHLIGHTED = new ImageLoader("/images/UI/cursors/cursor-highlighted.png");
    private static final Point CURSOR_POINT = new Point(20, 14);
    private static final Map<CursorType, Cursor> CURSOR_CACHE = new EnumMap<>(CursorType.class);

    /** Whether the game runs in full-screen mode instead of a window. */
    public static boolean fullScreen = false;
    /** The width of the screen or game window in pixels. */
    public static int screenWidth = 0;
    /** The height of the screen or game window in pixels. */
    public static int screenHeight = 0;
    /**
     * The screen width that pixel sizes (like light distances) are written for.
     * Set this to the resolution you tuned your lights at; anything given in pixels
     * is scaled from this width to the real screen width.
     */
    public static final int REFERENCE_WIDTH = 1920;

    /**
     * How much bigger (or smaller) the current screen is than the reference screen.
     * Based on width, the same way RoomPoints sizes the room, so lights and rooms scale together.
     * @return 1 if the screen size hasn't been set yet.
     */
    public static float getScale() {
        if (screenWidth <= 0) {
            return 1f;
        }
        return screenWidth / (float) REFERENCE_WIDTH;
    }

    /**
     * Converts a size in reference-screen pixels to real screen pixels.
     * Values of 0 or less are returned unchanged, and positive values never scale below 1.
     *
     * @param value the size in reference-screen pixels
     * @return the size in real screen pixels, or {@code value} itself if it is 0 or less
     */
    public static int scale(int value) {
        if (value <= 0) {
            return value;
        }
        return Math.max(1, Math.round(value * getScale()));
    }

    /**
     * Converts a size in real screen pixels to reference-screen pixels.
     * Values of 0 or less are returned unchanged, and positive values never scale below 1.
     *
     * @param value the size in real screen pixels
     * @return the size in reference-screen pixels, or {@code value} itself if it is 0 or less
     */
    public static int descale(int value) {
        if (value <= 0) {
            return value;
        }
        float scale = getScale();
        return Math.max(1, Math.round(value / scale));
    }

    public static Cursor getCursor(CursorType type) {
        // Cached so hovering does not build a new custom cursor on every mouse move
        return CURSOR_CACHE.computeIfAbsent(type, t -> {
            Image cursorImage = switch (t) {
                case CURSOR -> GameSettings.CURSOR.getImage();
                case CURSOR_CLICKED -> GameSettings.CURSOR_CLICKED.getImage();
                case CURSOR_HIGHLIGHTED -> GameSettings.CURSOR_HIGHLIGHTED.getImage();
            };
            return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, GameSettings.CURSOR_POINT, "Custom Cursor");
        });
    }

    public enum CursorType {
        CURSOR,
        CURSOR_CLICKED,
        CURSOR_HIGHLIGHTED
    }

    public static void setCustomMouse(JFrame panel) {
        panel.setCursor(getCursor(CursorType.CURSOR));
        Timer revert = new Timer(120, e -> panel.setCursor(getCursor(CursorType.CURSOR)));
        revert.setRepeats(false);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.setCursor(getCursor(CursorType.CURSOR_CLICKED));
                revert.restart();
            }
        });
    }

    /**
     * Gives a panel the game cursors: normal, highlighted while the mouse is over an
     * interactable object, and a short clicked flash on every press.
     * Use this on a panel that has its own mouse listeners, because mouse events are not
     * passed up to the frame's listener from {@link #setCustomMouse(JFrame)}.
     *
     * @param surface the panel the objects are drawn on
     * @param overInteractable returns true if the given point (in the panel's coordinates) is over an interactable object
     */
    public static void setInteractableMouse(JComponent surface, Predicate<Point> overInteractable) {
        surface.setCursor(getCursor(CursorType.CURSOR));

        Timer revert = new Timer(120, e -> surface.setCursor(restingCursor(surface, overInteractable)));
        revert.setRepeats(false);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                surface.setCursor(getCursor(CursorType.CURSOR_CLICKED));
                revert.restart();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                update(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                update(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                update(e);
            }

            private void update(MouseEvent e) {
                if (revert.isRunning()) {
                    return; // let the click flash finish first
                }
                boolean over = overInteractable.test(e.getPoint());
                surface.setCursor(getCursor(over ? CursorType.CURSOR_HIGHLIGHTED : CursorType.CURSOR));
            }
        };
        surface.addMouseListener(adapter);
        surface.addMouseMotionListener(adapter);
    }

    private static Cursor restingCursor(JComponent surface, Predicate<Point> overInteractable) {
        Point mouse = surface.getMousePosition();
        boolean over = mouse != null && overInteractable.test(mouse);
        return getCursor(over ? CursorType.CURSOR_HIGHLIGHTED : CursorType.CURSOR);
    }

    //For Unit Testing
    // Private constructor to hide the implicit public one and prevent instantiation
    private GameSettings() {
        throw new UnsupportedOperationException("GameSettings.java Can not be instantiation.");
    }
}
