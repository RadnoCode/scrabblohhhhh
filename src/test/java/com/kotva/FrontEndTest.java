package com.kotva;

import com.kotva.launcher.AppContext;
import com.kotva.presentation.fx.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * FrontEndTest is a small JavaFX entry point placed under test.
 * It is used to quickly preview the current front-end page without touching existing project launch code.
 */
public class FrontEndTest extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator navigator = new SceneNavigator(stage, new AppContext());
        navigator.showHome();
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
