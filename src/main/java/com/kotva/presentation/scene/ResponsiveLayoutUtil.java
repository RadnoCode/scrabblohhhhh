package com.kotva.presentation.scene;

import javafx.application.Platform;
import javafx.scene.layout.Region;

/**
 * Helps resize scene layouts.
 */
final class ResponsiveLayoutUtil {
    private ResponsiveLayoutUtil() {
    }

    static void install(Region region, Runnable updater) {
        region.widthProperty().addListener((observable, oldValue, newValue) -> updater.run());
        region.heightProperty().addListener((observable, oldValue, newValue) -> updater.run());
        Platform.runLater(updater);
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    static double resolvedWidth(Region region, double fallbackWidth) {
        return region.getWidth() > 0 ? region.getWidth() : fallbackWidth;
    }

    static double resolvedHeight(Region region, double fallbackHeight) {
        return region.getHeight() > 0 ? region.getHeight() : fallbackHeight;
    }
}
