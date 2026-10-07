package paintapp.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Spinner;
import javafx.scene.paint.Color;

import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;

import paintapp.classes.DrawingCanvas.DrawingTool;
import javafx.beans.binding.Bindings;

public class CustomToolBar {
    
    private final ToolBar toolBar;
    private final Slider lineWidthSlider;
    private final ColorPicker colorPicker;

    private final ComboBox<DrawingTool> toolSelector;
    private final Spinner<Integer> polygonSidesSpinner;
    private final CheckBox dashedCheckBox;
    
    public CustomToolBar() {

        colorPicker = new ColorPicker(Color.BLACK);
        Label colorValueLabel = new Label();

        colorValueLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> toHex(colorPicker.getValue()),
                colorPicker.valueProperty()
            )
        );
        
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

        dashedCheckBox = new CheckBox("Dashed outlines");

        polygonSidesSpinner = new Spinner<>(3, Integer.MAX_VALUE, 5);
        polygonSidesSpinner.setPrefWidth(90);

        toolBar = new ToolBar(
            toolSelector,
            lineWidthLabel,
            lineWidthSlider,
            lineWidthValueLabel,
            colorLabel,
            colorPicker,
            colorValueLabel,
            dashedCheckBox,
            new Label("Sides:"),
            polygonSidesSpinner
        );
    }

    /**
     * Converts a color to its hexadecimal RGB representation.
     *
     * @param color the color to describe
     * @return the color formatted as #RRGGBB
     */
    private String toHex(Color color) {
        return String.format(
            "#%02X%02X%02X",
            Math.round(color.getRed() * 255),
            Math.round(color.getGreen() * 255),
            Math.round(color.getBlue() * 255)
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
    public CheckBox getDashedCheckBox() {
        return dashedCheckBox;
    }
    public Spinner<Integer> getPolygonSidesSpinner() {
        return polygonSidesSpinner;
    }

}
