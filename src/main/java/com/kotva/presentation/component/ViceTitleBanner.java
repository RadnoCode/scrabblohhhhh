package com.kotva.presentation.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class ViceTitleBanner extends StackPane {
    private static final double PREF_WIDTH = 300;
    private static final double PREF_HEIGHT = 198;

    private final Label titleLabel;
    private final String imagePath;
    private String titleText;

    public ViceTitleBanner(String titleText) {
        this(titleText, null);
    }

    public ViceTitleBanner(String titleText, String imagePath) {
        this.titleText = titleText == null ? "" : titleText.trim();
        this.imagePath = imagePath;
        this.titleLabel = imagePath == null ? createTextLabel(this.titleText) : null;

        setAlignment(Pos.CENTER);
        setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        setMinSize(PREF_WIDTH, PREF_HEIGHT);
        setMaxSize(PREF_WIDTH, PREF_HEIGHT);
        setAccessibleText(this.titleText);

        if (imagePath == null) {
            getChildren().add(titleLabel);
        } else {
            getChildren().add(createImageView(imagePath));
        }
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText == null ? "" : titleText.trim();
        setAccessibleText(this.titleText);
        if (titleLabel != null) {
            titleLabel.setText(this.titleText);
        }
    }

    public String getTitleText() {
        return titleText;
    }

    private Label createTextLabel(String titleText) {
        Label label = new Label(titleText);
        getStyleClass().add("vice-title-banner");
        label.getStyleClass().add("vice-title-banner-label");
        return label;
    }

    private ImageView createImageView(String imagePath) {
        ImageView imageView = new ImageView(loadImage(imagePath));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        return imageView;
    }

    private Image loadImage(String imagePath) {
        return new Image(
            Objects.requireNonNull(
                ViceTitleBanner.class.getResource(imagePath),
                "Missing vice title banner image: " + imagePath)
                .toExternalForm());
    }
}
