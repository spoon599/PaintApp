package paintapp.classes.overrides;

import javafx.scene.Node;

public class ScrollPane extends javafx.scene.control.ScrollPane {
    
    public ScrollPane() {}
    public ScrollPane(Node content) {
        super(content);
    }

    public void setFitToDimensions(boolean value) {
        this.setFitToWidth(value);
        this.setFitToHeight(value);
    }

}
