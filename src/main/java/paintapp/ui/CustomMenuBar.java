package paintapp.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class CustomMenuBar {

    private final MenuBar menuBar;

    private final MenuItem newItem;
    private final MenuItem openItem;
    private final MenuItem saveItem;
    private final MenuItem saveAsItem;
    private final MenuItem exitItem;

    private final MenuItem resizeItem;

    private final MenuItem helpItem;
    private final MenuItem aboutItem;

    private final MenuItem undoItem;
    private final MenuItem redoItem;
    private final MenuItem clearItem;

    public CustomMenuBar() {

        // file
        Menu fileMenu = new Menu("File");

        newItem = new MenuItem("New");
        newItem.setAccelerator(
            new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
        );
        
        openItem = new MenuItem("Open");
        openItem.setAccelerator(
            new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
        );

        exitItem = new MenuItem("Exit");

        saveItem = new MenuItem("Save");
        saveItem.setAccelerator(
            new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
        );

        saveAsItem = new MenuItem("Save As");
        saveAsItem.setAccelerator(
            new KeyCodeCombination(
            KeyCode.S,
            KeyCombination.SHORTCUT_DOWN,
            KeyCombination.SHIFT_DOWN
            )
        );

        fileMenu.getItems().addAll(
            newItem,
            openItem,
            saveItem,
            saveAsItem,
            exitItem
        );

        // edit
        Menu editMenu = new Menu("Edit");
        undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN)
        );

        redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(
            new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN)
        );

        resizeItem = new MenuItem("Resize");
        clearItem = new MenuItem("Clear Canvas");

        editMenu.getItems().addAll(
            undoItem,
            redoItem,
            resizeItem,
            clearItem
        );

        // help
        Menu helpMenu = new Menu("Help");

        helpItem = new MenuItem("Help");
        aboutItem = new MenuItem("About");

        helpMenu.getItems().addAll(
            helpItem,
            aboutItem
        );

        // bar assembly
        menuBar = new MenuBar();
        menuBar.getMenus().addAll(
            fileMenu,
            editMenu,
            helpMenu
        );
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }
    public MenuItem getOpenItem() {
        return openItem;
    }
    public MenuItem getSaveItem() {
        return saveItem;
    }
    public MenuItem getSaveAsItem() {
        return saveAsItem;
    }
    public MenuItem getExitItem() {
        return exitItem;
    }
    public MenuItem getHelpItem() {
        return helpItem;
    }
    public MenuItem getAboutItem() {
        return aboutItem;
    }
    public MenuItem getResizeItem() {
        return resizeItem;
    }
    public MenuItem getNewItem() {
        return newItem;
    }
    public MenuItem getUndoItem() {
        return undoItem;
    }
    public MenuItem getRedoItem() {
        return redoItem;
    }
    public MenuItem getClearItem() {
        return clearItem;
    }

}
