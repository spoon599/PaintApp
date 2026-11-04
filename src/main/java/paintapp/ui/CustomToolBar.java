package paintapp.ui;

import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
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
    private final Spinner<Integer> starPointsSpinner;
    private final CheckBox dashedCheckBox;

    private final CheckBox showAutosaveCheckBox;
    private final Label autosaveCountdownLabel;
    
    private final CheckBox autosaveEnabledCheckBox;
    private final CheckBox notificationsEnabledCheckBox;
    
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

        starPointsSpinner = new Spinner<>(4, 100, 5);
        starPointsSpinner.setPrefWidth(90);

        showAutosaveCheckBox = new CheckBox("Show autosave countdown");
        showAutosaveCheckBox.setSelected(true);

        autosaveCountdownLabel = new Label("Autosave in: 60 s");
        autosaveCountdownLabel.visibleProperty().bind(
            showAutosaveCheckBox.selectedProperty()
        );
        autosaveCountdownLabel.managedProperty().bind(
            showAutosaveCheckBox.selectedProperty()
        );

        autosaveEnabledCheckBox = new CheckBox("Autosave enabled");
        autosaveEnabledCheckBox.setSelected(true); // default to autosave enabled

        notificationsEnabledCheckBox = new CheckBox("Autosave notifications");
        notificationsEnabledCheckBox.setSelected(true); // default to notifications enabled
        
        autosaveEnabledCheckBox.setTooltip(
            new javafx.scene.control.Tooltip(
                "Save backup copies automatically"
            )
        );

        notificationsEnabledCheckBox.setTooltip(
            new javafx.scene.control.Tooltip(
                "Show desktop notifications for autosave"
            )
        );

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
            polygonSidesSpinner,
            new Label("Star points:"),
            starPointsSpinner,
            showAutosaveCheckBox,
            autosaveCountdownLabel,
            autosaveEnabledCheckBox,
            notificationsEnabledCheckBox
        );

        // tooltips setup
        toolSelector.setTooltip(
            new Tooltip("Choose a drawing tool")
        );

        lineWidthSlider.setTooltip(
            new Tooltip("Change the drawing line width")
        );

        colorPicker.setTooltip(
            new Tooltip("Choose a drawing color")
        );

        dashedCheckBox.setTooltip(
            new Tooltip("Use dashed outlines for shapes")
        );

        polygonSidesSpinner.setTooltip(
            new Tooltip("Choose the number of polygon sides")
        );

        starPointsSpinner.setTooltip(
            new Tooltip("Choose the number of star points")
        );

        showAutosaveCheckBox.setTooltip(
            new Tooltip("Show or hide the autosave countdown")
        );

        addToolButtons();

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

    /**
     * Adds a button for each drawing tool to the toolbar.
     */
    private void addToolButtons() {
        ToggleGroup group = new ToggleGroup();
    
        for (DrawingTool tool : DrawingTool.values()) {
            String icon = switch (tool) {
                case PENCIL -> "✎";
                case LINE -> "╱";
                case RECTANGLE -> "▭";
                case SQUARE -> "□";
                case ELLIPSE -> "⬭";
                case CIRCLE -> "○";
                case TRIANGLE -> "△";
                case DIAMOND -> "◇";
                case POLYGON -> "⬠";
                case STAR -> "☆";
                case RIGHT_TRIANGLE -> "◿";
                case SELECT -> "⬚";
                case COLOR_GRABBER -> "⚗";
            };
        
            ToggleButton button = new ToggleButton(icon);
            button.setToggleGroup(group);
            button.setMinWidth(32);
        
            // show the tool name on hover
            String name = tool.toString().replace('_', ' ');
            button.setTooltip(new Tooltip(name));
            button.setAccessibleText(name);
        
            // show which tool is currently selected
            button.setSelected(tool == toolSelector.getValue());
        
            // change tools when the button is clicked
            button.setOnAction(event -> {
                toolSelector.setValue(tool);
            
                // keep the active tool button selected
                button.setSelected(true);
            });
        
            // update the buttons when the dropdown changes
            toolSelector.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                    button.setSelected(tool == newValue)
            );
        
            // put tool buttons at the start of the toolbar
            toolBar.getItems().add(tool.ordinal(), button);
        }
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
    public Spinner<Integer> getStarPointsSpinner() {
        return starPointsSpinner;
    }
    public Label getAutosaveCountdownLabel() {
        return autosaveCountdownLabel;
    }
    public CheckBox getAutosaveEnabledCheckBox() {
        return autosaveEnabledCheckBox;
    }
    public CheckBox getNotificationsEnabledCheckBox() {
        return notificationsEnabledCheckBox;
    }

}
