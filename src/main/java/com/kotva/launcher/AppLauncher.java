package com.kotva.launcher;

import com.kotva.infrastructure.logging.AppLog;
import com.kotva.presentation.fx.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    private static final double MIN_WIDTH = 1100;
    private static final double MIN_HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        AppContext appContext = new AppContext();
        SceneNavigator navigator = new SceneNavigator(stage, appContext);
        navigator.showHome();
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
    }

    public static void main(String[] args) {
        AppLog.initialize();
        AppLog.installUncaughtExceptionLogging();

        try {
            Application.launch(AppLauncher.class, args);
        } catch (Throwable throwable) {
            AppLog.logException(AppLauncher.class, "Application launch failed.", throwable);
            throw throwable;
        }
    }
}
