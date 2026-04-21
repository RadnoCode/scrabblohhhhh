package com.kotva.presentation.scene;

import com.kotva.presentation.component.TitleBanner;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;

public final class SceneTitleEntranceAnimationManager {
    private static final Duration TITLE_ENTRANCE_DURATION = Duration.seconds(0.3);
    private static final double TITLE_ENTRY_OFFSET_Y = 180;
    private static final double TITLE_ENTRY_SAFE_MARGIN = 32;

    private SceneTitleEntranceAnimationManager() {
    }

    public static void playEntranceIfPresent(Parent root) {
        if (root == null) {
            return;
        }
        if (Boolean.TRUE.equals(root.getProperties().get(OptionSceneEntranceAnimationManager.OPTION_ENTRANCE_INSTALLED_KEY))) {
            return;
        }

        TitleBanner titleBanner = findTitleBanner(root);
        if (titleBanner == null) {
            return;
        }

        Platform.runLater(() -> playEntrance(titleBanner));
    }

    private static void playEntrance(TitleBanner titleBanner) {
        double restingTranslateY = titleBanner.getTranslateY();
        double startTranslateY = resolveTitleEntryStartTranslateY(titleBanner, restingTranslateY);
        titleBanner.setTranslateY(startTranslateY);

        TranslateTransition transition = new TranslateTransition(TITLE_ENTRANCE_DURATION, titleBanner);
        transition.setFromY(startTranslateY);
        transition.setToY(restingTranslateY);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.playFromStart();
    }

    private static double resolveTitleEntryStartTranslateY(TitleBanner titleBanner, double restingTranslateY) {
        if (titleBanner.getScene() == null) {
            return restingTranslateY - TITLE_ENTRY_OFFSET_Y;
        }

        Bounds sceneBounds = titleBanner.localToScene(titleBanner.getBoundsInLocal());
        if (sceneBounds == null) {
            return restingTranslateY - TITLE_ENTRY_OFFSET_Y;
        }

        double requiredShift = sceneBounds.getMaxY() + TITLE_ENTRY_SAFE_MARGIN;
        return restingTranslateY - Math.max(requiredShift, TITLE_ENTRY_OFFSET_Y);
    }

    private static TitleBanner findTitleBanner(Node node) {
        if (node instanceof TitleBanner titleBanner) {
            return titleBanner;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                TitleBanner titleBanner = findTitleBanner(child);
                if (titleBanner != null) {
                    return titleBanner;
                }
            }
        }

        return null;
    }
}
