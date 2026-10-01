package Helper;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is a wrapper for loading font from the disk.
 */
public class FontWrapper {
    private String filePath;
    private String fontName;
    private final static Font DEFAULT_FONT = new Font(Font.SANS_SERIF,Font.PLAIN,16);
    private Font customFont;

    /**
     * Construct the FontWrapper with file path and font name provied.
     * @param filePath The path to the font file. Normally this is a .ttf.
     * @param fontName The name of the font, this is only used for error messages.
     */
    public FontWrapper(String filePath, String fontName) {
        this.filePath = filePath;
        this.fontName = fontName;

        if(this.filePath != null) {
            try (InputStream is = openStream(filePath)) {
                if (is == null) {
                    System.err.println(fontName + " Font not found! Defaulting to Sans Serif.");
                    // Fallback font
                    customFont = FontWrapper.DEFAULT_FONT;
                } else {
                    Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    ge.registerFont(baseFont);
                    customFont = baseFont;
                }
            } catch (IOException e) {
                this.handleError(fontName, "Font failed to load!");
            } catch (FontFormatException e) {
                this.handleError(fontName, "Font may be corrupted!");
            }
        } else {
            this.customFont = FontWrapper.DEFAULT_FONT;
        }
    }

    /**
     * Returns a ready to use font of the size provided.
     * @param size The size of the font.
     * @return The font of the size provided.
     */
    public Font getFont( int size){
        return customFont.deriveFont(Font.PLAIN, size);
    }

    private void handleError(String name, String message) {
        System.err.println(name + " " + message + " Defaulting to Sans Serif.");
        this.customFont = DEFAULT_FONT;
    }

    //For unit testing only
    String getFilePath() {
        return this.filePath;
    }
    String getFontName() {
        return this.fontName;
    }

    /**
     * Returns the default font for when a font fails to load.
     * @return The default font.
     */
    public static Font getDefaultFont() {
        return FontWrapper.DEFAULT_FONT;
    }
    InputStream openStream(String path) {
        return getClass().getResourceAsStream(path);
    }
}
