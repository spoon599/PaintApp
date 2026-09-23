package paintapp.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;

public class CustomToolBar {
    
    private final ToolBar toolBar;
    private final Slider lineWidthSlider;

    public CustomToolBar() {
        Label lineWidthLabel = new Label("Line Width:");

        lineWidthSlider = new Slider(
            1, // min
            20, // max
            3 // start
        );

        toolBar = new ToolBar(
            lineWidthLabel,
            lineWidthSlider
        );
    }

    public ToolBar getToolBar() {
        return toolBar;
    }
    public Slider getLineWidthSlider() {
        return lineWidthSlider;
    }
    
}
