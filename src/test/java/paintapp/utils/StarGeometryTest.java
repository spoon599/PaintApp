package paintapp.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StarGeometryTest {

    private static final double tolerance = 0.000001;

    @Test
    void fourPointsFormADiamond() {
        double[][] vertices = StarGeometry.createVertices(
            10, 20, 5, 0, 4
        );

        assertArrayEquals(
            new double[] {15, 10, 5, 10},
            vertices[0],
            tolerance
        );

        assertArrayEquals(
            new double[] {20, 25, 20, 15},
            vertices[1],
            tolerance
        );
    }

    @Test
    void fivePointStarHasFiveOuterTipsAndFiveInnerCorners() {
        double[][] vertices = StarGeometry.createVertices(
            10, 20, 10, 0, 5
        );

        assertEquals(10, vertices[0].length);
        assertEquals(10, vertices[1].length);

        assertEquals(20, vertices[0][0], tolerance);
        assertEquals(20, vertices[1][0], tolerance);

        for (int i = 0; i < 10; i++) {
            double distance = Math.hypot(
                vertices[0][i] - 10,
                vertices[1][i] - 20
            );

            double expectedRadius = i % 2 == 0 ? 10 : 4.5;
            assertEquals(expectedRadius, distance, tolerance);
        }
    }

    @Test
    void invalidPointCountsAndRadiiAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            StarGeometry.createVertices(0, 0, 10, 0, 3)
        );

        assertThrows(IllegalArgumentException.class, () ->
            StarGeometry.createVertices(0, 0, 10, 0, 101)
        );

        assertThrows(IllegalArgumentException.class, () ->
            StarGeometry.createVertices(0, 0, -1, 0, 5)
        );

        assertThrows(IllegalArgumentException.class, () ->
            StarGeometry.createVertices(0, 0, Double.NaN, 0, 5)
        );

        assertThrows(IllegalArgumentException.class, () ->
            StarGeometry.createVertices(
                0, 0, Double.POSITIVE_INFINITY, 0, 5
            )
        );
    }
}