package GUI.Lighting;

import Helper.Point;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds every light in a scene plus the scene's ambient light,
 * and works out what color things should be once lit.
 */
public class LightMgmt {

    private final List<LightPoint> lights = new ArrayList<>();

    private Color ambientColor;
    private float ambientIntensity;
    // ambient color * intensity as 0..1 values, stored so they aren't recalculated per pixel
    private float ambientRed;
    private float ambientGreen;
    private float ambientBlue;

    // Per-pixel light totals used by applyTo, reused between frames
    private float[] redBuf;
    private float[] greenBuf;
    private float[] blueBuf;

    public LightMgmt(Color ambientColor, float ambientIntensity) {
        setAmbient(ambientColor, ambientIntensity);
    }

    /** Dim white ambient light. */
    public LightMgmt() {
        this(Color.WHITE, 0.1f);
    }

    // ---------------------------------------------------------------
    // Ambient light
    // ---------------------------------------------------------------
    public void setAmbient(Color color, float intensity) {
        this.ambientColor = color;
        this.ambientIntensity = intensity;
        this.ambientRed = color.getRed() / 255f * intensity;
        this.ambientGreen = color.getGreen() / 255f * intensity;
        this.ambientBlue = color.getBlue() / 255f * intensity;
    }

    public void setAmbientColor(Color color) {
        setAmbient(color, ambientIntensity);
    }

    public void setAmbientIntensity(float intensity) {
        setAmbient(ambientColor, intensity);
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    // ---------------------------------------------------------------
    // Lights
    // ---------------------------------------------------------------
    public void addLight(LightPoint light) {
        lights.add(light);
    }

    public void removeLight(LightPoint light) {
        lights.remove(light);
    }

    public void clearLights() {
        lights.clear();
    }

    public List<LightPoint> getLights() {
        return lights;
    }

    // ---------------------------------------------------------------
    // Single point: give it a point and a color, get the lit color back
    // ---------------------------------------------------------------
    public Color getLitColor(Point point, Color baseColor) {
        int x = point.getX();
        int y = point.getY();

        // Start with the ambient light, then add every light that reaches this point
        float red = ambientRed;
        float green = ambientGreen;
        float blue = ambientBlue;
        for (LightPoint light : lights) {
            float strength = light.getBrightnessAt(x, y);
            if (strength > 0f) {
                red += strength * light.getRed();
                green += strength * light.getGreen();
                blue += strength * light.getBlue();
            }
        }

        return new Color(
                lightChannel(baseColor.getRed(), red),
                lightChannel(baseColor.getGreen(), green),
                lightChannel(baseColor.getBlue(), blue),
                baseColor.getAlpha());
    }

    /** Multiplies one 0..255 color channel by the light reaching it (capped at 1). */
    private static int lightChannel(int channel, float light) {
        if (light > 1f) {
            light = 1f;
        }
        return (int) (channel * light);
    }

    // ---------------------------------------------------------------
    // Whole image: lights every pixel of an image at once (used by LitPanel)
    // Does the same math as getLitColor, but much faster for a full frame.
    // ---------------------------------------------------------------
    public void applyTo(BufferedImage image) {
        int type = image.getType();
        if (type != BufferedImage.TYPE_INT_RGB && type != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalArgumentException("Image must be TYPE_INT_RGB or TYPE_INT_ARGB");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int size = width * height;

        if (redBuf == null || redBuf.length != size) {
            redBuf = new float[size];
            greenBuf = new float[size];
            blueBuf = new float[size];
        }

        // 1. Every pixel starts with the ambient light
        Arrays.fill(redBuf, ambientRed);
        Arrays.fill(greenBuf, ambientGreen);
        Arrays.fill(blueBuf, ambientBlue);

        // 2. Add each light, only inside the square it can reach
        for (LightPoint light : lights) {
            int cx = light.getLoc().getX();
            int cy = light.getLoc().getY();
            int x0 = Math.max(0, cx - light.getDist());
            int y0 = Math.max(0, cy - light.getDist());
            int x1 = Math.min(width - 1, cx + light.getDist());
            int y1 = Math.min(height - 1, cy + light.getDist());

            for (int y = y0; y <= y1; y++) {
                int row = y * width;
                for (int x = x0; x <= x1; x++) {
                    float strength = light.getBrightnessAt(x, y);
                    if (strength > 0f) {
                        int i = row + x;
                        redBuf[i] += strength * light.getRed();
                        greenBuf[i] += strength * light.getGreen();
                        blueBuf[i] += strength * light.getBlue();
                    }
                }
            }
        }

        // 3. Multiply every pixel's color by the light that reached it
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < size; i++) {
            int p = pixels[i];
            int alpha = p & 0xFF000000;
            int red = (p >> 16) & 0xFF;
            int green = (p >> 8) & 0xFF;
            int blue = p & 0xFF;

            red = lightChannel(red, redBuf[i]);
            green = lightChannel(green, greenBuf[i]);
            blue = lightChannel(blue, blueBuf[i]);

            pixels[i] = alpha | (red << 16) | (green << 8) | blue;
        }
    }
}