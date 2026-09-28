package paintapp.classes;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class DrawingCanvas extends CustomCanvas {

    private WritableImage drawingBeforeGesture;

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
            if (currentTool == DrawingTool.LINE || currentTool == DrawingTool.RECTANGLE) {
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                drawingBeforeGesture = snapshot(params, null);
            }
        });

        this.setOnMouseDragged(event -> {
            if (currentTool == DrawingTool.PENCIL) {
                double currentX = event.getX();
                double currentY = event.getY();

                graphics.strokeLine(lastX, lastY, currentX, currentY);
                modified = true;

                lastX = currentX;
                lastY = currentY;
            } else if (currentTool == DrawingTool.LINE  || currentTool == DrawingTool.RECTANGLE) {
                restoreDrawingBeforeGesture();
                drawShape(event.getX(), event.getY());
            }
        });

        this.setOnMouseReleased(event -> {
            if (currentTool == DrawingTool.LINE || currentTool == DrawingTool.RECTANGLE) {
                restoreDrawingBeforeGesture();
                drawShape(event.getX(), event.getY());
                
                modified = true;
                drawingBeforeGesture = null;
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
        LINE,
        RECTANGLE
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

    /**
     * Restores the drawing as it was before the current gesture.
     */
    private void restoreDrawingBeforeGesture() {
        clearDrawings();
        getGraphicsContext2D().drawImage(drawingBeforeGesture, 0, 0);
    }

    /**
     * Draws the selected shape from the gesture's starting point.
     *
     * @param endX the horizontal endpoint
     * @param endY the vertical endpoint
     */
    private void drawShape(double endX, double endY) {
        GraphicsContext graphics = getGraphicsContext2D();

        if (currentTool == DrawingTool.LINE) {
            graphics.strokeLine(startX, startY, endX, endY);
        } else if (currentTool == DrawingTool.RECTANGLE) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            graphics.strokeRect(x, y, width, height);
        }
    }
}
