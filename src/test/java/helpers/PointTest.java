package helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PointTest {
    @Test
    void validPoint() {
        int expectedX = 5;
        int expectedY = 10;
        int[] expectedArr = new int[]{expectedX, expectedY};

        Point point = new Point(expectedX, expectedY);

        assertEquals(expectedX, point.getX());
        assertEquals(expectedY, point.getY());
        assertArrayEquals(expectedArr, point.getCoords());
    }

    @Test
    void testingSetters() {
        int expectedX = 5;
        int expectedY = 10;

        Point point = new Point(0, 0);
        point.setX(expectedX);
        point.setY(expectedY);

        assertEquals(expectedX, point.getX());
        assertEquals(expectedY, point.getY());
    }
}
