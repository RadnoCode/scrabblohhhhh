package com.kotva.presentation.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class SettingsValueButton extends CommonButton {
    private static final double VALUE_WIDTH = 190;
    private static final double VALUE_RIGHT_OFFSET = 104;

    private final Label valueLabel;

    public SettingsValueButton(String valueText) {
        super();
        this.valueLabel = new Label(valueText);
        initializeSettingsValueButton();
    }

    private void initializeSettingsValueButton() {
        getStyleClass().add("settings-value-button");
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        valueLabel.getStyleClass().add("settings-value-button-value");
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        valueLabel.setMinWidth(VALUE_WIDTH);
        valueLabel.setPrefWidth(VALUE_WIDTH);
        valueLabel.setMaxWidth(VALUE_WIDTH);
        valueLabel.setMouseTransparent(true);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("settings-value-button-content");
        content.prefWidthProperty().bind(widthProperty());
        content.minWidthProperty().bind(widthProperty());
        content.maxWidthProperty().bind(widthProperty());
        content.setRight(valueLabel);
        BorderPane.setAlignment(valueLabel, Pos.CENTER_RIGHT);
        BorderPane.setMargin(valueLabel, new Insets(0, VALUE_RIGHT_OFFSET, 0, 0));

        setButtonContent(content);
    }
}
