package paintapp.classes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DrawingCanvas extends CustomCanvas {

    private double lastX;
    private double lastY;

    public DrawingCanvas() {}

    /**
     * Sets up the canvas for drawing.
     */
    public void setupDrawing() {
        GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(3);

        this.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        this.setOnMouseDragged(event -> {
            double currentX = event.getX();
            double currentY = event.getY();

            graphics.strokeLine(lastX, lastY, currentX, currentY);

            lastX = currentX;
            lastY = currentY;
        });
    }

    /**
     * Removes all drawings from the current canvas.
     */
    public void clearDrawings() {
        GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.clearRect(
            0, 
            0, 
            this.getWidth(), 
            this.getHeight()
        );
    }

    public void setLineWidth(double width) {
        this.getGraphicsContext2D().setLineWidth(width);
    }

    public void setLineColor(Color color) {
        this.getGraphicsContext2D().setStroke(color);
    }
    
}
