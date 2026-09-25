package GUI.Lighting;

import Helper.GameSettings;
import Helper.Point;

import java.awt.*;

public class LightPoint {
    private Point loc;
    private LightShape shape;
    private int dist;
    private int deadZone;
    private int deadZoneFade;
    private float intensity;
    private float red;
    private float green;
    private float blue;

    public LightPoint(Point loc, LightShape shape, int dist, int deadZone, int deadZoneFade, float intensity, Color lightColor) {
        this.loc = loc;
        this.shape = shape;
        this.dist = GameSettings.scale(dist);
        this.deadZone = GameSettings.scale(deadZone-1);
        this.intensity = intensity;
        this.deadZoneFade = GameSettings.scale(deadZoneFade);
        setLightColor(lightColor);
    }

    private void setLightColor(Color lightColor) {
        this.red = lightColor.getRed() /255f;
        this.green = lightColor.getGreen() /255f;
        this.blue = lightColor.getBlue() /255f;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }


    public float getBrightnessAt(int pointX, int pointY) {
        int xDist = pointX - this.loc.getX();
        int yDist = pointY - this.loc.getY();

        float d;

        if (shape == LightShape.CIRCLE) {
            d = (float) Math.sqrt(xDist * xDist + yDist * yDist);   // round distance
        } else {
            d = Math.max(Math.abs(xDist), Math.abs(yDist));   // square distance
        }

        if (d >= dist) {
            return 0f; // outside the light
        }
        if (d <= deadZone) {
            return 0f; // dead zone: this light adds nothing
        }
        float t = (d - deadZone) / (dist - deadZone); // 0 at dead-zone edge, 1 at outer edge
        float brightness = fade(t);

        // Soften the edge of the dead zone: fade in over the first few pixels past it
        if (deadZone > 0 && this.deadZoneFade > 0) {
            float fromDeadZone = d - this.deadZone;
            if (fromDeadZone < this.deadZoneFade) {
                float s = fromDeadZone / this.deadZoneFade;      // 0 at the dead zone's edge, 1 at the end of the blend
                brightness = brightness * s * s * (3f - 2f * s); // smooth S-curve
            }
        }

        return brightness;
    }

    public Point getLoc(){
        return this.loc;
    }

    public int getDist() {
        return this.dist;
    }
    private float fade(float t) {
        if (t <= intensity) {
            return 1f; // solid part
        }
        float fadeT = (t - intensity) / (1f - intensity); // 0 where the fade starts, 1 at the edge
        float falloff = 1f - fadeT;
        return falloff * falloff;
    }


    public enum LightShape {
        CIRCLE,
        SQUARE
    }
}
