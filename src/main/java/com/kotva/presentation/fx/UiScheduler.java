package com.kotva.presentation.fx;

import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Runs a task again and again in JavaFX.
 */
public class UiScheduler {
    private final Timeline timeline;

    /**
     * Creates a scheduler.
     *
     * @param interval time between runs
     * @param task code to run
     */
    public UiScheduler(Duration interval, Runnable task) {
        Objects.requireNonNull(interval, "interval cannot be null.");
        Objects.requireNonNull(task, "task cannot be null.");

        this.timeline = new Timeline(new KeyFrame(interval, event -> task.run()));
        this.timeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Starts the scheduler.
     */
    public void start() {
        if (timeline.getStatus() != Animation.Status.RUNNING) {
            timeline.play();
        }
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
        timeline.stop();
    }
}
