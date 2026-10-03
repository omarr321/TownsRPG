package gui.hud;

import helpers.GameSettings;
import helpers.Point;
import engine.room.Room;
import engine.room.parts.Ceiling;
import engine.room.parts.Floor;
import engine.room.parts.RoomPoints;
import engine.room.parts.Wall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class HudUITest {

    /**
     * A real Room that counts the calls HudUI makes to it. Every override
     * still delegates to the real implementation.
     */
    private static class RecordingRoom extends Room {
        int lookLeftCalls = 0;
        int lookRightCalls = 0;
        int rebuildCalls = 0;
        int messageBoxPointsCalls = 0;

        RecordingRoom(Floor floor, Ceiling ceiling, RoomPoints points) {
            super(floor, ceiling, points);
        }

        @Override
        public void lookLeft() {
            lookLeftCalls++;
            super.lookLeft();
        }

        @Override
        public void lookRight() {
            lookRightCalls++;
            super.lookRight();
        }

        @Override
        public void rebuildScreen() {
            rebuildCalls++;
            super.rebuildScreen();
        }

        @Override
        public Point[] getMessageBoxPoints(float sizeRatio) {
            messageBoxPointsCalls++;
            return super.getMessageBoxPoints(sizeRatio);
        }
    }

    private RecordingRoom room;
    private Floor floor;
    private Ceiling ceiling;
    private Wall[] walls;

    private HudUI hud;
    private JLayer<JComponent> layer;
    private BufferedImage canvas;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 1920;
        GameSettings.screenHeight = 1080;

        floor = new Floor(Color.GRAY);
        ceiling = new Ceiling(Color.WHITE);
        walls = new Wall[]{new Wall(Color.RED), new Wall(Color.GREEN), new Wall(Color.BLUE), new Wall(Color.YELLOW)};
        room = completeRoom();

        hud = new HudUI(room);
        layer = new JLayer<>(new JPanel(), hud);
        layer.setSize(GameSettings.screenWidth, GameSettings.screenHeight);
        layer.getView().setSize(GameSettings.screenWidth, GameSettings.screenHeight);

        canvas = new BufferedImage(GameSettings.screenWidth, GameSettings.screenHeight,
                BufferedImage.TYPE_INT_ARGB);
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    // ---------- helpers ----------

    /** A room with all four walls set, looking at wall 1 (so the room is "completed"). */
    private RecordingRoom completeRoom() {
        RecordingRoom room = new RecordingRoom(floor, ceiling, new RoomPoints(0.5, 0.5, 30, 2));
        for (int i = 0; i < 4; i++) {
            room.setWall(walls[i], i);
        }
        room.setLookingIndex(1);
        return room;
    }

    private void paintHud() {
        Graphics2D g = canvas.createGraphics();
        try {
            hud.paint(g, layer);
        } finally {
            g.dispose();
        }
    }

    private void setShowMessage(boolean value) throws Exception {
        Field f = HudUI.class.getDeclaredField("showMessage");
        f.setAccessible(true);
        f.setBoolean(hud, value);
    }

    private Rectangle getBounds(String fieldName) throws Exception {
        Field f = HudUI.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (Rectangle) f.get(hud);
    }

    private MouseEvent mouse(int id, int x, int y) {
        return new MouseEvent(layer, id, System.currentTimeMillis(), 0, x, y, 1, false);
    }

    private static int cx(Rectangle r) { return r.x + r.width / 2; }
    private static int cy(Rectangle r) { return r.y + r.height / 2; }

    private boolean hasNonTransparentPixel() {
        for (int x = 0; x < canvas.getWidth(); x += 4) {
            for (int y = 0; y < canvas.getHeight(); y += 4) {
                if ((canvas.getRGB(x, y) >>> 24) != 0) return true;
            }
        }
        return false;
    }

    // ---------- construction / install ----------

    @Test
    void createHudUI() {
        assertNotNull(new HudUI(this.room));
    }

    @Test
    void installUI_enablesMouseEvents() {
        // JLayer calls installUI when the UI is set in its constructor.
        assertEquals(AWTEvent.MOUSE_EVENT_MASK, layer.getLayerEventMask());
    }

    @Test
    void uninstallUI_clearsEventMask() {
        layer.setUI(null);
        assertEquals(0, layer.getLayerEventMask());
    }

    // ---------- paint ----------

    @Test
    void paint_withZeroSize_doesNotQueryRoom() {
        layer.setSize(0, 0);
        paintHud();
        assertEquals(0, room.messageBoxPointsCalls);
    }

    @Test
    void paint_requestsMessageBoxPointsFromRoom() {
        paintHud();
        assertTrue(room.messageBoxPointsCalls > 0);
    }

    @Test
    void paint_withMessageHidden_setsArrowBoundsToScaledArrowSize() throws Exception {
        paintHud();

        int expected = GameSettings.scale(120); // HudUI.ARROW_WIDTH
        Rectangle left = getBounds("leftArrowBounds");
        Rectangle right = getBounds("rightArrowBounds");

        assertEquals(expected, left.width);
        assertEquals(expected, left.height);
        assertEquals(expected, right.width);
        assertEquals(expected, right.height);
    }

    @Test
    void paint_leftArrowIsLeftOfRightArrow() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");
        Rectangle right = getBounds("rightArrowBounds");
        assertTrue(left.x + left.width <= right.x, "arrows should not overlap");
    }

    @Test
    void paint_withMessageShown_clearsArrowBounds() throws Exception {
        paintHud();                 // populate bounds first
        setShowMessage(true);
        paintHud();

        assertTrue(getBounds("leftArrowBounds").isEmpty());
        assertTrue(getBounds("rightArrowBounds").isEmpty());
    }

    @Test
    void paint_withMessageHidden_drawsSomething() {
        paintHud();
        assertTrue(hasNonTransparentPixel(), "expected arrows to draw pixels");
    }

    @Test
    void paint_calledTwice_isStable() throws Exception {
        paintHud();
        Rectangle first = new Rectangle(getBounds("leftArrowBounds"));
        paintHud();
        assertEquals(first, getBounds("leftArrowBounds"));
    }

    // ---------- processMouseEvent ----------

    @Test
    void click_onLeftArrow_looksLeftRebuildsAndConsumes() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");
        MouseEvent e = mouse(MouseEvent.MOUSE_CLICKED, cx(left), cy(left));

        hud.processMouseEvent(e, layer);

        assertEquals(1, room.lookLeftCalls);
        assertEquals(1, room.rebuildCalls);
        assertEquals(0, room.lookRightCalls);
        assertSame(walls[0], room.getLookingWall(), "started on wall 1, left should be wall 0");
        assertTrue(e.isConsumed());
    }

    @Test
    void click_onRightArrow_looksRightRebuildsAndConsumes() throws Exception {
        paintHud();
        Rectangle right = getBounds("rightArrowBounds");
        MouseEvent e = mouse(MouseEvent.MOUSE_CLICKED, cx(right), cy(right));

        hud.processMouseEvent(e, layer);

        assertEquals(1, room.lookRightCalls);
        assertEquals(1, room.rebuildCalls);
        assertEquals(0, room.lookLeftCalls);
        assertSame(walls[2], room.getLookingWall(), "started on wall 1, right should be wall 2");
        assertTrue(e.isConsumed());
    }

    @Test
    void click_leftThenRight_returnsToOriginalWall() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");
        Rectangle right = getBounds("rightArrowBounds");

        hud.processMouseEvent(mouse(MouseEvent.MOUSE_CLICKED, cx(left), cy(left)), layer);
        hud.processMouseEvent(mouse(MouseEvent.MOUSE_CLICKED, cx(right), cy(right)), layer);

        assertSame(walls[1], room.getLookingWall());
        assertEquals(2, room.rebuildCalls);
    }

    @Test
    void click_leftFourTimes_wrapsAroundToStartingWall() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");

        for (int i = 0; i < 4; i++) {
            hud.processMouseEvent(mouse(MouseEvent.MOUSE_CLICKED, cx(left), cy(left)), layer);
        }

        assertSame(walls[1], room.getLookingWall());
        assertEquals(4, room.lookLeftCalls);
    }

    @Test
    void click_leftFromWallZero_wrapsToWallThree() throws Exception {
        room.setLookingIndex(0);
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");

        hud.processMouseEvent(mouse(MouseEvent.MOUSE_CLICKED, cx(left), cy(left)), layer);

        assertSame(walls[3], room.getLookingWall());
    }

    @Test
    void click_rightFromWallThree_wrapsToWallZero() throws Exception {
        room.setLookingIndex(3);
        paintHud();
        Rectangle right = getBounds("rightArrowBounds");

        hud.processMouseEvent(mouse(MouseEvent.MOUSE_CLICKED, cx(right), cy(right)), layer);

        assertSame(walls[0], room.getLookingWall());
    }

    @Test
    void click_outsideArrows_doesNothing() throws Exception {
        paintHud();
        // Top-center of the screen: well clear of both arrows.
        MouseEvent e = mouse(MouseEvent.MOUSE_CLICKED, GameSettings.screenWidth / 2, 5);

        hud.processMouseEvent(e, layer);

        assertEquals(0, room.lookLeftCalls);
        assertEquals(0, room.lookRightCalls);
        assertEquals(0, room.rebuildCalls);
        assertFalse(e.isConsumed());
    }

    @Test
    void nonClickEvents_areIgnored() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");

        for (int id : new int[]{MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
                MouseEvent.MOUSE_ENTERED, MouseEvent.MOUSE_EXITED}) {
            MouseEvent e = mouse(id, cx(left), cy(left));
            hud.processMouseEvent(e, layer);
            assertFalse(e.isConsumed(), "event id " + id + " should be ignored");
        }

        assertEquals(0, room.lookLeftCalls);
        assertEquals(0, room.rebuildCalls);
    }

    @Test
    void click_whileMessageShown_doesNotNavigate() throws Exception {
        paintHud();
        Rectangle left = getBounds("leftArrowBounds");
        int x = cx(left), y = cy(left);

        setShowMessage(true);
        paintHud();                 // bounds are now zeroed

        MouseEvent e = mouse(MouseEvent.MOUSE_CLICKED, x, y);
        hud.processMouseEvent(e, layer);

        assertEquals(0, room.lookLeftCalls);
        assertEquals(0, room.lookRightCalls);
        assertSame(walls[1], room.getLookingWall());
        assertFalse(e.isConsumed());
    }

    @Test
    void click_beforeFirstPaint_doesNothing() {
        MouseEvent e = mouse(MouseEvent.MOUSE_CLICKED, 0, 0);
        hud.processMouseEvent(e, layer);

        assertEquals(0, room.lookLeftCalls);
        assertEquals(0, room.lookRightCalls);
        assertFalse(e.isConsumed());
    }

    @Test
    void click_fromChildComponent_convertsToLayerSpace() throws Exception {
        paintHud();
        Rectangle right = getBounds("rightArrowBounds");

        JComponent view = layer.getView();
        MouseEvent e = new MouseEvent(view, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, cx(right), cy(right), 1, false);

        hud.processMouseEvent(e, layer);

        assertEquals(1, room.lookRightCalls);
        assertTrue(e.isConsumed());
    }
}