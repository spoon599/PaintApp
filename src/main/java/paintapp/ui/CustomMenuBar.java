package paintapp.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class CustomMenuBar {

    private final MenuBar menuBar;

    private final MenuItem openItem;
    private final MenuItem saveItem;
    private final MenuItem saveAsItem;
    private final MenuItem exitItem;

    private final MenuItem resizeItem;

    private final MenuItem helpItem;
    private final MenuItem aboutItem;

    public CustomMenuBar() {

        // file
        Menu fileMenu = new Menu("File");

        openItem = new MenuItem("Open");
        saveItem = new MenuItem("Save");
        saveAsItem = new MenuItem("Save As");
        exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(
            openItem,
            saveItem,
            saveAsItem,
            exitItem
        );

        // edit
        Menu editMenu = new Menu("Edit");
        resizeItem = new MenuItem("Resize");
        editMenu.getItems().add(resizeItem);

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

}
