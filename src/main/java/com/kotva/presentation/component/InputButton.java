package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;

public class InputButton extends CommonButton {
    private final Label leftLabel;
    private final TextField textField;

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
        textField.setPromptText("Enter here");
        textField.setPrefWidth(180);
        textField.setContextMenu(null);
        textField.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, ContextMenuEvent::consume);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.setLeft(leftLabel);
        content.setRight(textField);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(textField, Pos.CENTER_RIGHT);

        setGraphic(content);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setInputText(String text) {
        textField.setText(text);
    }

    public void enableNumericOnlyInput() {
        textField.setTextFormatter(new TextFormatter<>(change -> {
                String nextText = change.getControlNewText();
                return nextText.matches("\\d*") ? change : null;
            }));
    }
}