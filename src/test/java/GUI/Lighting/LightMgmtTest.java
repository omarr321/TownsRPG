package GUI.Lighting;

import GUI.Lighting.LightBlocker.LightTag;
import GUI.Lighting.LightPoint.LightShape;
import Helper.GameSettings;
import Helper.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class LightMgmtTest {

    @BeforeEach
    void setUp() {
        // width 0 = "not set", so GameSettings.scale() leaves pixel sizes alone
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private static LightPoint whiteLight(int x, int y, int dist) {
        return new LightPoint(new Point(x, y), LightShape.CIRCLE, dist, 0, 0, .5f, Color.WHITE);
    }

    private static BufferedImage whiteImage(int w, int h, int type) {
        BufferedImage image = new BufferedImage(w, h, type);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return image;
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    private static int red(BufferedImage image, int x, int y) {
        return (image.getRGB(x, y) >> 16) & 0xFF;
    }

    // ---- constructors / ambient ----

    @Test
    void defaultConstructorIsDimWhiteAmbient() {
        LightMgmt mgmt = new LightMgmt();

        assertEquals(Color.WHITE, mgmt.getAmbientColor());
        assertEquals(0.1f, mgmt.getAmbientIntensity());
        assertNull(mgmt.getLightLayer());
        assertTrue(mgmt.getLights().isEmpty());
    }

    @Test
    void ambientConstructorStoresColorAndIntensity() {
        LightMgmt mgmt = new LightMgmt(Color.BLUE, 0.4f);

        assertEquals(Color.BLUE, mgmt.getAmbientColor());
        assertEquals(0.4f, mgmt.getAmbientIntensity());
        assertNull(mgmt.getLightLayer());
    }

    @Test
    void layerConstructorStoresLayerAndAmbient() {
        LightLayer layer = new LightLayer(10, 10);

        LightMgmt mgmt = new LightMgmt(layer, Color.GREEN, 0.3f);

        assertSame(layer, mgmt.getLightLayer());
        assertEquals(Color.GREEN, mgmt.getAmbientColor());
        assertEquals(0.3f, mgmt.getAmbientIntensity());
    }

    @Test
    void layerOnlyConstructorUsesDimWhiteAmbient() {
        LightLayer layer = new LightLayer(10, 10);

        LightMgmt mgmt = new LightMgmt(layer);

        assertSame(layer, mgmt.getLightLayer());
        assertEquals(Color.WHITE, mgmt.getAmbientColor());
        assertEquals(0.1f, mgmt.getAmbientIntensity());
    }

    @Test
    void setAmbientChangesColorAndIntensity() {
        LightMgmt mgmt = new LightMgmt();

        mgmt.setAmbient(Color.RED, 0.8f);

        assertEquals(Color.RED, mgmt.getAmbientColor());
        assertEquals(0.8f, mgmt.getAmbientIntensity());
    }

    @Test
    void setAmbientColorKeepsIntensity() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.25f);

        mgmt.setAmbientColor(Color.CYAN);

        assertEquals(Color.CYAN, mgmt.getAmbientColor());
        assertEquals(0.25f, mgmt.getAmbientIntensity());
    }

    @Test
    void setAmbientIntensityKeepsColor() {
        LightMgmt mgmt = new LightMgmt(Color.ORANGE, 0.25f);

        mgmt.setAmbientIntensity(0.9f);

        assertEquals(Color.ORANGE, mgmt.getAmbientColor());
        assertEquals(0.9f, mgmt.getAmbientIntensity());
    }

    // ---- lights ----

    @Test
    void addLightStoresLight() {
        LightMgmt mgmt = new LightMgmt();
        LightPoint light = whiteLight(0, 0, 10);

        mgmt.addLight(light);

        assertEquals(1, mgmt.getLights().size());
        assertSame(light, mgmt.getLights().get(0));
    }

    @Test
    void removeLightTakesItOut() {
        LightMgmt mgmt = new LightMgmt();
        LightPoint a = whiteLight(0, 0, 10);
        LightPoint b = whiteLight(5, 5, 10);
        mgmt.addLight(a);
        mgmt.addLight(b);

        mgmt.removeLight(a);

        assertEquals(1, mgmt.getLights().size());
        assertSame(b, mgmt.getLights().get(0));
    }

    @Test
    void removeLightThatWasNeverAddedDoesNothing() {
        LightMgmt mgmt = new LightMgmt();
        mgmt.addLight(whiteLight(0, 0, 10));

        assertDoesNotThrow(() -> mgmt.removeLight(whiteLight(1, 1, 5)));

        assertEquals(1, mgmt.getLights().size());
    }

    @Test
    void clearLightsRemovesEverything() {
        LightMgmt mgmt = new LightMgmt();
        mgmt.addLight(whiteLight(0, 0, 10));
        mgmt.addLight(whiteLight(5, 5, 10));

        mgmt.clearLights();

        assertTrue(mgmt.getLights().isEmpty());
    }

    // ---- light layer ----

    @Test
    void setLightLayerReplacesLayer() {
        LightMgmt mgmt = new LightMgmt();
        LightLayer layer = new LightLayer(10, 10);

        mgmt.setLightLayer(layer);
        assertSame(layer, mgmt.getLightLayer());

        mgmt.setLightLayer(null);
        assertNull(mgmt.getLightLayer());
    }

    // ---- reflect spread / lit blend ----

    @Test
    void reflectSpreadDefaultsToFifty() {
        assertEquals(50, new LightMgmt().getReflectSpread());
    }

    @Test
    void setReflectSpreadKeepsPositiveValues() {
        LightMgmt mgmt = new LightMgmt();

        mgmt.setReflectSpread(120);

        assertEquals(120, mgmt.getReflectSpread());
    }

    @Test
    void setReflectSpreadNeverGoesBelowOne() {
        LightMgmt mgmt = new LightMgmt();

        mgmt.setReflectSpread(0);
        assertEquals(1, mgmt.getReflectSpread());

        mgmt.setReflectSpread(-30);
        assertEquals(1, mgmt.getReflectSpread());
    }

    @Test
    void litBlendDefaultsToPointThree() {
        assertEquals(0.3f, new LightMgmt().getLitBlend());
    }

    @Test
    void setLitBlendKeepsValuesFromZeroToOne() {
        LightMgmt mgmt = new LightMgmt();

        mgmt.setLitBlend(0f);
        assertEquals(0f, mgmt.getLitBlend());

        mgmt.setLitBlend(0.75f);
        assertEquals(0.75f, mgmt.getLitBlend());

        mgmt.setLitBlend(1f);
        assertEquals(1f, mgmt.getLitBlend());
    }

    @Test
    void setLitBlendIsClampedBetweenZeroAndOne() {
        LightMgmt mgmt = new LightMgmt();

        mgmt.setLitBlend(-0.5f);
        assertEquals(0f, mgmt.getLitBlend());

        mgmt.setLitBlend(4f);
        assertEquals(1f, mgmt.getLitBlend());
    }

    // ---- getLitColor ----

    @Test
    void litColorWithNoLightsIsTheBaseColorTimesAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);

        Color lit = mgmt.getLitColor(new Point(10, 10), new Color(200, 100, 50));

        assertEquals(new Color(100, 50, 25), lit);
    }

    @Test
    void litColorKeepsTheBaseAlpha() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);

        Color lit = mgmt.getLitColor(new Point(10, 10), new Color(200, 100, 50, 128));

        assertEquals(128, lit.getAlpha());
    }

    @Test
    void litColorWithZeroAmbientAndNoLightsIsBlack() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0f);

        Color lit = mgmt.getLitColor(new Point(0, 0), Color.WHITE);

        assertEquals(0, lit.getRed());
        assertEquals(0, lit.getGreen());
        assertEquals(0, lit.getBlue());
    }

    @Test
    void litColorIsFullyLitAtTheCenterOfALight() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(50, 50, 100));

        Color lit = mgmt.getLitColor(new Point(50, 50), new Color(200, 100, 50));

        assertEquals(new Color(200, 100, 50), lit);
    }

    @Test
    void litColorOutsideALightIsOnlyAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);
        mgmt.addLight(whiteLight(0, 0, 10));

        Color lit = mgmt.getLitColor(new Point(500, 500), new Color(200, 100, 50));

        assertEquals(new Color(100, 50, 25), lit);
    }

    @Test
    void coloredLightOnlyLightsItsOwnChannels() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(new LightPoint(new Point(0, 0), LightShape.CIRCLE, 50, 0, 0, .5f, Color.RED));

        Color lit = mgmt.getLitColor(new Point(0, 0), new Color(100, 100, 100));

        assertEquals(new Color(100, 0, 0), lit);
    }

    @Test
    void litColorNeverGoesAboveTheBaseColor() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 1f);
        mgmt.addLight(whiteLight(0, 0, 50));
        mgmt.addLight(whiteLight(0, 0, 50));

        Color lit = mgmt.getLitColor(new Point(0, 0), new Color(120, 60, 30));

        assertEquals(new Color(120, 60, 30), lit);
    }

    @Test
    void separateLightsAddUp() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(new LightPoint(new Point(0, 0), LightShape.CIRCLE, 50, 0, 0, .5f, Color.RED));
        mgmt.addLight(new LightPoint(new Point(0, 0), LightShape.CIRCLE, 50, 0, 0, .5f, Color.BLUE));

        Color lit = mgmt.getLitColor(new Point(0, 0), Color.WHITE);

        assertEquals(new Color(255, 0, 255), lit);
    }

    // ---- applyTo: input checks ----

    @Test
    void applyToRejectsUnsupportedImageTypes() {
        LightMgmt mgmt = new LightMgmt();
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_BGR);

        assertThrows(IllegalArgumentException.class, () -> mgmt.applyTo(image));
    }

    @Test
    void applyToRejectsByteBackedImages() {
        LightMgmt mgmt = new LightMgmt();
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);

        assertThrows(IllegalArgumentException.class, () -> mgmt.applyTo(image));
    }

    // ---- applyTo: ambient only ----

    @Test
    void applyToDarkensRgbImageByAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);
        BufferedImage image = whiteImage(8, 8, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0x7F7F7F, rgb(image, 0, 0));
        assertEquals(0x7F7F7F, rgb(image, 7, 7));
    }

    @Test
    void applyToDarkensArgbImageByAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);
        BufferedImage image = whiteImage(8, 8, BufferedImage.TYPE_INT_ARGB);

        mgmt.applyTo(image);

        assertEquals(0x7F7F7F, rgb(image, 3, 3));
    }

    @Test
    void applyToKeepsAlpha() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(1, 1, 0x80FFFFFF);
        image.setRGB(2, 2, 0xFFFFFFFF);

        mgmt.applyTo(image);

        assertEquals(0x80, image.getRGB(1, 1) >>> 24);
        assertEquals(0xFF, image.getRGB(2, 2) >>> 24);
    }

    @Test
    void applyToWithFullWhiteAmbientLeavesImageUnchanged() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 1f);
        BufferedImage image = whiteImage(6, 6, BufferedImage.TYPE_INT_RGB);
        image.setRGB(2, 2, 0x123456);

        mgmt.applyTo(image);

        assertEquals(0xFFFFFF, rgb(image, 0, 0));
        assertEquals(0x123456, rgb(image, 2, 2));
    }

    @Test
    void applyToWithZeroAmbientAndNoLightsIsBlack() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0f);
        BufferedImage image = whiteImage(6, 6, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0, rgb(image, 3, 3));
    }

    @Test
    void applyToTintsWithColoredAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.RED, 1f);
        BufferedImage image = whiteImage(6, 6, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFF0000, rgb(image, 3, 3));
    }

    @Test
    void applyToCanBeCalledRepeatedlyOnNewImages() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);

        for (int i = 0; i < 3; i++) {
            BufferedImage image = whiteImage(8, 8, BufferedImage.TYPE_INT_RGB);
            mgmt.applyTo(image);
            assertEquals(0x7F7F7F, rgb(image, 4, 4));
        }
    }

    @Test
    void applyToWorksAfterTheImageSizeChanges() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.5f);

        BufferedImage small = whiteImage(4, 4, BufferedImage.TYPE_INT_RGB);
        mgmt.applyTo(small);
        BufferedImage large = whiteImage(12, 9, BufferedImage.TYPE_INT_RGB);
        mgmt.applyTo(large);

        assertEquals(0x7F7F7F, rgb(small, 0, 0));
        assertEquals(0x7F7F7F, rgb(large, 11, 8));
    }

    // ---- applyTo: lights ----

    @Test
    void applyToLightsTheAreaAroundALight() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(10, 10, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFFFFFF, rgb(image, 10, 10)); // center
        assertEquals(0xFFFFFF, rgb(image, 13, 10)); // solid part of the light
        assertEquals(0x000000, rgb(image, 0, 0));   // far corner, outside the light
        assertEquals(0x000000, rgb(image, 20, 20));
    }

    @Test
    void applyToFadesLightTowardsItsEdge() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(10, 10, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        int nearEdge = red(image, 10, 19); // 9 pixels from the light
        assertTrue(nearEdge > 0, "Light should still reach 9 pixels away");
        assertTrue(nearEdge < 255, "Light should be fading 9 pixels away");
    }

    @Test
    void applyToUsesTheLightColor() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(new LightPoint(new Point(10, 10), LightShape.CIRCLE, 10, 0, 0, .5f, Color.RED));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFF0000, rgb(image, 10, 10));
    }

    @Test
    void applyToAddsLightOnTopOfAmbient() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.25f);
        mgmt.addLight(whiteLight(10, 10, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFFFFFF, rgb(image, 10, 10));
        assertEquals(0x3F3F3F, rgb(image, 0, 0));
    }

    @Test
    void applyToHandlesALightPartlyOffTheImage() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(0, 0, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));

        assertEquals(0xFFFFFF, rgb(image, 0, 0));
        assertEquals(0x000000, rgb(image, 20, 20));
    }

    @Test
    void applyToHandlesALightFullyOffTheImage() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(500, 500, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));

        assertEquals(0, rgb(image, 10, 10));
    }

    @Test
    void applyToMatchesGetLitColorForAnUnblockedLight() {
        LightMgmt mgmt = new LightMgmt(Color.WHITE, 0.2f);
        mgmt.addLight(whiteLight(10, 10, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        for (int[] p : new int[][]{{10, 10}, {12, 10}, {10, 16}, {3, 3}, {0, 20}}) {
            Color expected = mgmt.getLitColor(new Point(p[0], p[1]), Color.WHITE);
            assertEquals(expected.getRGB() & 0xFFFFFF, rgb(image, p[0], p[1]), "pixel " + p[0] + "," + p[1]);
        }
    }

    @Test
    void removedLightNoLongerLightsTheImage() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        LightPoint light = whiteLight(10, 10, 10);
        mgmt.addLight(light);
        mgmt.removeLight(light);
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0, rgb(image, 10, 10));
    }

    // ---- applyTo: blockers ----

    @Test
    void blockerCastsAShadowBehindIt() {
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(15, 0, 5, 21, LightTag.BLOCK);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFFFFFF, rgb(image, 12, 10), "in front of the blocker");
        assertEquals(0x000000, rgb(image, 25, 10), "behind the blocker");
        assertEquals(0x000000, rgb(image, 35, 10), "further behind the blocker");
    }

    @Test
    void sameSceneWithoutABlockerLightsThePixelBehind() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertTrue(red(image, 25, 10) > 0);
    }

    @Test
    void removingTheBlockerBringsTheLightBack() {
        LightLayer layer = new LightLayer(41, 21);
        LightBlocker blocker = layer.addBlocker(15, 0, 5, 21, LightTag.BLOCK);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));

        layer.removeBlocker(blocker);
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);
        mgmt.applyTo(image);

        assertTrue(red(image, 25, 10) > 0);
    }

    @Test
    void hiddenBlockerDoesNotBlockLight() {
        LightLayer layer = new LightLayer(41, 21);
        LightBlocker blocker = layer.addBlocker(15, 0, 5, 21, LightTag.BLOCK);
        blocker.setVisible(false);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertTrue(red(image, 25, 10) > 0);
    }

    @Test
    void ambientLightIsNotBlocked() {
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(0, 0, 41, 21, LightTag.BLOCK);
        LightMgmt mgmt = new LightMgmt(layer, Color.WHITE, 0.5f);
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0x7F7F7F, rgb(image, 20, 10));
    }

    @Test
    void lightInsideABlockerStillShines() {
        // a light that sits inside a blocker ignores that blocker, otherwise it would smother itself
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(0, 0, 10, 21, LightTag.BLOCK);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        mgmt.applyTo(image);

        assertEquals(0xFFFFFF, rgb(image, 12, 10));
    }

    @Test
    void reflectBlockerAppliesWithoutErrors() {
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(15, 0, 5, 21, LightTag.REFLECT);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.setReflectSpread(10);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));

        assertEquals(0xFFFFFF, rgb(image, 12, 10), "in front of the reflector");
    }

    @Test
    void litBlockerAppliesWithoutErrors() {
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(15, 0, 5, 21, LightTag.LIT);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));
    }

    @Test
    void blockerOutsideTheImageIsIgnored() {
        LightLayer layer = new LightLayer(41, 21);
        layer.addBlocker(500, 500, 10, 10, LightTag.BLOCK);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 10, 30));
        BufferedImage image = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));

        assertTrue(red(image, 25, 10) > 0);
    }

    @Test
    void emptyLightLayerBehavesLikeNoLayer() {
        LightLayer layer = new LightLayer(41, 21);
        LightMgmt withLayer = new LightMgmt(layer, Color.BLACK, 0f);
        LightMgmt withoutLayer = new LightMgmt(Color.BLACK, 0f);
        withLayer.addLight(whiteLight(5, 10, 30));
        withoutLayer.addLight(whiteLight(5, 10, 30));
        BufferedImage a = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);
        BufferedImage b = whiteImage(41, 21, BufferedImage.TYPE_INT_RGB);

        withLayer.applyTo(a);
        withoutLayer.applyTo(b);

        for (int y = 0; y < 21; y++) {
            for (int x = 0; x < 41; x++) {
                assertEquals(rgb(b, x, y), rgb(a, x, y), "pixel " + x + "," + y);
            }
        }
    }

    //---New Code---

    @Test
    void stampBlockersSkipsTransparentPixels() {
        LightLayer layer = new LightLayer(21, 21);
        // Use a component-based blocker whose image has transparent pixels
        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 10, 10);
        LightBlocker blocker = layer.addBlocker(panel, LightTag.BLOCK);

        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 5, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_ARGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));
    }

    @Test
    void fillLitHandlesOffScreenLitBlocker() {
        LightLayer layer = new LightLayer(21, 21);
        // LIT blocker placed completely outside the 21x21 image bounds
        layer.addBlocker(100, 100, 10, 10, LightTag.LIT);
        LightMgmt mgmt = new LightMgmt(layer, Color.BLACK, 0f);
        mgmt.addLight(whiteLight(5, 5, 10));
        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        assertDoesNotThrow(() -> mgmt.applyTo(image));
    }

    @Test
    void applyToLightCompletelyOffScreenTriggersContinue() {
        LightMgmt mgmt = new LightMgmt(Color.BLACK, 0f);
        // Place a light entirely off-screen (e.g., center at -100, -100 with radius 10)
        mgmt.addLight(new LightPoint(new Point(-100, -100), LightShape.CIRCLE, 10, 0, 0, 0.5f, Color.WHITE));

        mgmt.addLight(new LightPoint(new Point(2000, 2000), LightPoint.LightShape.CIRCLE, 2000, 0, 0, .90f, Color.white));

        BufferedImage image = whiteImage(21, 21, BufferedImage.TYPE_INT_RGB);

        // This should pass cleanly without throwing out-of-bounds array errors
        // because the off-screen check skips processing it.
        assertDoesNotThrow(() -> mgmt.applyTo(image));
    }
}
