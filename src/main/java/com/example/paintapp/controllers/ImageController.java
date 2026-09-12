package com.example.paintapp.controllers;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageController {

    private final ImageView imageView;
    
    public ImageController() {
        imageView = new ImageView();

        imageView.setPreserveRatio(true);
        imageView.setFitWidth(850);
        imageView.setFitHeight(530);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

    public Image getImage() {
        return imageView.getImage();
    }
    
}
