package Helper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourcesTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/images/BrickWall.png",
            "/images/Crate.png",
            "/images/DebugImage.png",
            "/images/LampTransparent.png",
            "/images/LightTest.png",
            "/images/UVGrid.png",
            "/images/WoodFloor.png"
    })
    void requiredImageExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing image: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/branding/colors.txt",
            "/branding/icon.svg",
            "/branding/logo.svg"
    })
    void requiredBrandingExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing branding: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/images/UI/arrow_left.png",
            "/images/UI/arrow_right.png",
            "/images/UI/arrow_straight.png"
    })
    void requiredUIExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing UI: " + path);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/fonts/Tuffy.ttf",
            "/fonts/Tuffy_Bold.ttf",
            "/fonts/untyped.ttf"
    })
    void requiredFontExists(String path) {
        assertNotNull(getClass().getResource(path), "Missing font: " + path);
    }
}
