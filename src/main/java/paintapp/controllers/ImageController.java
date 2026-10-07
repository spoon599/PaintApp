package paintapp.controllers;

import java.util.Stack;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import paintapp.classes.CustomScrollPane;
import paintapp.classes.CustomStackPane;
import paintapp.classes.DrawingCanvas;

public class ImageController {

    private final ImageView imageView;
    private final DrawingCanvas drawingCanvas; // drawing sheet
    private final CustomStackPane stackPane; // snapshot container (image + drawing sheet)
    private final CustomStackPane workspace; // global allignment pane
    private final CustomScrollPane scrollPane; // scroll movement, top of chain

    private final Stack<Image> undoStack = new Stack<>();
    private final Stack<Image> redoStack = new Stack<>();
    
    /**
     * Creates a new ImageController with a default ImageView.
     */
    public ImageController() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // preserve image's aspect ratio when setting a new image

        drawingCanvas = new DrawingCanvas();
        drawingCanvas.setBeforeEdit(this::recordBeforeEdit);

        CustomStackPane.setAlignment(imageView, Pos.TOP_LEFT);
        CustomStackPane.setAlignment(drawingCanvas, Pos.TOP_LEFT);
        
        // actual image stack
        stackPane = new CustomStackPane();
        stackPane.getChildren().addAll(
            imageView, // add this first as our bottom layer
            drawingCanvas // invisible top layer for drawing
        );

        // used to center the stackPane
        workspace = new CustomStackPane(stackPane); // workspace -> stackPane -> imageView & canvas

        // setup scrolling
        scrollPane = new CustomScrollPane(workspace);
        scrollPane.setFitToDimensions(true);

        drawingCanvas.setupDrawing();
        createBlankImage(800, 500);
    }
    
    public Image getImage() {
        return imageView.getImage();
    }
    public ImageView getImageView() {
        return imageView;
    }
    public CustomStackPane getStackPane() {
        return stackPane;
    }
    public CustomStackPane getWorkspace() {
        return workspace;
    }
    public CustomScrollPane getScrollPane() {
        return scrollPane;
    }
    public DrawingCanvas getDrawingCanvas() {
        return drawingCanvas;
    }

    /**
     * Replaces the complete image with white while preserving its dimensions.
     * The previous image is retained for undo.
     */
    public void clearCanvas() {
        int width = (int) drawingCanvas.getWidth();
        int height = (int) drawingCanvas.getHeight();
    
        recordBeforeEdit();
        createBlankImage(width, height);
        drawingCanvas.setModified(true);
    }

    /**
     * Records the complete image before an edit begins.
     * Starting a new edit discards the previous redo history.
     */
    public void recordBeforeEdit() {
        undoStack.push(getModifiedImage());
        redoStack.clear();
    }

    /**
     * Restores the image before the most recent edit.
     */
    public void undo() {
        if (undoStack.empty()) {
            return;
        }

        redoStack.push(getModifiedImage());
        setImage(undoStack.pop());
        drawingCanvas.setModified(true);
    }

    /**
     * Restores the most recently undone edit.
     */
    public void redo() {
        if (redoStack.empty()) {
            return;
        }

        undoStack.push(getModifiedImage());
        setImage(redoStack.pop());
        drawingCanvas.setModified(true);
    }
    
    /**
     * Sets the image displayed in the controller's ImageView.
     * @param image The image to display
     */
    public void setImage(Image image) {
        imageView.setImage(image); // apply image (aspect ratio preserved by default)
        drawingCanvas.sizeTo(image);
        stackPane.sizeTo(image);
        
        drawingCanvas.clearDrawings(); // clear the canvas when a new image is set
    }

    /**
     * Returns a snapshot of the current image and drawings as a new image.
     */
    public Image getModifiedImage() {
        int width = (int) drawingCanvas.getWidth();
        int height = (int) drawingCanvas.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("Canvas dimensions must be positive to create a modified image.");
        }

        WritableImage modifiedImage = new WritableImage(
            width,
            height
        );
        
        stackPane.snapshot(null, modifiedImage); // take a snapshot of the current stack pane (should be image + drawings)
        return modifiedImage;
    }

    /**
     * Creates a blank white image and prepares the drawing area.
     *
     * @param width width in pixels
     * @param height height in pixels
     */
    private void createBlankImage(int width, int height) {
        WritableImage blankImage = new WritableImage(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                blankImage.getPixelWriter().setColor(x, y, Color.WHITE);
            }
        }

        setImage(blankImage);
        drawingCanvas.setModified(false);
    }

    /**
     * Reads the color at the given pixel
     * @return the color of the given position or null if it isn't within the dimensions of the canvas
     */
    public Color readColorAt(double x, double y) {
        if (x < 0 || y < 0
            || x >= drawingCanvas.getWidth()
            || y >= drawingCanvas.getHeight()
        ) {
            return null;
        }

        Image image = getModifiedImage();

        return image.getPixelReader().getColor(
            (int) x,
            (int) y
        );
    }
    
}
