package paintapp;

import paintapp.ui.CustomMenuBar;
import paintapp.ui.CustomToolBar;
import paintapp.utils.ExceptionHandler;
import paintapp.utils.ImageTransform;
import paintapp.classes.DrawingCanvas.DrawingTool;
import paintapp.classes.ImageTab;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import paintapp.services.ActivityLogger;
import paintapp.services.Desktop;
import paintapp.services.WebServer;

import java.nio.file.Path;
import java.util.ArrayList;

public class Main extends Application {

    private final ButtonType saveButton = new ButtonType("Save");
    private final ButtonType dontSaveButton = new ButtonType("Don't Save");
    private final ButtonType cancelButton = new ButtonType("Cancel");

    private static final int AUTOSAVE_INTERVAL_SECONDS = 60;
    private int autosaveSecondsRemaining = AUTOSAVE_INTERVAL_SECONDS;
    private Timeline autosaveTimer;

    private WebServer webServer;
    private Timeline webRefreshTimer;
    private final ActivityLogger activityLogger = new ActivityLogger(
        Path.of(
            System.getProperty("user.home"),
            "PaintApp-logs",
            "activity.log"
        )
    );

    private final Desktop desktop = new Desktop();

    @Override
    public void start(Stage stage) {

        activityLogger.log("PaintApp", "App opened");

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

        // turn autosave on or off
        toolbar.getAutosaveEnabledCheckBox().setOnAction(event -> {
            boolean enabled =
                toolbar.getAutosaveEnabledCheckBox().isSelected();
        
            // restart the countdown when the setting changes
            autosaveSecondsRemaining = AUTOSAVE_INTERVAL_SECONDS;
        
            toolbar.getAutosaveCountdownLabel().setText(
                enabled ? "Autosave in: 60 s" : "Autosave off"
            );
        
            activityLogger.log(
                "application",
                "Autosave " + (enabled ? "enabled" : "disabled")
            );
        
            if (toolbar.getNotificationsEnabledCheckBox().isSelected()) {
                desktop.showAutosaveNotification(
                    enabled ? "Autosave enabled." : "Autosave disabled."
                );
            }
        });

        // turn autosave notifications on or off
        toolbar.getNotificationsEnabledCheckBox().setOnAction(event -> {
            boolean enabled =
                toolbar.getNotificationsEnabledCheckBox().isSelected();
        
            activityLogger.log(
                "application",
                "Autosave notifications " + (enabled ? "enabled" : "disabled")
            );
        
            if (enabled) {
                desktop.showAutosaveNotification("Notifications enabled.");
            }
        });

        toolbar.getLineWidthSlider().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getColorPicker().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getToolSelector().valueProperty().addListener(
            (observable, oldValue, newValue) -> {
                applyToolbarSettings(tabPane, toolbar);

                // log current tool in current tab
                if (tabPane.getSelectionModel().getSelectedItem() instanceof ImageTab selectedTab) {
                    activityLogger.log(
                        selectedTab.getText(),
                        newValue + " tool selected"
                    );
                }
            }
        );

        toolbar.getDashedCheckBox().selectedProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );
        toolbar.getPolygonSidesSpinner().valueProperty().addListener(
            (observable, oldValue, newValue) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) ->
                applyToolbarSettings(tabPane, toolbar)
        );

        toolbar.getStarPointsSpinner().valueProperty().addListener(
            (observable, oldValue, newValue) ->
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

                        // update autosave timer
                        autosaveSecondsRemaining = AUTOSAVE_INTERVAL_SECONDS;
                        toolbar.getAutosaveCountdownLabel().setText(
                            "Autosave in: " + autosaveSecondsRemaining + " s"
                        );
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

                        // update autosave timer
                        autosaveSecondsRemaining = AUTOSAVE_INTERVAL_SECONDS;
                        toolbar.getAutosaveCountdownLabel().setText(
                            "Autosave in: " + autosaveSecondsRemaining + " s"
                        );
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

                    if (Double.isFinite(width) && Double.isFinite(height) && width > 0 && height > 0) {
                        controller.recordBeforeEdit();
                        controller.clearSelection();

                        controller.getDrawingCanvas().resizeDrawing(width, height);
                        controller.getStackPane().setSize(width, height);
                        controller.getDrawingCanvas().setModified(true);
                    }
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
            }
        });

        // Undo
        menu.getUndoItem().setOnAction(event -> {
            if (tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab) {
                selectedTab.getImageController().undo();
            }
        });

        // Redo
        menu.getRedoItem().setOnAction(event -> {
            if (tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab) {
                selectedTab.getImageController().redo();
            }
        });

        // Copy
        menu.getCopyItem().setOnAction(event -> {
            if (!(tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab)) {
                return;
            }

            Image copiedImage = selectedTab.getImageController().copySelection();

            if (copiedImage != null) {
                ClipboardContent content = new ClipboardContent();
                content.putImage(copiedImage);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        // Paste
        menu.getPasteItem().setOnAction(event -> {
            if (!(tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab)) {
                return;
            }

            Clipboard clipboard = Clipboard.getSystemClipboard();

            if (clipboard.hasImage()) {
                selectedTab.getImageController().beginPaste(
                    clipboard.getImage()
                );
            }
        });

        // Move selection
        menu.getMoveItem().setOnAction(event -> {
            if (tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab) {
                selectedTab.getImageController().beginMove();
            }
        });

        // Add text
        menu.getTextItem().setOnAction(event -> {
            if (!(tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab)) {
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(stage);
            dialog.setTitle("Add Text");
            dialog.setHeaderText("Enter text and its font size.");

            TextField textField = new TextField();
            TextField sizeField = new TextField("24");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(new Label("Text:"), 0, 0);
            grid.add(textField, 1, 0);
            grid.add(new Label("Font size:"), 0, 1);
            grid.add(sizeField, 1, 1);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
            );

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.OK
                    || textField.getText().isBlank()) {
                return;
            }

            double fontSize;

            try {
                fontSize = Double.parseDouble(sizeField.getText());

                if (!Double.isFinite(fontSize)
                        || fontSize < 1 || fontSize > 512) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                Alert error = new Alert(AlertType.ERROR);
                error.initOwner(stage);
                error.setTitle("Invalid Font Size");
                error.setHeaderText("Enter a font size between 1 and 512.");
                error.showAndWait();
                return;
            }

            Text text = new Text(textField.getText());
            text.setFont(Font.font(fontSize));
            text.setFill(toolbar.getColorPicker().getValue());

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);

            Image textImage = text.snapshot(parameters, null);
            selectedTab.getImageController().beginPaste(textImage);
        });

        // Clear Canvas
        menu.getClearItem().setOnAction(event -> {
            if (!(tabPane.getSelectionModel().getSelectedItem()
                    instanceof ImageTab selectedTab)) {
                return;
            }
        
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.initOwner(stage);
            confirmation.setTitle("Clear Canvas");
            confirmation.setHeaderText("Clear the entire image?");
            confirmation.setContentText(
                "This will replace the image and all drawings with white. "
                + "You can undo this action."
            );
        
            Optional<ButtonType> result = confirmation.showAndWait();
        
            if (result.isPresent() && result.get() == ButtonType.OK) {
                selectedTab.getImageController().clearCanvas();
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

        // add transform options for the canvas and selection
        Menu transformMenu = new Menu("Transform");

        transformMenu.getItems().addAll(
            createTransformMenu("Whole canvas", false, tabPane),
            createTransformMenu("Selection", true, tabPane)
        );

        // place transform before the help menu
        menu.getMenuBar().getMenus().add(2, transformMenu);

        // setup logging for all menus in the menu bar
        for (Menu currentMenu : menu.getMenuBar().getMenus()) {
            setupMenuLogging(currentMenu, tabPane);
        }
        
        // create the scene and set it on the stage
        Scene scene = new Scene(root, 1500, 800); // parent to root and set dimensions to 1500x800
        stage.setTitle("Nolan's Pain(t)");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            event.consume(); // overrides default window closing
            attemptExit(stage, tabPane);
        });
        
        stage.show();
        setupAutosave(tabPane, toolbar);
        setupWebServer(tabPane);

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
            canvas.setPolygonSides(toolbar.getPolygonSidesSpinner().getValue());
            canvas.setStarPoints(toolbar.getStarPointsSpinner().getValue());
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

        controller.getDrawingCanvas().addEventHandler( // log each mouse release event
            MouseEvent.MOUSE_RELEASED,
            event -> {
                var tool = controller.getDrawingCanvas().getCurrentTool();
                activityLogger.log(
                    tab.getText(),
                    tool + " used at "
                        + (int) event.getX() + ", "
                        + (int) event.getY()
                );
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

    /**
     * Sets up the autosave timer for FileService's save backup method
     * @param tabPane
     * @param toolbar
     */
    private void setupAutosave(TabPane tabPane, CustomToolBar toolbar) {
        autosaveTimer = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                if (!toolbar.getAutosaveEnabledCheckBox().isSelected()) {
                    return; // skip autosaving when disabled
                }
                autosaveSecondsRemaining--;

                if (autosaveSecondsRemaining <= 0) {
                    boolean allSaved = true;

                    for (var tab : tabPane.getTabs()) {
                        if (tab instanceof ImageTab imageTab) {
                            try {
                                imageTab.getFileService().saveBackup(
                                    imageTab.getImageController()
                                        .getModifiedImage()
                                );

                                // log after the backup saves successfully
                                activityLogger.log(
                                    imageTab.getText(),
                                    "Autosave backup saved"
                                );
                            } catch (Exception e) {
                                allSaved = false;
                                ExceptionHandler.printFormattedException(e);
                            }
                        }
                    }

                    // notify the user if backup properly saved
                    if (!allSaved && toolbar.getNotificationsEnabledCheckBox().isSelected()) {
                        desktop.showAutosaveNotification(
                            "Autosave failed. Check the application."
                        );
                    }

                    autosaveSecondsRemaining = AUTOSAVE_INTERVAL_SECONDS;

                    toolbar.getAutosaveCountdownLabel().setText(
                        allSaved
                            ? "Backups saved. Next: "
                                + autosaveSecondsRemaining + " s"
                            : "Autosave failed. Retry in: "
                                + autosaveSecondsRemaining + " s"
                    );
                } else {
                    toolbar.getAutosaveCountdownLabel().setText(
                        "Autosave in: " + autosaveSecondsRemaining + " s"
                    );
                }
            })
        );

        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }

    /**
     * Stops the autosave timer (called automatically by javafx)
     * Stops any active webserver
     */
    @Override
    public void stop() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
        }

        if (webRefreshTimer != null) {
            webRefreshTimer.stop();
        }

        if (webServer != null) {
            webServer.stop();
        }

        activityLogger.log("PaintApp", "App closed");
        activityLogger.close();

        desktop.close();
    }

    /**
     * Sets up logging for all menu items within the given menu.
     * @param menu the menu to set up logging for
     * @param tabPane the tab pane to reference for the current tab's name
     */
    private void setupMenuLogging(Menu menu, TabPane tabPane) {
        for (var item : menu.getItems()) {
            if (item instanceof Menu submenu) {
                setupMenuLogging(submenu, tabPane);
            } else {
                item.addEventHandler(ActionEvent.ACTION, event -> {
                    String filename = "new tab";

                    // get the current tab's name
                    if (tabPane.getSelectionModel().getSelectedItem()
                            instanceof ImageTab selectedTab) {
                        filename = selectedTab.getText();
                    }

                    activityLogger.log(
                        filename,
                        menu.getText() + ": " + item.getText() + " requested"
                    );
                });
            }
        }
    }

    // create rotate and flip options for one target
    private Menu createTransformMenu(
            String title,
            boolean selectionOnly,
            TabPane tabPane
    ) {
        Menu menu = new Menu(title);

        for (var operation : ImageTransform.Transform.values()) {
            String label = switch (operation) {
                case ROTATE_90 -> "Rotate 90° clockwise";
                case ROTATE_180 -> "Rotate 180°";
                case ROTATE_270 -> "Rotate 270° clockwise";
                case FLIP_HORIZONTAL -> "Flip horizontally";
                case FLIP_VERTICAL -> "Flip vertically";
            };

            MenuItem item = new MenuItem(label);

            // disable the option when no tab is open
            item.disableProperty().bind(
                tabPane.getSelectionModel()
                .selectedItemProperty()
                .isNull());

            item.setOnAction(event -> {
                if (tabPane.getSelectionModel().getSelectedItem() instanceof ImageTab selectedTab) {
                    boolean changed = selectedTab
                    .getImageController()
                    .transform(operation, selectionOnly);

                    if (!changed) {
                        Alert alert = new Alert(
                            AlertType.INFORMATION,
                            "Choose the Select tool and drag an area first."
                        );

                        alert.initOwner(tabPane.getScene().getWindow());
                        alert.showAndWait();
                    }
                }
            });

            menu.getItems().add(item);
        }

        return menu;
    }

    /**
     * Sets up a web server
     * @param tabPane
     */
    private void setupWebServer(TabPane tabPane) {
        webServer = new WebServer();

        try {
            webServer.start();
        } catch (Exception e) {
            ExceptionHandler.printFormattedException(e);

            Alert error = new Alert(AlertType.ERROR);
            error.setTitle("Webserver Error");
            error.setHeaderText("Could not start the gallery server.");
            error.setContentText("Port 8000 might be being used by another application.");
            error.showAndWait();
            return;
        }

        webRefreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(3), event -> {
                ArrayList<Image> sharedImages = new ArrayList<>();

                try {
                    for (var tab : tabPane.getTabs()) {
                        if (tab instanceof ImageTab imageTab
                                && imageTab.isSharedOnWeb()) {
                            sharedImages.add(
                                imageTab.getImageController()
                                    .getModifiedImage()
                            );
                        }
                    }

                    webServer.updateImages(sharedImages);
                } catch (Exception e) {
                    ExceptionHandler.printFormattedException(e);
                }
            })
        );

        webRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        webRefreshTimer.play();
    }
    
}