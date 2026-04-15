package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class LockedButton extends CommonButton {
    private final Label leftLabel;
    private final Label valueLabel;
    private final LockIconView lockIconView;

    public LockedButton(String labelText, String valueText) {
        super();
        this.leftLabel = new Label(labelText);
        this.valueLabel = new Label(valueText);
        this.lockIconView = new LockIconView();
        initializeLockedButton();
    }

    private void initializeLockedButton() {
        getStyleClass().add("setting-item-button");
        getStyleClass().add("locked-button");
        setText(null);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        leftLabel.getStyleClass().add("setting-item-label");
        valueLabel.getStyleClass().add("locked-button-value");
        lockIconView.setPrefSize(22, 26);

        HBox rightBox = new HBox(10, valueLabel, lockIconView);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane content = new BorderPane();
        content.getStyleClass().add("setting-item-content");
        content.setLeft(leftLabel);
        content.setRight(rightBox);
        BorderPane.setAlignment(leftLabel, Pos.CENTER_LEFT);
        BorderPane.setAlignment(rightBox, Pos.CENTER_RIGHT);

        setGraphic(content);
    }
}