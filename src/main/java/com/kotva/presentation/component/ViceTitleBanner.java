package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ViceTitleBanner extends StackPane {
    private final Label titleLabel;

    public ViceTitleBanner(String titleText) {
        this.titleLabel = new Label(titleText);

        getStyleClass().add("vice-title-banner");
        titleLabel.getStyleClass().add("vice-title-banner-label");

        setAlignment(Pos.CENTER);
        setPrefSize(300, 66);
        setMinSize(300, 66);
        setMaxSize(300, 66);

        getChildren().add(titleLabel);
    }

    public void setTitleText(String titleText) {
        titleLabel.setText(titleText);
    }

    public String getTitleText() {
        return titleLabel.getText();
    }
}