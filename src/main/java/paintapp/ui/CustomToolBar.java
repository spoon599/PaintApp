package paintapp.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Color;

public class CustomToolBar {
    
    private final ToolBar toolBar;
    private final Slider lineWidthSlider;
    private final ColorPicker colorPicker;

    public CustomToolBar() {
        Label lineWidthLabel = new Label("Line Width:");
        Label colorLabel = new Label("Color:");

        colorPicker = new ColorPicker(Color.BLACK);

        lineWidthSlider = new Slider(
            1, // min
            20, // max
            3 // start
        );

        toolBar = new ToolBar(
            lineWidthLabel,
            lineWidthSlider,
            colorLabel,
            colorPicker
        );
    }

    public ToolBar getToolBar() {
        return toolBar;
    }
    public Slider getLineWidthSlider() {
        return lineWidthSlider;
    }
    public ColorPicker getColorPicker() {
        return colorPicker;
    }

}
