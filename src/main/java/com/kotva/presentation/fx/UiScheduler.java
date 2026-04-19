package com.kotva.presentation.fx;

import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * UiScheduler is a thin wrapper around a JavaFX Timeline.
 * It keeps the polling loop wiring readable inside controllers.
 */
public class UiScheduler {
    private final Timeline timeline;

    public UiScheduler(Duration interval, Runnable task) {
        Objects.requireNonNull(interval, "interval cannot be null.");
        Objects.requireNonNull(task, "task cannot be null.");

        this.timeline = new Timeline(new KeyFrame(interval, event -> task.run()));
        this.timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        if (timeline.getStatus() != Animation.Status.RUNNING) {
            timeline.play();
        }
    }

    public void stop() {
        timeline.stop();
    }
}
