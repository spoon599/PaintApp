package paintapp.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import paintapp.classes.CustomCanvas;
import paintapp.classes.CustomScrollPane;
import paintapp.classes.CustomStackPane;

public class ImageController {

    private final ImageView imageView;
    private final CustomCanvas canvas; // drawing sheet
    private final CustomStackPane stackPane; // snapshot container (image + drawing sheet)
    private final CustomStackPane workspace; // global allignment pane
    private final CustomScrollPane scrollPane; // scroll movement, top of chain

    private double lastX;
    private double lastY;
    
    /**
     * Creates a new ImageController with a default ImageView.
     */
    public ImageController() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // preserve image's aspect ratio when setting a new image

        canvas = new CustomCanvas();
        
        // actual image stack
        stackPane = new CustomStackPane();
        stackPane.getChildren().addAll(
            imageView, // add this first as our bottom layer
            canvas // invisible top layer for drawing
        );

        // used to center the stackPane
        workspace = new CustomStackPane(stackPane); // workspace -> stackPane -> imageView & canvas

        // setup scrolling
        scrollPane = new CustomScrollPane(workspace);
        scrollPane.setFitToDimensions(true);

        setupDrawing();
    }
    
    /**
     * Returns the current image being displayed within the ImageView.
     */
    public Image getImage() {
        return imageView.getImage();
    }

    /**
     * Returns the current ImageView.
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     * Returns the pane containing the image and drawing canvas.
     */
    public CustomStackPane getStackPane() {
        return stackPane;
    }

    /**
     * Returns the workspace stackpane.
     */
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
        canvas.sizeTo(image);
        stackPane.sizeTo(image);
        cleanCanvas(); // clear the canvas when a new image is set
    }

    /**
     * Sets up the canvas for drawing on top of the image.
     */
    private void setupDrawing() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setStroke(Color.BLACK);
        graphics.setLineWidth(3);

        canvas.setOnMousePressed(event -> {
            lastX = event.getX();
            lastY = event.getY();
        });

        canvas.setOnMouseDragged(event -> {
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
    private void cleanCanvas() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(
            0, 
            0, 
            canvas.getWidth(), 
            canvas.getHeight()
        );
        graphics.restore();
    }

    /**
     * Returns a snapshot of the current image and drawings as a new image.
     */
    public Image getModifiedImage() {
        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();

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
