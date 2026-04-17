package com.kotva.presentation.scene;

import com.kotva.presentation.component.EnvelopeIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.HomeController;
import com.kotva.presentation.viewmodel.HomeViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class HomeScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public HomeScene(HomeController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(HomeController controller) {
        HomeViewModel viewModel = controller.getViewModel();

        StackPane sceneRoot = new StackPane();
        BorderPane root = new BorderPane();
        root.getStyleClass().add("home-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setAlignment(titleBanner, Pos.CENTER);
        BorderPane.setMargin(titleBanner, new Insets(60, 110, 30, 110));
        root.setTop(titleBanner);

        EnvelopeIconView envelopeIconView = new EnvelopeIconView();
        envelopeIconView.setPrefSize(380, 270);

        CommonButton playButton = new CommonButton(viewModel.getPlayText());
        CommonButton tutorialButton = new CommonButton(viewModel.getTutorialText());
        CommonButton settingsButton = new CommonButton(viewModel.getSettingsText());
        CommonButton helpButton = new CommonButton(viewModel.getHelpText());

        controller.bindActions(playButton, tutorialButton, settingsButton, helpButton, envelopeIconView);

        VBox buttonColumn = new VBox(26);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);
        buttonColumn.getStyleClass().add("home-button-column");
        buttonColumn.getChildren().addAll(playButton, tutorialButton, settingsButton, helpButton);

        Region contentSpacer = new Region();
        contentSpacer.setMinWidth(70);

        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("home-content-box");
        contentBox.getChildren().addAll(envelopeIconView, contentSpacer, buttonColumn);

        BorderPane.setMargin(contentBox, new Insets(20, 110, 90, 110));
        root.setCenter(contentBox);

        sceneRoot.getChildren().add(root);
        if (controller.isTutorialPromptVisible()) {
            sceneRoot.getChildren().add(createTutorialPrompt(controller));
        }
        return sceneRoot;
    }

    private static Parent createTutorialPrompt(HomeController controller) {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("home-tutorial-overlay");

        VBox card = new VBox(16);
        card.getStyleClass().add("home-tutorial-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(520);
        card.setPadding(new Insets(28));

        Label titleLabel = new Label("第一次游玩提醒");
        titleLabel.getStyleClass().add("home-tutorial-title");

        Label bodyLabel = new Label(
            "推荐先完成新手教程。\n教程会固定盘面与 rack，并逐步介绍落子、合法性、提交、后续连接与 rearrange。");
        bodyLabel.getStyleClass().add("home-tutorial-body");
        bodyLabel.setWrapText(true);

        CommonButton startButton = new CommonButton("开始教程");
        CommonButton skipButton = new CommonButton("暂时跳过");
        startButton.setPrefWidth(280);
        skipButton.setPrefWidth(280);
        startButton.setOnAction(event -> controller.startTutorialFromPrompt());
        skipButton.setOnAction(event -> {
            controller.dismissTutorialPrompt();
            overlay.setVisible(false);
            overlay.setManaged(false);
        });

        VBox buttonBox = new VBox(12, startButton, skipButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(titleLabel, bodyLabel, buttonBox);
        overlay.getChildren().add(card);
        return overlay;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
    }
}
