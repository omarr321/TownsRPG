package helpers;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class FontWrapperTest {
    private static final String VALID = "/fonts/Tuffy.ttf";
    private static final String VALID_NAME = "Tuffy";
    private static final String MISSING = "/font/does_not_exist.ttf";
    private static final Font MISSING_FONT = new Font(Font.SANS_SERIF,Font.PLAIN,16);

    @Test
    void loadsValidFontValidName() {
        FontWrapper fontWrapper = new FontWrapper(VALID, VALID_NAME);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNotNull(fontWrapper.getFontName());
    }

    @Test
    void loadsValidFontNullName() {
        FontWrapper fontWrapper = new FontWrapper(VALID, null);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNull(fontWrapper.getFontName());
    }

    @Test
    void loadsInvalidFontValidName() {
        FontWrapper fontWrapper = new FontWrapper(MISSING, VALID_NAME);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNotNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void loadsInvalidFontNullName() {
        FontWrapper fontWrapper = new FontWrapper(MISSING, null);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void loadsNullFontValidName() {
        FontWrapper fontWrapper = new FontWrapper(null, VALID_NAME);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNull(fontWrapper.getFilePath());
        assertNotNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void loadsNullFontNullName() {
        FontWrapper fontWrapper = new FontWrapper(null, null);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNull(fontWrapper.getFilePath());
        assertNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void loadsEmptyFontNullName() {
        FontWrapper fontWrapper = new FontWrapper("", null);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void loadsEmptyFontValidName() {
        FontWrapper fontWrapper = new FontWrapper("", VALID_NAME);

        //basic checks
        assertNotNull(fontWrapper.getFont(1));
        assertNotNull(fontWrapper.getFilePath());
        assertNotNull(fontWrapper.getFontName());

        //makes sure the replacement font is the correct replacement font.
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }

    @Test
    void defaultFontIsCorrect() {
        assertEquals(MISSING_FONT, FontWrapper.getDefaultFont());
    }

    @Test
    void loadsFontThrowsIOException() {
        FontWrapper fontWrapper = new FontWrapper("/fonts/Tuffy.ttf", "Tuffy") {
            @Override
            InputStream openStream(String path) {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("Simulated disk read error");
                    }
                };
            }
        };
        assertEquals(MISSING_FONT, fontWrapper.getFont(16));
    }
}
