package com.example.paintapp.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ImageController {

    private final ImageView imageView;
    private final Canvas canvas;
    private final StackPane stackPane;

    private double lastX;
    private double lastY;
    
    /**
     * Creates a new ImageController with a default ImageView.
     */
    public ImageController() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true); // preserve image's aspect ratio when setting a new image

        canvas = new Canvas();
        
        stackPane = new StackPane();
        stackPane.getChildren().addAll(
            imageView, // add this first as our bottom layer
            canvas // invisible top layer for drawing
        ); 

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
    public StackPane getStackPane() {
        return stackPane;
    }

    /**
     * Sets the image displayed in the controller's ImageView.
     * @param image The image to display
     */
    public void setImage(Image image) {
        imageView.setImage(image);

        // adjust canvas size to match the new image dimensions
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
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
    
}
