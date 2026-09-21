package GraphicClasses;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontWrapper {
    String filePath = "";
    Font customFont = null;

    public FontWrapper(String filePath, String fontName) {
        this.filePath = filePath;
        //System.out.println("Resource URL: " + getClass().getResource(filePath));
        try (InputStream is = getClass().getResourceAsStream(filePath)) {
            if (is == null) {
                System.err.println(fontName + " Font not found! Defaulting to Sans Serif.");
                // Fallback font
                customFont = new Font(Font.SANS_SERIF,Font.PLAIN,16);
            } else {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(baseFont);
                customFont = baseFont;
            }
        } catch (IOException e) {
            System.err.println(fontName + " Font failed to load! Defaulting to Sans Serif.");
            customFont = new Font(Font.SANS_SERIF,Font.PLAIN,16);
        } catch (FontFormatException e) {
            System.err.println(fontName + " Font may be corrupted! Defaulting to Sans Serif.");
            customFont = new Font(Font.SANS_SERIF,Font.PLAIN,16);
        }
    }

    public Font getFont(int style, int size){
        return customFont.deriveFont(style, size);
    }
}
