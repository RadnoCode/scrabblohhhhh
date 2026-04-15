package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.LocalAiSetupController;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * LocalAiSetupScene builds the second-level local AI setup page.
 */
public class LocalAiSetupScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public LocalAiSetupScene(LocalAiSetupController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(LocalAiSetupController controller) {
        StackPane sceneRoot = new StackPane();
        GameBranchSetupViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(60, 110, 30, 110));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(420, 320);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText());

        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(420);
        viceTitleBox.setMinWidth(420);
        viceTitleBox.setMaxWidth(420);

        SwitchButton firstButton = new SwitchButton(viewModel.getFirstOptionText());
        SwitchButton secondButton = new SwitchButton(viewModel.getSecondOptionText());
        SwitchButton thirdButton = new SwitchButton(viewModel.getThirdOptionText());
        CommonButton goButton = new CommonButton("Go!");
        controller.bindActions(firstButton, secondButton, thirdButton, goButton);

        VBox buttonColumn = new VBox(26);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.getChildren().addAll(viceTitleBox, firstButton, secondButton, thirdButton, goButton);

        Region spacer = new Region();
        spacer.setMinWidth(80);

        HBox contentBox = new HBox(cardStackIconView, spacer, buttonColumn);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(20, 110, 90, 110));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(10, 0, 0, 30));

        sceneRoot.getChildren().addAll(root, backButton);
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/mode.css").toExternalForm());
    }
}
