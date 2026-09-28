package paintapp;

import paintapp.controllers.ImageController;
import paintapp.services.FileService;
import paintapp.ui.CustomMenuBar;
import paintapp.ui.CustomToolBar;
import paintapp.utils.ExceptionHandler;
import paintapp.classes.DrawingCanvas.DrawingTool;
import paintapp.classes.ImageTab;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class Main extends Application {

    private final ButtonType saveButton = new ButtonType("Save");
    private final ButtonType dontSaveButton = new ButtonType("Don't Save");
    private final ButtonType cancelButton = new ButtonType("Cancel");

    @Override
    public void start(Stage stage) {

        // main controllers and services
        BorderPane root = new BorderPane();
        CustomMenuBar menu = new CustomMenuBar();
        CustomToolBar toolbar = new CustomToolBar();
        ImageTab initialTab = new ImageTab("Untitled");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(initialTab);

        VBox container = new VBox(
            menu.getMenuBar(),
            toolbar.getToolBar()
        );

        toolbar.getLineWidthSlider().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getColorPicker().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getToolSelector().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getDashedCheckBox().selectedProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        applyToolbarSettings(tabPane, toolbar);
        setupColorGrabber(initialTab, toolbar);
        setupTabClosing(stage, tabPane, initialTab);

        root.setTop(container); // set to top container
        root.setCenter(tabPane); // set to our tab pane, top of stack
        // current hierarchy goes tabPane -> scrollPane -> workspace -> stackpane -> imageview + canvas

        // New
        menu.getNewItem().setOnAction(event -> {
            ImageTab newTab = new ImageTab("Untitled");

            setupColorGrabber(newTab, toolbar);
            setupTabClosing(stage, tabPane, newTab);

            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        });

        // Open
        menu.getOpenItem().setOnAction(event -> {
            attemptOpen(stage, tabPane, toolbar);
        });

        // Exit
        menu.getExitItem().setOnAction(event -> {
            attemptExit(stage, tabPane);
        });

        // Save
        menu.getSaveItem().setOnAction(event -> {
            if (tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab) {
                var controller = selectedTab.getImageController();
                var files = selectedTab.getFileService();
                    
                try {
                    boolean saved = files.saveImage(
                        stage,
                        controller.getModifiedImage()
                    );
                
                    if (saved) {
                        controller.getDrawingCanvas().setModified(false);
                        selectedTab.setText(files.getFileName());
            }
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
            }
        });

        // Save As
        menu.getSaveAsItem().setOnAction(event -> {
            if (tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab) {
                var controller = selectedTab.getImageController();
                var files = selectedTab.getFileService();
                    
                try {
                    boolean saved = files.saveImageAs(
                        stage,
                        controller.getModifiedImage()
                    );
                
                    if (saved) {
                        controller.getDrawingCanvas().setModified(false);
                        selectedTab.setText(files.getFileName());
                    }
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
            }
        });

        // Resize
        menu.getResizeItem().setOnAction(event -> {
            if (!(tabPane.getSelectionModel().getSelectedItem() instanceof ImageTab selectedTab)) {
                return;
            }

            var controller = selectedTab.getImageController();

            Dialog<ButtonType> resizeDialog = new Dialog<>();
            resizeDialog.setTitle("Resize Canvas");
            resizeDialog.setHeaderText("Enter the new canvas size.");

            TextField widthField = new TextField();
            TextField heightField = new TextField();

            widthField.setPromptText("Width");
            heightField.setPromptText("Height");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(new Label("Width:"), 0, 0);
            grid.add(widthField, 1, 0);

            grid.add(new Label("Height:"), 0, 1);
            grid.add(heightField, 1, 1);

            resizeDialog.getDialogPane().setContent(grid);
            resizeDialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
            );

            Optional<ButtonType> result = resizeDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    double width = Double.parseDouble(widthField.getText());
                    double height = Double.parseDouble(heightField.getText());

                    if (width > 0 && height > 0) {
                        controller.getDrawingCanvas().resizeDrawing(width, height);
                        controller.getStackPane().setSize(width, height);
                        controller.getDrawingCanvas().setModified(true);
                    }
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
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
            about.setContentText("Version 0.3.0");
            about.showAndWait();
        });
        
        // create the scene and set it on the stage
        Scene scene = new Scene(root, 900, 600); // parent to root and set dimensions to 900x600
        stage.setTitle("Nolan's Pain(t)");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            event.consume(); // overrides default window closing
            attemptExit(stage, tabPane);
        });
        
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Checks all image tabs for unsaved changes before exiting.
     *
     * @param stage the application window
     * @param tabPane the pane containing the image tabs
     */
    private void attemptExit(Stage stage, TabPane tabPane) {
        for (var tab : tabPane.getTabs()) {
            if (tab instanceof ImageTab imageTab) {
                tabPane.getSelectionModel().select(imageTab);

                if (!confirmTabClose(stage, imageTab)) {
                    return;
                }
            }
        }

        Platform.exit();
    }

    /**
     * Opens an image in a new tab.
     *
     * @param stage the application window
     * @param tabPane the pane containing the image tabs
     * @param toolbar the shared drawing controls
     */
    private void attemptOpen(
        Stage stage,
        TabPane tabPane,
        CustomToolBar toolbar
    ) {
        ImageTab newTab = new ImageTab("Untitled");
        var files = newTab.getFileService();

        try {
            Image image = files.openImage(stage);

            if (image == null) {
                return;
            }

            if (image.isError()) {
                throw new IllegalArgumentException(
                    "Could not load the selected image.",
                    image.getException()
                );
            }

            newTab.getImageController().setImage(image);
            newTab.setText(files.getFileName());

            setupColorGrabber(newTab, toolbar);
            setupTabClosing(stage, tabPane, newTab);

            tabPane.getTabs().add(newTab);
            tabPane.getSelectionModel().select(newTab);
        } catch (Exception e) {
            ExceptionHandler.printFormattedException(e);
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

    /**
     * Applies the shared toolbar settings to the selected image tab.
     * @param tabPane the pane containing the image tabs
     * @param toolbar the shared drawing controls
     */
    private void applyToolbarSettings(
        TabPane tabPane,
        CustomToolBar toolbar
    ) {
        if (tabPane.getSelectionModel().getSelectedItem() instanceof ImageTab selectedTab) {
            var canvas = selectedTab.getImageController().getDrawingCanvas();

            canvas.setLineWidth(toolbar.getLineWidthSlider().getValue());
            canvas.setLineColor(toolbar.getColorPicker().getValue());
            canvas.setCurrentTool(toolbar.getToolSelector().getValue());
            canvas.setDashed(toolbar.getDashedCheckBox().isSelected());
        }
    }

    /** 
     * Connects a tab's canvas to the shared color picker.
     * @param tab the image tab to configure
     * @param toolbar the shared drawing controls
     */
    private void setupColorGrabber(
        ImageTab tab,
        CustomToolBar toolbar
    ) {
        var controller = tab.getImageController();

        controller.getDrawingCanvas().addEventHandler(
            MouseEvent.MOUSE_PRESSED,
            event -> {
                if (toolbar.getToolSelector().getValue()
                        == DrawingTool.COLOR_GRABBER) {
                    Color sampledColor = controller.readColorAt(
                        event.getX(),
                        event.getY()
                    );

                    if (sampledColor != null) {
                        toolbar.getColorPicker().setValue(sampledColor);
                    }
                }
            }
        );
    }

    /**
     * Checks whether a tab can close, offering to save changes.
     * @param stage
     * @param tab
     * @return true if can close, false if cancelled or saving fails
     */
    private boolean confirmTabClose(Stage stage, ImageTab tab) {
        var controller = tab.getImageController();

        if (!controller.getDrawingCanvas().isModified()) {
            return true;
        }

        Optional<ButtonType> result = showUnsavedChangesAlert();

        if (result.isEmpty()) {
            return false;
        }

        ButtonType choice = result.get();

        if (choice == dontSaveButton) {
            return true;
        }

        if (choice == saveButton) {
            try {
                boolean saved = tab.getFileService().saveImage(
                    stage,
                    controller.getModifiedImage()
                );

                if (saved) {
                    controller.getDrawingCanvas().setModified(false);
                    tab.setText(tab.getFileService().getFileName());
                }

                return saved;
            } catch (Exception e) {
                ExceptionHandler.printFormattedException(e);
                return false;
            }
        }

        return false;
    }

    /**
     * Enables tab closing with an unsaved-changes check.
     *
     * @param stage the application window
     * @param tabPane the pane containing the tab
     * @param tab the image tab to configure
     */
    private void setupTabClosing(
        Stage stage,
        TabPane tabPane,
        ImageTab tab
    ) {
        tab.setClosable(true);

        tab.setOnCloseRequest(event -> {
            tabPane.getSelectionModel().select(tab);

            if (!confirmTabClose(stage, tab)) {
                event.consume();
            }
        });
    }
    
}

