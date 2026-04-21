
package com.kotva.presentation.component;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

abstract class AbstractEnvelopeGifView extends StackPane {
    private static final double ENVELOPE_WIDTH = 495;
    private static final double ENVELOPE_HEIGHT = 372;

    private final String resourcePath;
    private final ImageView imageView;

    protected AbstractEnvelopeGifView(String resourcePath) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath cannot be null.");
        this.imageView = new ImageView();
        initialize();
    }


    private void initialize() {
        getStyleClass().add("home-envelope-gif");
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMouseTransparent(true);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setFitWidth(ENVELOPE_WIDTH);
        imageView.setFitHeight(ENVELOPE_HEIGHT);
        imageView.setMouseTransparent(true);
        imageView.setImage(loadImage());

        getChildren().add(imageView);
    }

    public void activate() {
        toFront();
        imageView.setImage(loadImage());
    }

    private Image loadImage() {
        return new Image(
                Objects.requireNonNull(
                                getClass().getResource(resourcePath),
                                "Cannot find envelope gif resource: " + resourcePath)
                        .toExternalForm(),
                false);
    }
}