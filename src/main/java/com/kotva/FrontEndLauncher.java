package com.kotva;

import com.kotva.presentation.fx.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * FrontEndLauncher is a dedicated JavaFX entry point under main sources.
 * The javafx-maven-plugin can run this class directly with `mvn javafx:run`.
 *
 * It is separated from the existing launcher code so we do not need to
 * change the current project startup flow while the front-end is still evolving.
 */
public class FrontEndLauncher extends Application {
    @Override
    public void start(Stage stage) {
        SceneNavigator navigator = new SceneNavigator(stage);
        navigator.showHome();
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
