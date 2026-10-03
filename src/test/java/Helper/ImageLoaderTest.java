package Helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ImageLoaderTest {
    private static final String VALID = "/images/debugging/DebugImage.png";
    private static final String MISSING = "/images/does_not_exist.png";

    @Test
    void loadsValidImage() {
        ImageLoader loader = new ImageLoader(VALID);

        assertTrue(loader.isLoaded());
        assertFalse(loader.isUsingDefault());
        assertNotNull(loader.getImage());
    }

    @Test
    void loadedImageHasRealDimensions() {
        ImageLoader loader = new ImageLoader(VALID);

        assertTrue(loader.getImage().getWidth(null) > 0);
        assertTrue(loader.getImage().getHeight(null) > 0);
    }

    @Test
    void fallsBackToDefaultWhenImageIsMissing() {
        ImageLoader loader = new ImageLoader(MISSING);

        assertTrue(loader.isLoaded());
        assertTrue(loader.isUsingDefault());
        assertNotNull(loader.getImage());
    }

    @Test
    void notLoadedWhenDefaultIsAlsoMissing() {
        ImageLoader loader = new ImageLoader(MISSING) {
            @Override
            String getDefaultPath() {
                return MISSING;
            }
        };

        assertFalse(loader.isLoaded());
        assertNull(loader.getImage());
    }

    @Test
    void emptyPathFallsBackToDefault() {
        ImageLoader loader = new ImageLoader("");

        assertTrue(loader.isUsingDefault());
    }

    @Test
    void nullPathFallsBackToDefault() {
        ImageLoader loader = new ImageLoader(null);

        assertTrue(loader.isUsingDefault());
        assertTrue(loader.isLoaded());
    }
}
