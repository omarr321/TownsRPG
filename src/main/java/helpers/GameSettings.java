package helpers;

import javax.swing.*;
import java.awt.*;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
    /** The tip of the cursor, in pixels of the original cursor image files (before any resizing). */
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

    /**
     * Returns the game's custom cursor for the given state.
     * <p>
     * Cursors are built lazily on first request and then cached, so repeated calls
     * (for example on every mouse move) reuse the same {@link Cursor} instead of creating
     * a new custom cursor each time.
     *
     * @param type which cursor to get: normal, clicked or highlighted
     * @return the cached custom cursor for {@code type}, never {@code null}
     */
    public static Cursor getCursor(CursorType type) {
        // Cached so hovering does not build a new custom cursor on every mouse move
        return CURSOR_CACHE.computeIfAbsent(type, t -> {
            Image cursorImage = switch (t) {
                case CURSOR -> GameSettings.CURSOR.getImage();
                case CURSOR_CLICKED -> GameSettings.CURSOR_CLICKED.getImage();
                case CURSOR_HIGHLIGHTED -> GameSettings.CURSOR_HIGHLIGHTED.getImage();
            };
            return buildCursor(cursorImage);
        });
    }

    /**
     * Builds a custom cursor from an image, resized to a size the operating system supports.
     * <p>
     * The OS only draws cursors up to its own maximum size, and the hotspot is not adjusted when
     * the image is resized. So the image is shrunk here, and the hotspot ({@link #CURSOR_POINT}) is
     * shrunk by the same amount, which keeps the click point under the tip of the drawn cursor.
     *
     * @param src the full-size cursor image
     * @return a custom cursor whose click point matches the tip of the drawn image
     */
    private static Cursor buildCursor(Image src) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w <= 0 || h <= 0) {
            // Size unknown, so there is nothing to scale against; use the image as it is
            return toolkit.createCustomCursor(src, CURSOR_POINT, "Custom Cursor");
        }

        Dimension best = toolkit.getBestCursorSize(w, h);
        // To see what is going on, uncomment:
        // System.out.println("cursor image " + w + "x" + h + ", OS cursor size " + best.width + "x" + best.height);
        if (best.width <= 0 || best.height <= 0) {
            return Cursor.getDefaultCursor(); // custom cursors are not supported on this system
        }

        // Shrink to fit (never enlarge), keeping the shape; the rest of the canvas stays transparent
        double ratio = Math.min(1.0, Math.min(best.width / (double) w, best.height / (double) h));
        int drawW = Math.max(1, (int) Math.round(w * ratio));
        int drawH = Math.max(1, (int) Math.round(h * ratio));

        BufferedImage fitted = new BufferedImage(best.width, best.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fitted.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, drawW, drawH, null);
        g.dispose();

        Point hotspot = new Point(
                Math.min(best.width - 1, (int) Math.round(CURSOR_POINT.x * ratio)),
                Math.min(best.height - 1, (int) Math.round(CURSOR_POINT.y * ratio)));
        return toolkit.createCustomCursor(fitted, hotspot, "Custom Cursor");
    }

    /**
     * The cursor states the game can show. Each constant maps to its own cursor image
     * in {@link GameSettings}.
     */
    public enum CursorType {
        /** The default cursor, shown when nothing special is happening. */
        CURSOR,
        /** Shown briefly after a mouse press. */
        CURSOR_CLICKED,
        /** Shown while the mouse is over an interactable object. */
        CURSOR_HIGHLIGHTED
    }

    /**
     * Gives a frame the game cursor, with a short clicked flash on every mouse press.
     * <p>
     * The cursor switches to {@link CursorType#CURSOR_CLICKED} on press and returns to
     * {@link CursorType#CURSOR} after 120 ms. This method does not show the highlighted
     * cursor. Use {@link #setInteractableMouse(JComponent, Predicate)} for a panel that
     * needs hover feedback over interactable objects.
     *
     * @param panel the frame that should use the game cursor
     */
    public static void setCustomMouse(JFrame panel) {
        panel.setCursor(getCursor(CursorType.CURSOR));
        Timer revert = new Timer(120, _ -> panel.setCursor(getCursor(CursorType.CURSOR)));
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

        Timer revert = new Timer(120, _ -> surface.setCursor(restingCursor(surface, overInteractable)));
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