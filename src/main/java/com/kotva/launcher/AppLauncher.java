package com.kotva.launcher;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.fx.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Starts the JavaFX application.
 */
public class AppLauncher extends Application {
    private static final double MIN_WIDTH = 1100;
    private static final double MIN_HEIGHT = 720;

    /**
     * Creates the first window and opens the home screen.
     *
     * @param stage the main JavaFX window
     */
    @Override
    public void start(Stage stage) {
        AppContext appContext = new AppContext();
        CommonButton.setAudioManager(appContext.getAudioManager());
        appContext.getAudioManager().warmUpSoundEffects();
        SceneNavigator navigator = new SceneNavigator(stage, appContext);
        navigator.showHome();
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Application.launch(AppLauncher.class, args);
    }
}
