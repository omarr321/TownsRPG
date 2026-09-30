package Helper;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontWrapper {
    private String filePath;
    private String fontName;
    private final static Font DEFAULT_FONT = new Font(Font.SANS_SERIF,Font.PLAIN,16);
    private Font customFont;

    public FontWrapper(String filePath, String fontName) {
        this.filePath = filePath;
        this.fontName = fontName;

        if(this.filePath != null) {
            try (InputStream is = getClass().getResourceAsStream(filePath)) {
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
                System.err.println(fontName + " Font failed to load! Defaulting to Sans Serif.");
                customFont = FontWrapper.DEFAULT_FONT;
            } catch (FontFormatException e) {
                System.err.println(fontName + " Font may be corrupted! Defaulting to Sans Serif.");
                customFont = FontWrapper.DEFAULT_FONT;
            }
        } else {
            this.customFont = FontWrapper.DEFAULT_FONT;
        }
    }

    public Font getFont( int size){
        return customFont.deriveFont(Font.PLAIN, size);
    }

    //For unit testing only
    String getFilePath() {
        return this.filePath;
    }
    String getFontName() {
        return this.fontName;
    }
    public static Font getDefaultFont() {
        return FontWrapper.DEFAULT_FONT;
    }
}
