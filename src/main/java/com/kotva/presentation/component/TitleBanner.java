package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TitleBanner extends StackPane {
    private final Label titleLabel;

    public TitleBanner(String titleText) {
        this.titleLabel = new Label(addLetterSpacing(titleText));

        getStyleClass().add("title-banner");
        titleLabel.getStyleClass().add("title-banner-label");

        setAlignment(Pos.CENTER);
        setMaxWidth(Double.MAX_VALUE);
        setPrefHeight(120);

        getChildren().add(titleLabel);
    }

    public void setTitleText(String titleText) {
        titleLabel.setText(addLetterSpacing(titleText));
    }

    public String getTitleText() {
        return titleLabel.getText().replace(" ", "");
    }

    private String addLetterSpacing(String titleText) {
        String cleanText = titleText == null ? "" : titleText.trim().toUpperCase();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < cleanText.length(); i++) {
            builder.append(cleanText.charAt(i));
            if (i < cleanText.length() - 1) {
                builder.append(' ');
            }
        }

        return builder.toString();
    }
}