package com.kotva.presentation.scene;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.controller.SettlementController;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class SettlementScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String GO_BACK_TEXT = "Go Back To Game Setting Page";

    public SettlementScene(SettlementController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(SettlementController controller) {
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        CommonButton goBackButton = new CommonButton(GO_BACK_TEXT);
        controller.bindActions(goBackButton);
        root.getChildren().add(goBackButton);
        return root;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
    }
}