package com.example.paintapp.classes;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

public class CustomScrollPane extends ScrollPane {
    
    public CustomScrollPane() {}
    public CustomScrollPane(Node content) {
        super(content);
    }

    public void setFitToDimensions(boolean value) {
        this.setFitToWidth(value);
        this.setFitToHeight(value);
    }

}
