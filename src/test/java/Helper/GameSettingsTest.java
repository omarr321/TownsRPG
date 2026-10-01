package Helper;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

public class GameSettingsTest {
    @Test
    void defaultValuesNotChanged() {
        assertFalse(GameSettings.fullScreen);

        assertEquals(1920, GameSettings.REFERENCE_WIDTH);
        assertEquals(0, GameSettings.screenWidth);
        assertEquals(0, GameSettings.screenHeight);
    }

    @Test
    void getScaleValid() {
        GameSettings.screenWidth = 1920;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = 960;
        assertEquals(0.50f, GameSettings.getScale());

        GameSettings.screenWidth = 3840;
        assertEquals(2f, GameSettings.getScale());
    }

    @Test
    void getScaleInvalid() {
        GameSettings.screenWidth = 0;
        assertEquals(1f, GameSettings.getScale());

        GameSettings.screenWidth = -1920;
        assertEquals(1f, GameSettings.getScale());
    }

    @Test
    void scaleValid() {
        GameSettings.screenWidth = 0;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(1, GameSettings.scale(1));
        assertEquals(100, GameSettings.scale(100));
        assertEquals(200, GameSettings.scale(200));
        assertEquals(300, GameSettings.scale(300));
        assertEquals(400, GameSettings.scale(400));
        assertEquals(500, GameSettings.scale(500));

        GameSettings.screenWidth = 960;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(1, GameSettings.scale(1));
        assertEquals(50, GameSettings.scale(100));
        assertEquals(100, GameSettings.scale(200));
        assertEquals(150, GameSettings.scale(300));
        assertEquals(200, GameSettings.scale(400));
        assertEquals(250, GameSettings.scale(500));

        GameSettings.screenWidth = 3840;
        assertEquals(-1, GameSettings.scale(-1));
        assertEquals(0, GameSettings.scale(0));
        assertEquals(2, GameSettings.scale(1));
        assertEquals(200, GameSettings.scale(100));
        assertEquals(400, GameSettings.scale(200));
        assertEquals(600, GameSettings.scale(300));
        assertEquals(800, GameSettings.scale(400));
        assertEquals(1000, GameSettings.scale(500));
    }

    @Test
    void descaleValid() {
        GameSettings.screenWidth = 0;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(1, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(100));
        assertEquals(200, GameSettings.descale(200));
        assertEquals(300, GameSettings.descale(300));
        assertEquals(400, GameSettings.descale(400));
        assertEquals(500, GameSettings.descale(500));

        GameSettings.screenWidth = 960;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(2, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(50));
        assertEquals(200, GameSettings.descale(100));
        assertEquals(300, GameSettings.descale(150));
        assertEquals(400, GameSettings.descale(200));
        assertEquals(500, GameSettings.descale(250));

        GameSettings.screenWidth = 3840;
        assertEquals(-1, GameSettings.descale(-1));
        assertEquals(0, GameSettings.descale(0));
        assertEquals(1, GameSettings.descale(1));
        assertEquals(100, GameSettings.descale(200));
        assertEquals(150, GameSettings.descale(300));
        assertEquals(200, GameSettings.descale(400));
        assertEquals(250, GameSettings.descale(500));
        assertEquals(300, GameSettings.descale(600));
    }

    @Test
    void testPrivateConstructorIsHidden() throws Exception {
        Constructor<GameSettings> constructor = GameSettings.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // This asserts that calling the private constructor throws an UnsupportedOperationException
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
