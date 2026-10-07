package paintapp.controllers;

import java.util.Stack;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import paintapp.classes.CustomScrollPane;
import paintapp.classes.CustomStackPane;
import paintapp.classes.DrawingCanvas;
import paintapp.classes.CustomCanvas;
import paintapp.classes.DrawingCanvas.DrawingTool;

public class ImageController {

    private final ImageView imageView;
    private final CustomCanvas selectionCanvas;
    private final DrawingCanvas drawingCanvas; // drawing sheet
    private final CustomStackPane stackPane; // snapshot container (image + drawing sheet)
    private final CustomStackPane workspace; // global allignment pane
    private final CustomScrollPane scrollPane; // scroll movement, top of chain

    private final Stack<Image> undoStack = new Stack<>();
    private final Stack<Image> redoStack = new Stack<>();

    private double selectionStartX;
    private double selectionStartY;
    private Rectangle2D selection;
    private Image pendingPaste;
    private Rectangle2D pendingMoveSource;
    
    /**
     * Creates a new ImageController with a default ImageView.
     */
    public ImageController() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // preserve image's aspect ratio when setting a new image

        drawingCanvas = new DrawingCanvas();
        drawingCanvas.setBeforeEdit(this::recordBeforeEdit);

        selectionCanvas = new CustomCanvas();
        selectionCanvas.setMouseTransparent(true);
        selectionCanvas.widthProperty().bind(drawingCanvas.widthProperty());
        selectionCanvas.heightProperty().bind(drawingCanvas.heightProperty());

        CustomStackPane.setAlignment(selectionCanvas, Pos.TOP_LEFT);
        CustomStackPane.setAlignment(imageView, Pos.TOP_LEFT);
        CustomStackPane.setAlignment(drawingCanvas, Pos.TOP_LEFT);
        
        // actual image stack
        stackPane = new CustomStackPane();
        stackPane.getChildren().addAll(
            imageView, // add this first as our bottom layer
            drawingCanvas, // invisible top layer for drawing
            selectionCanvas
        );

        // used to center the stackPane
        workspace = new CustomStackPane(stackPane); // workspace -> stackPane -> imageView & canvas

        // setup scrolling
        scrollPane = new CustomScrollPane(workspace);
        scrollPane.setFitToDimensions(true);

        drawingCanvas.setupDrawing();
        createBlankImage(800, 500);
        setupSelection();
        setupPaste();
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
     * Updates the selected area and its temporary outline.
     */
    private void updateSelection(double endX, double endY) {
        endX = Math.max(0, Math.min(endX, drawingCanvas.getWidth()));
        endY = Math.max(0, Math.min(endY, drawingCanvas.getHeight()));

        double x = Math.min(selectionStartX, endX);
        double y = Math.min(selectionStartY, endY);
        double width = Math.abs(endX - selectionStartX);
        double height = Math.abs(endY - selectionStartY);

        selection = new Rectangle2D(x, y, width, height);

        var graphics = selectionCanvas.getGraphicsContext2D();
        graphics.clearRect(
            0, 0, selectionCanvas.getWidth(), selectionCanvas.getHeight()
        );
        graphics.setStroke(Color.BLUE);
        graphics.setLineWidth(1);
        graphics.setLineDashes(5, 5);
        graphics.strokeRect(x, y, width, height);
    }

    /**
     * Connects mouse gestures to the selection preview.
     */
    private void setupSelection() {
        drawingCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (drawingCanvas.getCurrentTool() == DrawingTool.SELECT) {
                selectionStartX = Math.max(
                    0, Math.min(event.getX(), drawingCanvas.getWidth())
                );
                selectionStartY = Math.max(
                    0, Math.min(event.getY(), drawingCanvas.getHeight())
                );

                updateSelection(selectionStartX, selectionStartY);
            }
        });

        drawingCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (drawingCanvas.getCurrentTool() == DrawingTool.SELECT) {
                updateSelection(event.getX(), event.getY());
            }
        });

        drawingCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (drawingCanvas.getCurrentTool() == DrawingTool.SELECT) {
                updateSelection(event.getX(), event.getY());
            }
        });
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
     * Begins the process of moving the currently selected area.
     */
    public void beginMove() {
        WritableImage selectedImage = copySelection();

        if (selectedImage == null) {
            return;
        }

        Rectangle2D source = new Rectangle2D(
            Math.floor(selection.getMinX()),
            Math.floor(selection.getMinY()),
            selectedImage.getWidth(),
            selectedImage.getHeight()
        );

        beginPaste(selectedImage);
        pendingMoveSource = source;
        showPastePreview(source.getMinX(), source.getMinY());
    }

    /**
     * Begins the process of pasting an image onto the canvas.
     * @param image to be pasted onto the canvas
     */
    public void beginPaste(Image image) {
        if (image == null || image.isError()) {
            return;
        }

        clearSelection();
        pendingPaste = image;
        showPastePreview(0, 0);
    }

    /**
     * Displays a preview of the pending paste operation at the specified coordinates.
     * @param x
     * @param y
     */
    private void showPastePreview(double x, double y) {
        var graphics = selectionCanvas.getGraphicsContext2D();

        graphics.clearRect(
            0, 0,
            selectionCanvas.getWidth(),
            selectionCanvas.getHeight()
        );

        graphics.save();

        if (pendingMoveSource != null) {
            graphics.setFill(Color.WHITE);
            graphics.fillRect(
                pendingMoveSource.getMinX(),
                pendingMoveSource.getMinY(),
                pendingMoveSource.getWidth(),
                pendingMoveSource.getHeight()
            );
        }

        if (pendingPaste != null) {
            graphics.drawImage(pendingPaste, x, y);
        }

        graphics.restore();
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
     * Removes the selected area and its temporary outline.
     */
    public void clearSelection() {
        selection = null;
        pendingPaste = null;
        pendingMoveSource = null;
    
        selectionCanvas.getGraphicsContext2D().clearRect(
            0,
            0,
            selectionCanvas.getWidth(),
            selectionCanvas.getHeight()
        );
    }

    /**
     * Sets up the event handling for pasting an image onto the canvas.
     * This includes tracking mouse movements for preview and handling mouse clicks to finalize the paste.
     */
    private void setupPaste() {
        drawingCanvas.addEventFilter(MouseEvent.ANY, event -> {
            if (pendingPaste == null) {
                return;
            }

            double x = Math.floor(event.getX());
            double y = Math.floor(event.getY());

            if (event.getEventType() == MouseEvent.MOUSE_MOVED
                    || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                showPastePreview(x, y);
            }

            if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    recordBeforeEdit();

                    var graphics = drawingCanvas.getGraphicsContext2D();
                    graphics.save();

                    if (pendingMoveSource != null) {
                        graphics.setFill(Color.WHITE);
                        graphics.fillRect(
                            pendingMoveSource.getMinX(),
                            pendingMoveSource.getMinY(),
                            pendingMoveSource.getWidth(),
                            pendingMoveSource.getHeight()
                        );
                    }

                    graphics.drawImage(pendingPaste, x, y);
                    graphics.restore();

                    clearSelection();
                    drawingCanvas.setModified(true);
                } else if (event.getButton()
                        == javafx.scene.input.MouseButton.SECONDARY) {
                    clearSelection();
                }
            }

            event.consume();
        });
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
        clearSelection(); // clear the selection when a new image is set
    }

    /**
     * Copies the selected pixels from the image and drawings.
     *
     * @return the selected image, or null if no area is selected
     */
    public WritableImage copySelection() {
        if (selection == null
                || selection.getWidth() <= 0
                || selection.getHeight() <= 0) {
            return null;
        }

        Image image = getModifiedImage();

        int x = (int) Math.floor(selection.getMinX());
        int y = (int) Math.floor(selection.getMinY());

        int right = Math.min(
            (int) image.getWidth(),
            (int) Math.ceil(selection.getMaxX())
        );
        int bottom = Math.min(
            (int) image.getHeight(),
            (int) Math.ceil(selection.getMaxY())
        );

        if (right <= x || bottom <= y) {
            return null;
        }

        return new WritableImage(
            image.getPixelReader(),
            x,
            y,
            right - x,
            bottom - y
        );
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
        
        boolean selectionWasVisible = selectionCanvas.isVisible();
        selectionCanvas.setVisible(false);

        try {
            stackPane.snapshot(null, modifiedImage);
            return modifiedImage;
        } finally {
            selectionCanvas.setVisible(selectionWasVisible);
        }
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
