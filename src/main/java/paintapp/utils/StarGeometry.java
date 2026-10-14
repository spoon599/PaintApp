package paintapp.utils;

public final class StarGeometry {

    private StarGeometry() {}

    public static double[][] createVertices(
            double centerX,
            double centerY,
            double radius,
            double startAngle,
            int points) {

        if (points < 4 || points > 100) {
            throw new IllegalArgumentException(
                "Choose between 4 and 100 star points."
            );
        }

        if (!Double.isFinite(radius) || radius < 0) {
            throw new IllegalArgumentException(
                "Radius must be finite and non-negative."
            );
        }

        int vertexCount = points == 4 ? 4 : points * 2;
        double[] xPoints = new double[vertexCount];
        double[] yPoints = new double[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            double angle = startAngle
                + i * 2 * Math.PI / vertexCount;

            double vertexRadius = points == 4 || i % 2 == 0
                ? radius
                : radius * 0.45;

            xPoints[i] = centerX + vertexRadius * Math.cos(angle);
            yPoints[i] = centerY + vertexRadius * Math.sin(angle);
        }

        return new double[][] { xPoints, yPoints };
    }
}