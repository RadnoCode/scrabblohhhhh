package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * SwitchButton is a special common component with a right-side trigger area.
 * Clicking the small right area rotates the current language text.
 */
public class SwitchButton extends CommonButton {
    private final Label leftLabel;
    private final Label valueLabel;
    private final StackPane switchTrigger;
    private Runnable onSwitchAction;

    public SwitchButton(String labelText) {
        super();
        this.leftLabel = new Label(labelText);
        this.valueLabel = new Label();
        this.switchTrigger = new StackPane(valueLabel);
        initializeSwitchButton();
    }

    private void initializeSwitchButton() {
        getStyleClass().add("setting-item-button");
        getStyleClass().add("switch-button");
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        leftLabel.getStyleClass().add("setting-item-label");
        valueLabel.getStyleClass().add("switch-button-value");
        switchTrigger.getStyleClass().add("switch-trigger");
        switchTrigger.setMinWidth(180);
        switchTrigger.setPrefHeight(42);

        switchTrigger.setOnMouseClicked(event -> {
            if (onSwitchAction != null) {
                onSwitchAction.run();
            }
            event.consume();
        });

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.setLeft(leftLabel);
        content.setRight(switchTrigger);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(switchTrigger, Pos.CENTER_RIGHT);

        setGraphic(content);
    }

    public void setCurrentValue(String value) {
        valueLabel.setText(value);
    }

    public void setOnSwitchAction(SwitchAction switchAction) {
        this.onSwitchAction = () -> setCurrentValue(switchAction.onSwitch());
    }

    @FunctionalInterface
    public interface SwitchAction {
        String onSwitch();
    }
}
