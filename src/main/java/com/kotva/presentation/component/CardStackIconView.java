package com.kotva.presentation.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class CardStackIconView extends StackPane {
    private static final String CARD_IMAGE_RESOURCE_PATH = "/images/card-stack/card-frame-18.png";
    private static final double PREF_WIDTH = 360;
    private static final double PREF_HEIGHT = 270;
    private static final double DISPLAY_SCALE = 2.0;

    private final ImageView imageView;

    public CardStackIconView() {
        this.imageView = new ImageView(loadCardImage());
        initialize();
    }

    private void initialize() {
        setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMouseTransparent(true);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(widthProperty().multiply(DISPLAY_SCALE));
        imageView.fitHeightProperty().bind(heightProperty().multiply(DISPLAY_SCALE));
        imageView.setMouseTransparent(true);

        getChildren().add(imageView);
    }

    private Image loadCardImage() {
        try (InputStream inputStream = Objects.requireNonNull(
            getClass().getResourceAsStream(CARD_IMAGE_RESOURCE_PATH),
            "Cannot find card image resource: " + CARD_IMAGE_RESOURCE_PATH)) {
            return new Image(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read card image resource: " + CARD_IMAGE_RESOURCE_PATH, exception);
        }
    }
}
