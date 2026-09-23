package paintapp.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import paintapp.classes.CustomScrollPane;
import paintapp.classes.CustomStackPane;
import paintapp.classes.DrawingCanvas;

public class ImageController {

    private final ImageView imageView;
    private final DrawingCanvas drawingCanvas; // drawing sheet
    private final CustomStackPane stackPane; // snapshot container (image + drawing sheet)
    private final CustomStackPane workspace; // global allignment pane
    private final CustomScrollPane scrollPane; // scroll movement, top of chain
    
    /**
     * Creates a new ImageController with a default ImageView.
     */
    public ImageController() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // preserve image's aspect ratio when setting a new image

        drawingCanvas = new DrawingCanvas();
        
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
    
}
