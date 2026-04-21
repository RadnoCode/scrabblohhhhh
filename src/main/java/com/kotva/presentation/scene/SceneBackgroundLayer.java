package com.kotva.presentation.scene;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

final class SceneBackgroundLayer {
    private static final String BACKGROUND_IMAGE_PATH = "/images/home/Background.png";

    private SceneBackgroundLayer() {
    }

    static ImageView createFor(StackPane sceneRoot) {
        Image backgroundImage = new Image(SceneBackgroundLayer.class.getResource(BACKGROUND_IMAGE_PATH).toExternalForm());
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setPreserveRatio(false);
        backgroundView.setSmooth(true);
        backgroundView.setMouseTransparent(true);
        backgroundView.fitWidthProperty().bind(sceneRoot.widthProperty());
        backgroundView.fitHeightProperty().bind(sceneRoot.heightProperty());
        return backgroundView;
    }
}
