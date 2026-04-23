package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class InputButton extends CommonButton {
    private static final double DEFAULT_INPUT_FIELD_WIDTH = 172;
    private static final double DEFAULT_INPUT_FIELD_HEIGHT = 40;
    private static final double INPUT_FIELD_RIGHT_OFFSET = 16;

    public enum InputFieldTone {
        AUTO,
        DARK_SURFACE,
        LIGHT_SURFACE
    }

    private final Label leftLabel;
    private final TextField textField;
    private InputFieldTone inputFieldTone = InputFieldTone.AUTO;

    public InputButton(String labelText) {
        super();
        this.leftLabel = new Label(labelText);
        this.textField = new TextField();
        initializeInputButton();
    }

    private void initializeInputButton() {
        getStyleClass().add("setting-item-button");
        getStyleClass().add("input-button");
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        leftLabel.getStyleClass().add("setting-item-label");
        textField.getStyleClass().add("input-button-field");
        textField.setAlignment(Pos.CENTER_RIGHT);
        textField.setPromptText("Enter here");
        setInputFieldSize(DEFAULT_INPUT_FIELD_WIDTH, DEFAULT_INPUT_FIELD_HEIGHT);
        textField.setContextMenu(null);
        textField.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ContextMenuEvent::consume);
        applyInputFieldTone();

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isTextFieldTarget(event.getTarget())) {
                focusInputField();
                event.consume();
            }
        });

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.prefWidthProperty().bind(widthProperty());
        content.minWidthProperty().bind(widthProperty());
        content.maxWidthProperty().bind(widthProperty());
        content.setLeft(leftLabel);
        content.setRight(textField);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(textField, Pos.CENTER_RIGHT);
        BorderPane.setMargin(textField, new javafx.geometry.Insets(0, INPUT_FIELD_RIGHT_OFFSET, 0, 0));

        setButtonContent(content);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setInputText(String text) {
        textField.setText(text);
    }

    public void setInputFieldTone(InputFieldTone tone) {
        inputFieldTone = tone == null ? InputFieldTone.AUTO : tone;
        applyInputFieldTone();
    }

    public void setInputFieldSize(double width, double height) {
        textField.setMinWidth(width);
        textField.setPrefWidth(width);
        textField.setMaxWidth(width);
        textField.setMinHeight(height);
        textField.setPrefHeight(height);
        textField.setMaxHeight(height);
    }

    @Override
    public void setTemplateState(TemplateState templateState) {
        super.setTemplateState(templateState);
        applyInputFieldTone();
    }

    public void enableNumericOnlyInput() {
        textField.setTextFormatter(new TextFormatter<>(change -> {
                String nextText = change.getControlNewText();
                return nextText.matches("\\d*") ? change : null;
            }));
    }

    private void focusInputField() {
        textField.requestFocus();
        textField.positionCaret(textField.getText() == null ? 0 : textField.getText().length());
    }

    private boolean isTextFieldTarget(Object target) {
        if (!(target instanceof Node node)) {
            return false;
        }
        Node current = node;
        while (current != null) {
            if (current == textField) {
                return true;
            }
            if (current == this) {
                return false;
            }
            current = current.getParent();
        }
        return false;
    }

    private void applyInputFieldTone() {
        textField.getStyleClass().removeAll("input-button-field-dark", "input-button-field-light");
        InputFieldTone resolvedTone = resolveInputFieldTone();
        textField.getStyleClass().add(
            resolvedTone == InputFieldTone.DARK_SURFACE
                ? "input-button-field-dark"
                : "input-button-field-light");
    }

    private InputFieldTone resolveInputFieldTone() {
        if (inputFieldTone != InputFieldTone.AUTO) {
            return inputFieldTone;
        }
        return getTemplateState() == TemplateState.TEMPLATE_1
            ? InputFieldTone.DARK_SURFACE
            : InputFieldTone.LIGHT_SURFACE;
    }
}
