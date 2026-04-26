package com.kotva.presentation.component;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * Draws the settings gear icon.
 */
public class SettingsGearIconView extends StackPane {
    private static final String GEAR_IMAGE_PATH = "/images/settings/gear.png";

    private final ImageView imageView;

    public SettingsGearIconView() {
        this.imageView = new ImageView(loadGearImage());
        initialize();
    }

    private void initialize() {
        getStyleClass().add("settings-icon");
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setMouseTransparent(true);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        getChildren().add(imageView);
    }

    private Image loadGearImage() {
        return new Image(
            Objects.requireNonNull(
                getClass().getResource(GEAR_IMAGE_PATH),
                "Missing settings gear image: " + GEAR_IMAGE_PATH)
                .toExternalForm());
    }
}
