package com.example.paintapp;

import com.example.paintapp.controllers.ImageController;
import com.example.paintapp.services.FileService;
import com.example.paintapp.ui.MenuBarFactory;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

public class Main extends Application {

    @Override 
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        ImageController imageController = new ImageController();
        FileService fileService = new FileService();
        MenuBarFactory menu = new MenuBarFactory();

        root.setTop(menu.getMenuBar());
        root.setCenter(imageController.getImageView());
        
        // Open
        menu.getOpenItem().setOnAction(event -> {
            Image image = fileService.openImage(stage);

            if (image != null) {
                imageController.setImage(image);
            }
        });

        // Exit
        menu.getExitItem().setOnAction(event -> 
            Platform.exit()
        );

        // Save
        menu.getSaveItem().setOnAction(event -> {
            try {
                fileService.saveImage(imageController.getImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Save As
        menu.getSaveAsItem().setOnAction(event -> {
            try {
                fileService.saveImageAs(stage, imageController.getImage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("Nolan's Pain(t)");
        stage.setScene(scene);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
    
}
