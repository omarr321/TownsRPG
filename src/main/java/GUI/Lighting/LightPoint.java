package GUI.Lighting;

import Helper.GameSettings;
import Helper.Point;

import java.awt.*;

/**
 * A single point light source that adds colored, distance-attenuated light to a scene.
 *
 * <p>The light has a location, a shape (circular or square), a maximum reach, an optional
 * inner dead zone where it emits nothing, and a color. Use {@link #getBrightnessAt(int, int)}
 * to find how bright the light is at any given coordinate.
 */
public class LightPoint {
    private final Point loc;
    private final LightShape shape;
    private final int dist;
    private final int deadZone;
    private final int deadZoneFade;
    private final float intensity;
    private float red;
    private float green;
    private float blue;

    /**
     * Constructs a LightPoint with the specified location, shape, reach distance,
     * dead zone configuration, intensity, and color.
     * @param loc The 2D coordinate location of the light source.
     * @param shape The geometric shape of the light (e.g., CIRCLE or SQUARE).
     * @param dist The maximum reach distance of the light.
     * @param deadZone The radius/size of the inner dead zone where light is not emitted.
     * @param deadZoneFade The pixel distance over which the dead zone edge softens.
     * @param intensity The solid-intensity ratio of the light spread.
     * @param lightColor The base Color of the light.
     */
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

    /**
     * Gets the normalized red component of the light color (0.0 to 1.0).
     * @return The red float value.
     */
    public float getRed() {
        return this.red;
    }

    /**
     * Gets the normalized green component of the light color (0.0 to 1.0).
     * @return The green float value.
     */
    public float getGreen() {
        return this.green;
    }

    /**
     * Gets the normalized blue component of the light color (0.0 to 1.0).
     * @return The blue float value.
     */
    public float getBlue() {
        return this.blue;
    }

    /**
     * Calculates the brightness level of this light at a given 2D screen coordinate,
     * factoring in shape boundaries, dead zones, distance falloff, and smooth edge blending.
     * @param pointX The x coordinate to check.
     * @param pointY The y coordinate to check.
     * @return A float representing the brightness value (from 0.0 to 1.0).
     */
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

    /**
     * Gets the location point of this light source.
     * @return The Point representing the light's location.
     */
    public Point getLoc(){
        return this.loc;
    }

    /**
     * Gets the maximum reach distance of this light source.
     * @return The distance in scaled pixels.
     */
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

    /**
     * Defines the geometric spread shape options for light sources.
     */
    public enum LightShape {
        /** Light spreads outward in a circle, measured by straight-line distance from the source. */
        CIRCLE,
        /** Light spreads outward in a square, measured by the larger of the horizontal and vertical distances. */
        SQUARE
    }
}
