package com.example.paintapp.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class MenuBarFactory {

    private final MenuBar menuBar;

    private final MenuItem openItem;
    private final MenuItem saveItem;
    private final MenuItem saveAsItem;
    private final MenuItem exitItem;

    public MenuBarFactory() {
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

        menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);
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
    
}
