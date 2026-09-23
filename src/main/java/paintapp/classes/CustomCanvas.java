package paintapp.classes;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class CustomCanvas extends Canvas {

    public CustomCanvas() {}

    /**
     * Sets the size of the canvas to the given dimensions.
     */
    public void setSize(double width, double height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    /**
     * Sets the size of the canvas to fit the given image's dimensions
     */
    public void sizeTo(Image image) {
        this.setSize(image.getWidth(), image.getHeight());
    }
    
}
