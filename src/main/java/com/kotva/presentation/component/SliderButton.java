package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SliderButton extends CommonButton {
    private final Label leftLabel;
    private final Slider slider;
    private final Label valueLabel;
    private HBox sliderBox;
    private ValueChangeListener valueChangeListener;

    public SliderButton(String labelText) {
        super();
        this.leftLabel = new Label(labelText);
        this.slider = new Slider(0, 100, 60);
        this.valueLabel = new Label("60");
        initializeSliderButton();
    }

    private void initializeSliderButton() {
        getStyleClass().add("setting-item-button");
        getStyleClass().add("slider-button");
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        leftLabel.getStyleClass().add("setting-item-label");
        slider.getStyleClass().add("slider-button-control");
        valueLabel.getStyleClass().add("slider-value-label");
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        slider.setPrefWidth(136);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
                double value = newValue.doubleValue();
                valueLabel.setText(String.valueOf((int) Math.round(value)));
                if (valueChangeListener != null) {
                    valueChangeListener.onValueChanged(value);
                }
            });

        sliderBox = new HBox(10, slider, valueLabel);
        sliderBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.setLeft(leftLabel);
        content.setRight(sliderBox);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(sliderBox, Pos.CENTER_RIGHT);

        setButtonContent(content);
    }

    public void setSliderValue(double value) {
        slider.setValue(value);
        valueLabel.setText(String.valueOf((int) Math.round(value)));
    }

    public void setSliderWidth(double width) {
        slider.setMinWidth(width);
        slider.setPrefWidth(width);
        slider.setMaxWidth(width);
    }

    public void setSliderRightOffset(double rightOffset) {
        BorderPane.setMargin(sliderBox, new Insets(0, rightOffset, 0, 0));
    }

    public void setOnValueChanged(ValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    @Override
    protected void playClickSound() {
        // Slider interactions should stay silent to avoid noisy feedback while adjusting values.
    }

        @FunctionalInterface
    public interface ValueChangeListener {

        void onValueChanged(double value);
    }
}
