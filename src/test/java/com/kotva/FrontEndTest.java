package com.kotva;

import com.kotva.launcher.AppContext;
import com.kotva.presentation.fx.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

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