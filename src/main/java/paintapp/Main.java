package paintapp;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;
import paintapp.controllers.ImageController;
import paintapp.services.FileService;
import paintapp.ui.MenuBarFactory;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // main controllers and services
        BorderPane root = new BorderPane();
        ImageController imageController = new ImageController();
        FileService fileService = new FileService();
        MenuBarFactory menu = new MenuBarFactory();

        root.setTop(menu.getMenuBar()); // set the top of the border pane to the menu bar
        root.setCenter(imageController.getScrollPane()); // set to our scroll pane, top of stack
        // current hierarchy goes ScrollPane -> workspace -> stackpane -> imageview + canvas
        
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
                fileService.saveImage(imageController.getModifiedImage()); // save the modified image to the current file
            } catch (Exception e) {
                printFormattedException(e);
            }
        });

        // Save As
        menu.getSaveAsItem().setOnAction(event -> {
            try {
                fileService.saveImageAs(stage, imageController.getModifiedImage()); // save the modified image to a new file chosen by the user
            } catch (Exception e) {
                printFormattedException(e);
            }
        });
        
        // create the scene and set it on the stage
        Scene scene = new Scene(root, 900, 600); // parent to root and set dimensions to 900x600
        stage.setTitle("Nolan's Pain(t)");
        stage.setScene(scene);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }

    private void printFormattedException(Exception e) {
        StackTraceElement trace = e.getStackTrace()[0];
        System.err.println("""
            %s%s Failed: %s
            File: %s
            Line: %d%s"""
            .formatted(
                "\u001B[31m", trace.getMethodName(), e.getMessage(),
                trace.getFileName(),
                trace.getLineNumber(), "\u001B[0m"
            )
        );
    }
    
}
