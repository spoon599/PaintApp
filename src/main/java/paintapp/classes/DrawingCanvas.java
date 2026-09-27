package paintapp.classes;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class DrawingCanvas extends CustomCanvas {

    private double startX;
    private double startY;
    private double lastX;
    private double lastY;

    private boolean modified = false;

    public DrawingCanvas() {}

    /**
     * Sets up the canvas for drawing.
     */
    public void setupDrawing() {
        GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(3);

        this.setOnMousePressed(event -> {
            startX = event.getX();
            startY = event.getY();

            lastX = startX;
            lastY = startY;
        });

        this.setOnMouseDragged(event -> {
            if (currentTool == DrawingTool.PENCIL) {
                double currentX = event.getX();
                double currentY = event.getY();

                graphics.strokeLine(lastX, lastY, currentX, currentY);
                modified = true;

                lastX = currentX;
                lastY = currentY;
            }
        });

        this.setOnMouseReleased(event -> {
            if (currentTool == DrawingTool.LINE) {
                graphics.strokeLine(
                    startX,
                    startY,
                    event.getX(),
                    event.getY()
                );
                modified = true;
            }
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

    public void resizeDrawing(double width, double height) {
        WritableImage oldDrawing = new WritableImage(
            (int) getWidth(),
            (int) getHeight()
        );

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        snapshot(params, oldDrawing);
        setSize(width, height);

        GraphicsContext graphics = getGraphicsContext2D();
        graphics.drawImage(oldDrawing, 0, 0);
    }

    public void setLineWidth(double width) {
        this.getGraphicsContext2D().setLineWidth(width);
    }
    public void setLineColor(Color color) {
        this.getGraphicsContext2D().setStroke(color);
    }
    public boolean isModified() {
        return modified;
    }
    public void setModified(boolean value) {
        modified = value;
    }

    /**
     * The drawing tools supported by this canvas.
     */
    public enum DrawingTool {
        PENCIL,
        LINE
    }

    private DrawingTool currentTool = DrawingTool.PENCIL;

    /**
     * Sets the tool used for subsequent drawing gestures.
     *
     * @param tool the drawing tool to use
     */
    public void setCurrentTool(DrawingTool tool) {
        currentTool = tool;
    }
}
