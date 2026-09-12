package com.example.paintapp.services;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileService {

    private File currentFile;

    public Image openImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Image Files",
                "*.png",
                "*.jpg",
                "*.jpeg"
            )
        );

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            currentFile = selectedFile;
            return new Image(selectedFile.toURI().toString());
        }

        return null;
    }

    public void saveImage(Image image) throws IOException {
        if (currentFile != null && image != null) {
            writeImage(image, currentFile);
        }
    }

    public void saveImageAs(Stage stage, Image image) throws IOException {
        if (image == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image As");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "PNG Image",
                "*.png"
            )
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {

            if (!selectedFile.getName().toLowerCase().endsWith(".png")) {
                selectedFile = new File(
                    selectedFile.getAbsolutePath() + ".png"
                );
            }

            currentFile = selectedFile;
            writeImage(image, currentFile);
        }
    }

    private void writeImage(Image image, File file) throws IOException {
        ImageIO.write(
            SwingFXUtils.fromFXImage(image, null),
            "png",
            file
        );
    }
}