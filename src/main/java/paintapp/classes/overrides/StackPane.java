package paintapp.classes.overrides;

import javafx.scene.Node;
import javafx.scene.image.Image;

public class StackPane extends javafx.scene.layout.StackPane {

    public StackPane() {}
    public StackPane(Node... content) {
       super(content);
    }

    /**
     * Scales the pane to the the given dimensions.
     */
    public void setSize(double width, double height) {
        this.setMinSize(width, height);
        this.setMaxSize(width, height);
        this.setPrefSize(width, height);
    }

    /** 
     * Scales the pane to the size of the given image.
    */
    public void sizeTo(Image image) {
        this.setSize(image.getWidth(), image.getHeight());
    }
    
}
