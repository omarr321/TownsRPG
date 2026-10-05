package gui.lighting;

import gui.lighting.LightPoint.LightShape;
import helpers.GameSettings;
import helpers.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class LightPointTest {
    private static final int CX = 100;
    private static final int CY = 100;

    @BeforeEach
    void setUp() {
        // width 0 means "not set", which makes GameSettings.scale() leave sizes unchanged
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    @AfterEach
    void tearDown() {
        GameSettings.screenWidth = 0;
        GameSettings.screenHeight = 0;
    }

    private LightPoint circle(int dist, int deadZone, int deadZoneFade, float intensity) {
        return new LightPoint(new Point(CX, CY), LightShape.CIRCLE, dist, deadZone, deadZoneFade, intensity, Color.WHITE);
    }

    private LightPoint square(int dist, int deadZone, int deadZoneFade, float intensity) {
        return new LightPoint(new Point(CX, CY), LightShape.SQUARE, dist, deadZone, deadZoneFade, intensity, Color.WHITE);
    }

    // ---- enum ----

    @Test
    void lightShapeHasCircleAndSquare() {
        assertEquals(2, LightShape.values().length);
        assertNotNull(LightShape.valueOf("CIRCLE"));
        assertNotNull(LightShape.valueOf("SQUARE"));
    }

    // ---- constructor / getters ----

    @Test
    void getLocReturnsTheGivenPoint() {
        Point loc = new Point(5, 6);

        LightPoint light = new LightPoint(loc, LightShape.CIRCLE, 50, 0, 0, .5f, Color.WHITE);

        assertSame(loc, light.getLoc());
    }

    @Test
    void getDistIsUnscaledWhenScreenSizeIsNotSet() {
        assertEquals(100, circle(100, 0, 0, .5f).getDist());
    }

    @Test
    void getDistScalesWithScreenWidth() {
        GameSettings.screenWidth = 3840; // twice the 1920 reference width

        assertEquals(200, circle(100, 0, 0, .5f).getDist());
    }

    @Test
    void getDistShrinksOnSmallerScreens() {
        GameSettings.screenWidth = 960; // half the reference width

        assertEquals(50, circle(100, 0, 0, .5f).getDist());
    }

    @Test
    void colorChannelsAreConvertedToZeroToOne() {
        LightPoint light = new LightPoint(new Point(0, 0), LightShape.CIRCLE, 10, 0, 0, .5f, new Color(255, 0, 51));

        assertEquals(1f, light.getRed(), 0.0001f);
        assertEquals(0f, light.getGreen(), 0.0001f);
        assertEquals(0.2f, light.getBlue(), 0.0001f);
    }

    @Test
    void whiteLightHasAllChannelsAtOne() {
        LightPoint light = circle(10, 0, 0, .5f);

        assertEquals(1f, light.getRed());
        assertEquals(1f, light.getGreen());
        assertEquals(1f, light.getBlue());
    }

    @Test
    void blackLightHasAllChannelsAtZero() {
        LightPoint light = new LightPoint(new Point(0, 0), LightShape.CIRCLE, 10, 0, 0, .5f, Color.BLACK);

        assertEquals(0f, light.getRed());
        assertEquals(0f, light.getGreen());
        assertEquals(0f, light.getBlue());
    }

    @Test
    void movingTheLocationMovesTheLight() {
        LightPoint light = circle(50, 0, 0, .5f);
        assertEquals(0f, light.getBrightnessAt(CX + 200, CY));

        light.getLoc().setX(CX + 200);

        assertEquals(1f, light.getBrightnessAt(CX + 200, CY));
    }

    // ---- brightness: basic shape ----

    @Test
    void brightnessIsFullAtTheCenter() {
        assertEquals(1f, circle(100, 0, 0, .5f).getBrightnessAt(CX, CY));
    }

    @Test
    void brightnessIsZeroAtAndBeyondTheEdge() {
        LightPoint light = circle(100, 0, 0, .5f);

        assertEquals(0f, light.getBrightnessAt(CX + 100, CY));
        assertEquals(0f, light.getBrightnessAt(CX + 101, CY));
        assertEquals(0f, light.getBrightnessAt(CX, CY - 500));
    }

    @Test
    void brightnessIsPositiveJustInsideTheEdge() {
        float brightness = circle(100, 0, 0, .5f).getBrightnessAt(CX + 99, CY);

        assertTrue(brightness > 0f);
        assertTrue(brightness < 1f);
    }

    @Test
    void brightnessStaysBetweenZeroAndOne() {
        LightPoint light = circle(100, 0, 0, .5f);

        for (int d = 0; d <= 120; d++) {
            float b = light.getBrightnessAt(CX + d, CY);
            assertTrue(b >= 0f && b <= 1f, "brightness " + b + " at distance " + d);
        }
    }

    @Test
    void brightnessNeverIncreasesWithDistance() {
        LightPoint light = circle(100, 0, 0, .5f);
        float previous = light.getBrightnessAt(CX, CY);

        for (int d = 1; d <= 120; d++) {
            float b = light.getBrightnessAt(CX + d, CY);
            assertTrue(b <= previous, "brightness went up at distance " + d);
            previous = b;
        }
    }

    @Test
    void brightnessIsSymmetricAroundTheCenter() {
        LightPoint light = circle(100, 0, 0, .5f);

        float right = light.getBrightnessAt(CX + 60, CY);

        assertEquals(right, light.getBrightnessAt(CX - 60, CY));
        assertEquals(right, light.getBrightnessAt(CX, CY + 60));
        assertEquals(right, light.getBrightnessAt(CX, CY - 60));
    }

    // ---- intensity ----

    @Test
    void solidPartIsFullBrightness() {
        LightPoint light = circle(100, 0, 0, .5f);

        // the solid part covers roughly the first half of the radius
        assertEquals(1f, light.getBrightnessAt(CX + 20, CY));
        assertEquals(1f, light.getBrightnessAt(CX + 40, CY));
        assertTrue(light.getBrightnessAt(CX + 80, CY) < 1f);
    }

    @Test
    void higherIntensityKeepsTheLightSolidFurtherOut() {
        LightPoint soft = circle(100, 0, 0, .1f);
        LightPoint hard = circle(100, 0, 0, .9f);

        assertTrue(hard.getBrightnessAt(CX + 70, CY) > soft.getBrightnessAt(CX + 70, CY));
    }

    @Test
    void intensityOfOneIsSolidAllTheWayToTheEdge() {
        LightPoint light = circle(100, 0, 0, 1f);

        assertEquals(1f, light.getBrightnessAt(CX + 50, CY));
        assertEquals(1f, light.getBrightnessAt(CX + 99, CY));
        assertEquals(0f, light.getBrightnessAt(CX + 100, CY));
    }

    @Test
    void intensityOfZeroFadesImmediately() {
        LightPoint light = circle(100, 0, 0, 0f);

        assertTrue(light.getBrightnessAt(CX + 10, CY) < 1f);
    }

    // ---- shapes ----

    @Test
    void circleAndSquareAgreeAlongTheAxes() {
        LightPoint circle = circle(100, 0, 0, .5f);
        LightPoint square = square(100, 0, 0, .5f);

        assertEquals(circle.getBrightnessAt(CX + 70, CY), square.getBrightnessAt(CX + 70, CY));
        assertEquals(circle.getBrightnessAt(CX, CY - 70), square.getBrightnessAt(CX, CY - 70));
    }

    @Test
    void squareLightReachesIntoTheCorners() {
        LightPoint circle = circle(100, 0, 0, .5f);
        LightPoint square = square(100, 0, 0, .5f);

        // (90, 90) away is 127 pixels by circle distance (outside) but only 90 by square distance (inside)
        assertEquals(0f, circle.getBrightnessAt(CX + 90, CY + 90));
        assertTrue(square.getBrightnessAt(CX + 90, CY + 90) > 0f);
    }

    @Test
    void squareLightIsCutOffAtItsSide() {
        LightPoint square = square(100, 0, 0, .5f);

        assertEquals(0f, square.getBrightnessAt(CX + 100, CY + 100));
        assertEquals(0f, square.getBrightnessAt(CX + 100, CY));
        assertTrue(square.getBrightnessAt(CX + 99, CY + 99) > 0f);
    }

    @Test
    void squareDistanceUsesTheLargerOfXAndY() {
        LightPoint square = square(100, 0, 0, .5f);

        assertEquals(square.getBrightnessAt(CX + 60, CY), square.getBrightnessAt(CX + 60, CY + 30));
    }

    // ---- dead zone ----

    @Test
    void deadZoneAddsNoLight() {
        // a dead zone value of 11 means 10 pixels (the constructor subtracts one)
        LightPoint light = circle(100, 11, 0, .5f);

        assertEquals(0f, light.getBrightnessAt(CX, CY));
        assertEquals(0f, light.getBrightnessAt(CX + 5, CY));
        assertEquals(0f, light.getBrightnessAt(CX + 10, CY));
    }

    @Test
    void lightStartsJustOutsideTheDeadZone() {
        LightPoint light = circle(100, 11, 0, .5f);

        assertTrue(light.getBrightnessAt(CX + 11, CY) > 0f);
    }

    @Test
    void deadZoneFadeSoftensTheInnerEdge() {
        LightPoint hard = circle(100, 11, 0, .5f);
        LightPoint soft = circle(100, 11, 20, .5f);

        float hardEdge = hard.getBrightnessAt(CX + 11, CY);
        float softEdge = soft.getBrightnessAt(CX + 11, CY);

        assertEquals(1f, hardEdge);
        assertTrue(softEdge > 0f);
        assertTrue(softEdge < 0.1f, "Fade should start nearly dark, got " + softEdge);
    }

    @Test
    void deadZoneFadeGetsBrighterAwayFromTheEdge() {
        LightPoint light = circle(100, 11, 20, .5f);

        float near = light.getBrightnessAt(CX + 12, CY);
        float mid = light.getBrightnessAt(CX + 20, CY);

        assertTrue(mid > near);
    }

    @Test
    void deadZoneFadeHasNoEffectPastTheFadeDistance() {
        LightPoint hard = circle(100, 11, 0, .5f);
        LightPoint soft = circle(100, 11, 20, .5f);

        assertEquals(hard.getBrightnessAt(CX + 60, CY), soft.getBrightnessAt(CX + 60, CY));
    }

    @Test
    void deadZoneFadeIsIgnoredWithoutADeadZone() {
        LightPoint plain = circle(100, 0, 0, .5f);
        LightPoint withFade = circle(100, 0, 30, .5f);

        assertEquals(plain.getBrightnessAt(CX + 5, CY), withFade.getBrightnessAt(CX + 5, CY));
    }

    @Test
    void deadZoneAlsoWorksForSquareLights() {
        LightPoint light = square(100, 11, 0, .5f);

        assertEquals(0f, light.getBrightnessAt(CX + 10, CY + 10));
        assertTrue(light.getBrightnessAt(CX + 11, CY + 11) > 0f);
    }

    // ---- scaling ----

    @Test
    void deadZoneScalesWithScreenWidth() {
        GameSettings.screenWidth = 3840;
        LightPoint light = circle(100, 11, 0, .5f); // dead zone is 10 reference pixels = 20 real pixels

        assertEquals(0f, light.getBrightnessAt(CX + 20, CY));
        assertTrue(light.getBrightnessAt(CX + 21, CY) > 0f);
    }

    @Test
    void lightReachScalesWithScreenWidth() {
        GameSettings.screenWidth = 3840;
        LightPoint light = circle(100, 0, 0, .5f); // reaches 200 real pixels

        assertTrue(light.getBrightnessAt(CX + 150, CY) > 0f);
        assertEquals(0f, light.getBrightnessAt(CX + 200, CY));
    }
}
