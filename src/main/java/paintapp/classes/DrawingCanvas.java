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
    private boolean dashed = false;

    private Runnable beforeEdit = () -> {};
    private boolean historyRecorded = false;

    public DrawingCanvas() {}

    /**
     * Sets up the canvas for drawing.
     */
    public void setupDrawing() {
        GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(3);

        this.setOnMousePressed(event -> {
            historyRecorded = false;
            
            startX = event.getX();
            startY = event.getY();

            lastX = startX;
            lastY = startY;
            if (currentToolIsShape()) {
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                drawingBeforeGesture = snapshot(params, null);
            }
        });

        this.setOnMouseDragged(event -> {
            if (currentTool == DrawingTool.PENCIL) {
                recordGestureHistory();

                double currentX = event.getX();
                double currentY = event.getY();

                graphics.strokeLine(lastX, lastY, currentX, currentY);
                modified = true;

                lastX = currentX;
                lastY = currentY;
            } else if (currentToolIsShape()) {
                recordGestureHistory();
                restoreDrawingBeforeGesture();
                drawShape(event.getX(), event.getY());
            }
        });

        this.setOnMouseReleased(event -> {
            if (currentToolIsShape()) {
                recordGestureHistory();
                restoreDrawingBeforeGesture();
                drawShape(event.getX(), event.getY());

                modified = true;
                drawingBeforeGesture = null;
            }
        });

    }

    /**
     * Sets the action called before a drawing edit begins.
     *
     * @param action the action that records the previous image
     */
    public void setBeforeEdit(Runnable action) {
        beforeEdit = action;
    }

    /**
     * Records history once for the current drawing gesture.
     */
    private void recordGestureHistory() {
        if (!historyRecorded) {
            beforeEdit.run();
            historyRecorded = true;
        }
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
        RECTANGLE,
        SQUARE,
        ELLIPSE,
        CIRCLE,
        TRIANGLE,
        DIAMOND,
        COLOR_GRABBER
    }

    private DrawingTool currentTool = DrawingTool.PENCIL;

    /**
     * Sets the tool used for subsequent drawing gestures.
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
     * Sets whether lines and shape outlines use dashes.
     * @param dashed true for dashed outlines, false for solid outlines
     */
    public void setDashed(boolean dashed) {
        this.dashed = dashed;
    }

    /**
     * Draws the selected shape from the gesture's starting point.
     * @param endX the horizontal endpoint
     * @param endY the vertical endpoint
     */
    private void drawShape(double endX, double endY) {
        GraphicsContext graphics = getGraphicsContext2D();
        graphics.save();

        if (dashed) {
            graphics.setLineDashes(10, 6);
        } else {
            graphics.setLineDashes();
        }

        if (currentTool == DrawingTool.LINE) {
            graphics.strokeLine(startX, startY, endX, endY);
        } else if (currentTool == DrawingTool.RECTANGLE) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            graphics.strokeRect(x, y, width, height);
        } else if (currentTool == DrawingTool.SQUARE) {
            double side = Math.min(
                Math.abs(endX - startX),
                Math.abs(endY - startY)
            );

            double x = endX < startX ? startX - side : startX;
            double y = endY < startY ? startY - side : startY;

            graphics.strokeRect(x, y, side, side);
        } else if (currentTool == DrawingTool.ELLIPSE) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            graphics.strokeOval(x, y, width, height);
        } else if (currentTool == DrawingTool.CIRCLE) {
            double diameter = Math.min(
                Math.abs(endX - startX),
                Math.abs(endY - startY)
            );

            double x = endX < startX ? startX - diameter : startX;
            double y = endY < startY ? startY - diameter : startY;

            graphics.strokeOval(x, y, diameter, diameter);
        } else if (currentTool == DrawingTool.TRIANGLE) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            double[] xPoints = {
                x + width / 2,
                x + width,
                x
            };

            double[] yPoints = {
                y,
                y + height,
                y + height
            };

            graphics.strokePolygon(xPoints, yPoints, 3);
        } else if (currentTool == DrawingTool.DIAMOND) {
            double x = Math.min(startX, endX);
            double y = Math.min(startY, endY);
            double width = Math.abs(endX - startX);
            double height = Math.abs(endY - startY);

            double[] xPoints = {
                x + width / 2,
                x + width,
                x + width / 2,
                x
            };

            double[] yPoints = {
                y,
                y + height / 2,
                y + height,
                y + height / 2
            };

            graphics.strokePolygon(xPoints, yPoints, 4);
        }
        graphics.restore();
    }

    /**
     * Returns true if the user's current drawing tool is a shape
     */
    private boolean currentToolIsShape() {
        if (currentTool == DrawingTool.LINE
            || currentTool == DrawingTool.RECTANGLE
            || currentTool == DrawingTool.SQUARE
            || currentTool == DrawingTool.ELLIPSE
            || currentTool == DrawingTool.CIRCLE
            || currentTool == DrawingTool.TRIANGLE
            || currentTool == DrawingTool.DIAMOND
        ) {
            return true;
        }
        return false;
    }
}