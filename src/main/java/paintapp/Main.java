package paintapp;

import paintapp.controllers.ImageController;
import paintapp.services.FileService;
import paintapp.ui.CustomMenuBar;
import paintapp.ui.CustomToolBar;
import paintapp.utils.ExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // main controllers and services
        BorderPane root = new BorderPane();
        ImageController imageController = new ImageController();
        FileService fileService = new FileService();
        CustomMenuBar menu = new CustomMenuBar();
        CustomToolBar toolbar = new CustomToolBar();

        VBox container = new VBox(
            menu.getMenuBar(),
            toolbar.getToolBar()
        );

        // line width event
        toolbar.getLineWidthSlider().valueProperty().addListener(
            (observable, oldVal, newVal) -> {
                imageController.getDrawingCanvas().setLineWidth(newVal.doubleValue());
            }
        );

        root.setTop(container); // set to top container
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
                ExceptionHandler.printFormattedException(e);
            }
        });

        // Save As
        menu.getSaveAsItem().setOnAction(event -> {
            try {
                fileService.saveImageAs(stage, imageController.getModifiedImage()); // save the modified image to a new file chosen by the user
            } catch (Exception e) {
                ExceptionHandler.printFormattedException(e);
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
    
}
