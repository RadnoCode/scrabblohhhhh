package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class SwitchButton extends CommonButton {
    private static final double DEFAULT_SWITCH_TRIGGER_WIDTH = 172;
    private static final double DEFAULT_SWITCH_TRIGGER_HEIGHT = 40;
    private static final double SWITCH_TRIGGER_RIGHT_OFFSET = 16;

    public enum SwitchTriggerTone {
        AUTO,
        DARK_SURFACE,
        LIGHT_SURFACE
    }

    private final Label leftLabel;
    private final Label valueLabel;
    private final StackPane switchTrigger;
    private Runnable onSwitchAction;
    private SwitchTriggerTone switchTriggerTone = SwitchTriggerTone.AUTO;

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
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        switchTrigger.getStyleClass().add("switch-trigger");
        switchTrigger.setAlignment(Pos.CENTER_RIGHT);
        setSwitchTriggerSize(DEFAULT_SWITCH_TRIGGER_WIDTH, DEFAULT_SWITCH_TRIGGER_HEIGHT);
        applySwitchTriggerTone();

        switchTrigger.setOnMousePressed(event -> {
            triggerSwitch();
            event.consume();
        });

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.prefWidthProperty().bind(widthProperty());
        content.minWidthProperty().bind(widthProperty());
        content.maxWidthProperty().bind(widthProperty());
        content.setLeft(leftLabel);
        content.setRight(switchTrigger);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(switchTrigger, Pos.CENTER_RIGHT);
        BorderPane.setMargin(switchTrigger, new javafx.geometry.Insets(0, SWITCH_TRIGGER_RIGHT_OFFSET, 0, 0));

        setButtonContent(content);
    }

    public void setCurrentValue(String value) {
        valueLabel.setText(value);
    }

    public void setSwitchTriggerTone(SwitchTriggerTone tone) {
        switchTriggerTone = tone == null ? SwitchTriggerTone.AUTO : tone;
        applySwitchTriggerTone();
    }

    public void setSwitchTriggerSize(double width, double height) {
        switchTrigger.setMinWidth(width);
        switchTrigger.setPrefWidth(width);
        switchTrigger.setMaxWidth(width);
        switchTrigger.setMinHeight(height);
        switchTrigger.setPrefHeight(height);
        switchTrigger.setMaxHeight(height);
    }

    @Override
    public void setTemplateState(TemplateState templateState) {
        super.setTemplateState(templateState);
        applySwitchTriggerTone();
    }

    public void setOnSwitchAction(SwitchAction switchAction) {
        this.onSwitchAction = () -> setCurrentValue(switchAction.onSwitch());
    }

    public void triggerSwitch() {
        playClickSound();
        if (onSwitchAction != null) {
            onSwitchAction.run();
        }
    }

    public boolean isSwitchTriggerTarget(Node node) {
        Node current = node;
        while (current != null) {
            if (current == switchTrigger) {
                return true;
            }
            if (current == this) {
                return false;
            }
            current = current.getParent();
        }
        return false;
    }

    private void applySwitchTriggerTone() {
        switchTrigger.getStyleClass().removeAll("switch-trigger-dark", "switch-trigger-light");
        valueLabel.getStyleClass().removeAll("switch-button-value-dark", "switch-button-value-light");
        SwitchTriggerTone resolvedTone = resolveSwitchTriggerTone();
        if (resolvedTone == SwitchTriggerTone.DARK_SURFACE) {
            switchTrigger.getStyleClass().add("switch-trigger-dark");
            valueLabel.getStyleClass().add("switch-button-value-dark");
        } else {
            switchTrigger.getStyleClass().add("switch-trigger-light");
            valueLabel.getStyleClass().add("switch-button-value-light");
        }
    }

    private SwitchTriggerTone resolveSwitchTriggerTone() {
        if (switchTriggerTone != SwitchTriggerTone.AUTO) {
            return switchTriggerTone;
        }
        return getTemplateState() == TemplateState.TEMPLATE_1
            ? SwitchTriggerTone.DARK_SURFACE
            : SwitchTriggerTone.LIGHT_SURFACE;
    }

    @FunctionalInterface
    public interface SwitchAction {

        String onSwitch();
    }
}
