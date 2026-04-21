package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class TitleBanner extends StackPane {
    private static final String TITLE_IMAGE_PATH = "/images/home/title.png";
    private static final double PREF_HEIGHT = 120;
    private static final double TITLE_OFFSET_Y = 30;

    private final ImageView titleImageView;
    private String titleText;

    public TitleBanner(String titleText) {
        this.titleText = titleText == null ? "" : titleText.trim();
        this.titleImageView = new ImageView(loadTitleImage());

        setAlignment(Pos.CENTER);
        setMaxWidth(Double.MAX_VALUE);
        setPrefHeight(PREF_HEIGHT);
        setMinHeight(PREF_HEIGHT);
        setTranslateY(TITLE_OFFSET_Y);

        titleImageView.setPreserveRatio(true);
        titleImageView.setSmooth(true);
        titleImageView.fitWidthProperty().bind(widthProperty());
        titleImageView.fitHeightProperty().bind(heightProperty());
        setAccessibleText(this.titleText);

        getChildren().add(titleImageView);
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText == null ? "" : titleText.trim();
        setAccessibleText(this.titleText);
    }

    public String getTitleText() {
        return titleText;
    }

    private Image loadTitleImage() {
        return new Image(
            Objects.requireNonNull(
                TitleBanner.class.getResource(TITLE_IMAGE_PATH),
                "Missing title banner image: " + TITLE_IMAGE_PATH)
                .toExternalForm());
    }
}
