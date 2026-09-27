package paintapp.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.paint.Color;

import javafx.scene.control.ComboBox;
import paintapp.classes.DrawingCanvas.DrawingTool;

public class CustomToolBar {
    
    private final ToolBar toolBar;
    private final Slider lineWidthSlider;
    private final ColorPicker colorPicker;
    private final ComboBox<DrawingTool> toolSelector;

    public CustomToolBar() {

        colorPicker = new ColorPicker(Color.BLACK);
        lineWidthSlider = new Slider(
            1, // min
            20, // max
            3 // start
        );
        
        Label colorLabel = new Label("Color:");
        Label lineWidthLabel = new Label("Line Width:");
        Label lineWidthValueLabel = new Label();
        lineWidthValueLabel.textProperty().bind(
            lineWidthSlider.valueProperty().asString("%.1f px") // bind changes in width slider's value to the value label
        );

        toolSelector = new ComboBox<>();
        toolSelector.getItems().addAll(DrawingTool.values());
        toolSelector.setValue(DrawingTool.PENCIL);

        toolBar = new ToolBar(
            toolSelector,
            lineWidthLabel,
            lineWidthSlider,
            lineWidthValueLabel,
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
    public ComboBox<DrawingTool> getToolSelector() {
        return toolSelector;
    }

}
