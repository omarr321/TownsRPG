package gui.lighting;

import gui.lighting.LightBlocker.LightTag;
import gui.lighting.LightPoint.LightShape;
import helpers.GameSettings;
import helpers.Point;
import engine.room.objects.InteractableObj;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LightingLayerUITest {
    private static final int SIZE = 50;
    private static final int BLUE = 0x0000FF;

    @BeforeEach
    void setUp() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    /** Paints a solid blue panel wrapped in a JLayer that uses the given UI, and returns what was drawn. */
    private static BufferedImage render(LightingLayerUI ui) {
        JPanel view = new JPanel();
        view.setBackground(Color.BLUE);
        JLayer<JComponent> layer = new JLayer<>(view, ui);
        layer.setSize(SIZE, SIZE);
        layer.doLayout();

        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        layer.print(g);
        g.dispose();
        return image;
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    private static LightMgmt fullAmbient() {
        return new LightMgmt(Color.WHITE, 1f);
    }

    // ---- getters / setters ----

    @Test
    void constructorStoresLightMgmt() {
        LightMgmt mgmt = fullAmbient();

        assertSame(mgmt, new LightingLayerUI(mgmt).getLightMgmt());
    }

    @Test
    void setLightMgmtReplacesIt() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        LightMgmt other = new LightMgmt();

        ui.setLightMgmt(other);

        assertSame(other, ui.getLightMgmt());
    }

    @Test
    void debugOverlaysAreOffByDefault() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());

        assertFalse(ui.isShowBlockers());
        assertFalse(ui.isShowLights());
    }

    @Test
    void showBlockersAndShowLightsCanBeToggled() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());

        ui.setShowBlockers(true);
        ui.setShowLights(true);
        assertTrue(ui.isShowBlockers());
        assertTrue(ui.isShowLights());

        ui.setShowBlockers(false);
        ui.setShowLights(false);
        assertFalse(ui.isShowBlockers());
        assertFalse(ui.isShowLights());
    }

    @Test
    void setInteractablesAcceptsNull() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setShowInteractable(true);
        ui.setInteractables(null);

        assertDoesNotThrow(() -> render(ui));
    }

    // ---- painting ----

    @Test
    void paintWithoutLightMgmtDrawsTheViewUnchanged() {
        BufferedImage image = render(new LightingLayerUI(null));

        assertEquals(BLUE, rgb(image, 25, 25));
    }

    @Test
    void paintWithFullWhiteAmbientKeepsTheViewColors() {
        BufferedImage image = render(new LightingLayerUI(fullAmbient()));

        assertEquals(BLUE, rgb(image, 25, 25));
        assertEquals(BLUE, rgb(image, 0, 0));
    }

    @Test
    void paintDarkensTheViewByTheAmbientLight() {
        BufferedImage image = render(new LightingLayerUI(new LightMgmt(Color.WHITE, 0.5f)));

        assertEquals(0x00007F, rgb(image, 25, 25));
    }

    @Test
    void paintWithNoAmbientAndNoLightsIsBlack() {
        BufferedImage image = render(new LightingLayerUI(new LightMgmt(Color.WHITE, 0f)));

        assertEquals(0, rgb(image, 25, 25));
    }

    @Test
    void paintUsesTheCurrentLightMgmt() {
        LightingLayerUI ui = new LightingLayerUI(new LightMgmt(Color.WHITE, 0f));
        assertEquals(0, rgb(render(ui), 25, 25));

        ui.setLightMgmt(fullAmbient());

        assertEquals(BLUE, rgb(render(ui), 25, 25));
    }

    @Test
    void paintLightsOnlyTheAreaNearALight() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(new LightPoint(new Point(25, 25), LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE));

        BufferedImage image = render(new LightingLayerUI(mgmt));

        assertEquals(BLUE, rgb(image, 25, 25));
        assertEquals(0, rgb(image, 2, 2));
    }

    // ---- blocker outlines ----

    private BufferedImage renderWithBlockerOutline(LightTag tag, boolean visible) {
        LightLayer layer = new LightLayer(SIZE, SIZE);
        LightBlocker blocker = layer.addBlocker(10, 10, 20, 20, tag);
        blocker.setVisible(visible);
        LightingLayerUI ui = new LightingLayerUI(new LightMgmt(layer, Color.WHITE, 1f));
        ui.setShowBlockers(true);
        return render(ui);
    }

    @Test
    void blockerOutlinesAreHiddenByDefault() {
        LightLayer layer = new LightLayer(SIZE, SIZE);
        layer.addBlocker(10, 10, 20, 20, LightTag.BLOCK);

        BufferedImage image = render(new LightingLayerUI(new LightMgmt(layer, Color.WHITE, 1f)));

        assertEquals(BLUE, rgb(image, 10, 20));
    }

    @Test
    void blockOutlineIsRed() {
        assertEquals(0xFF0000, rgb(renderWithBlockerOutline(LightTag.BLOCK, true), 10, 20));
    }

    @Test
    void reflectOutlineIsCyan() {
        assertEquals(0x00FFFF, rgb(renderWithBlockerOutline(LightTag.REFLECT, true), 10, 20));
    }

    @Test
    void litOutlineIsGreen() {
        assertEquals(0x00FF00, rgb(renderWithBlockerOutline(LightTag.LIT, true), 10, 20));
    }

    @Test
    void inactiveBlockerOutlineIsGray() {
        assertEquals(Color.GRAY.getRGB() & 0xFFFFFF, rgb(renderWithBlockerOutline(LightTag.BLOCK, false), 10, 20));
    }

    @Test
    void blockerOutlinesWithoutALightLayerDoNothing() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setShowBlockers(true);

        BufferedImage image = render(ui);

        assertEquals(BLUE, rgb(image, 10, 20));
    }

    // ---- light markers ----

    @Test
    void lightMarkersAreHiddenByDefault() {
        LightMgmt mgmt = fullAmbient();
        mgmt.addLight(new LightPoint(new Point(25, 25), LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE));

        assertEquals(BLUE, rgb(render(new LightingLayerUI(mgmt)), 25, 25));
    }

    @Test
    void lightMarkerIsAWhiteSquareWithBlackOutline() {
        LightMgmt mgmt = fullAmbient();
        mgmt.addLight(new LightPoint(new Point(25, 25), LightShape.CIRCLE, 10, 0, 0, .5f, Color.WHITE));
        LightingLayerUI ui = new LightingLayerUI(mgmt);
        ui.setShowLights(true);

        BufferedImage image = render(ui);

        assertEquals(0xFFFFFF, rgb(image, 25, 25));
        assertEquals(0x000000, rgb(image, 19, 19)); // top left corner of the 12px marker
    }

    // ---- interactable markers ----

    private static InteractableObj interactable() {
        Point[] corners = {new Point(10, 10), new Point(40, 10), new Point(40, 40), new Point(10, 40)};
        return new InteractableObj(corners, Color.RED);
    }

    private static boolean anyPixelChanged(BufferedImage image) {
        for (int y = 12; y < 38; y++) {
            for (int x = 12; x < 38; x++) {
                if (rgb(image, x, y) != BLUE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void interactableMarkersAreHiddenByDefault() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setInteractables(() -> List.of(interactable()));

        assertFalse(anyPixelChanged(render(ui)));
    }

    @Test
    void interactableMarkersAreDrawnWhenEnabled() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setShowInteractable(true);
        ui.setInteractables(() -> List.of(interactable()));

        assertTrue(anyPixelChanged(render(ui)));
    }

    @Test
    void interactableMarkersWithNothingToMarkLeaveTheViewAlone() {
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setShowInteractable(true);

        assertFalse(anyPixelChanged(render(ui)));
    }

    @Test
    void interactableWithTooFewCornersIsSkipped() {
        InteractableObj broken = new InteractableObj(new Point[]{new Point(10, 10), new Point(40, 40)}, Color.RED);
        LightingLayerUI ui = new LightingLayerUI(fullAmbient());
        ui.setShowInteractable(true);
        ui.setInteractables(() -> List.of(broken));

        BufferedImage image = assertDoesNotThrow(() -> render(ui));

        assertFalse(anyPixelChanged(image));
    }
}
