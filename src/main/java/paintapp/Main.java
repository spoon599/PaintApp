package paintapp;

import paintapp.controllers.ImageController;
import paintapp.services.FileService;
import paintapp.ui.CustomMenuBar;
import paintapp.ui.CustomToolBar;
import paintapp.utils.ExceptionHandler;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Main extends Application {

    private final ButtonType saveButton = new ButtonType("Save");
    private final ButtonType dontSaveButton = new ButtonType("Don't Save");
    private final ButtonType cancelButton = new ButtonType("Cancel");

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
        toolbar.getColorPicker().valueProperty().addListener(
            (observable, oldColor, newColor) -> {
                imageController.getDrawingCanvas().setLineColor(newColor);
            }
        );

        root.setTop(container); // set to top container
        root.setCenter(imageController.getScrollPane()); // set to our scroll pane, top of stack
        // current hierarchy goes ScrollPane -> workspace -> stackpane -> imageview + canvas
        
        // Open
        menu.getOpenItem().setOnAction(event -> {
            attemptOpen(stage, imageController, fileService);
        });

        // Exit
        menu.getExitItem().setOnAction(event -> {
            attemptExit(imageController, fileService);
        });

        // Save
        menu.getSaveItem().setOnAction(event -> {
            try {
                fileService.saveImage(imageController.getModifiedImage()); // save the modified image to the current file
                imageController.getDrawingCanvas().setModified(false);
            } catch (Exception e) {
                ExceptionHandler.printFormattedException(e);
            }
        });

        // Save As
        menu.getSaveAsItem().setOnAction(event -> {
            try {
                boolean saved = fileService.saveImageAs(
                    stage, 
                    imageController.getModifiedImage()
                );

                if (saved) {
                    imageController.getDrawingCanvas().setModified(false); 
                }
            } catch (Exception e) {
                ExceptionHandler.printFormattedException(e);
            }
        });

        // Help
        menu.getHelpItem().setOnAction(event -> {
            Alert about = new Alert(AlertType.INFORMATION);
            about.setTitle("Help");
            about.setHeaderText("Nolan's Pain(t)");
            about.setContentText("""
                Open - Opens an image.
                Save - Saves changes to the image to it's current file.
                Save As - Saves the image to a new file.
                Exit - Closes the application.
            """);
            about.showAndWait();
        });

        // About
        menu.getAboutItem().setOnAction(event -> {
            Alert about = new Alert(AlertType.INFORMATION);
            about.setTitle("About");
            about.setHeaderText("Nolan's Pain(t)");
            about.setContentText("Version 0.2.0");
            about.showAndWait();
        });
        
        // create the scene and set it on the stage
        Scene scene = new Scene(root, 900, 600); // parent to root and set dimensions to 900x600
        stage.setTitle("Nolan's Pain(t)");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            event.consume(); // overrides default window closing
            attemptExit(imageController, fileService);
        });
        
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }

    private void attemptExit(
        ImageController imageController,
        FileService fileService
    ) {
        if (imageController.getDrawingCanvas().isModified()) {
            Optional<ButtonType> result = showUnsavedChangesAlert();
            if (result.isEmpty()) {
                return;
            }

            ButtonType selectedButton = result.get();
            if (selectedButton == saveButton) {
                try {
                    fileService.saveImage(imageController.getModifiedImage());
                    imageController.getDrawingCanvas().setModified(false);
                    Platform.exit();
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
            } else if (selectedButton == dontSaveButton) {
                Platform.exit();
            }
        } else {
            Platform.exit();
        }
    }

    private void attemptOpen(
        Stage stage,
        ImageController imageController,
        FileService fileService
    ) {
        if (imageController.getDrawingCanvas().isModified()) {
            Optional<ButtonType> result = showUnsavedChangesAlert();
            if (result.isEmpty()) {
                return;
            }

            ButtonType selectedButton = result.get();
            if (selectedButton == saveButton) {
                try {
                    fileService.saveImage(
                        imageController.getModifiedImage()
                    );
                    imageController
                        .getDrawingCanvas()
                        .setModified(false);
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                    return;
                }
            } else if (selectedButton == cancelButton) {
                return;
            }
        }

        Image image = fileService.openImage(stage);
        if (image != null) {
            imageController.setImage(image);
        }
    }

    private Optional<ButtonType> showUnsavedChangesAlert() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes!");
        alert.setContentText("Would you like to save your changes");

        alert.getButtonTypes().setAll( // set instead of add to replace default confirmation buttons
            saveButton,
            dontSaveButton,
            cancelButton
        );

        return alert.showAndWait();
    }
    
}
