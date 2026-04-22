package com.kotva.presentation.scene;

import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.HelpEnvelope;
import com.kotva.presentation.component.PlayEnvelope;
import com.kotva.presentation.component.SettingEnvelope;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TutorialEnvelope;
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
import java.util.List;

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
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        PlayEnvelope playEnvelope = new PlayEnvelope();
        TutorialEnvelope tutorialEnvelope = new TutorialEnvelope();
        SettingEnvelope settingEnvelope = new SettingEnvelope();
        HelpEnvelope helpEnvelope = new HelpEnvelope();
        StackPane envelopeStack = new StackPane(playEnvelope, tutorialEnvelope, settingEnvelope, helpEnvelope);
        envelopeStack.getStyleClass().add("home-envelope-stack");
        playEnvelope.showForwardStartState();

        CommonButton playButton = new CommonButton(viewModel.getPlayText());
        CommonButton tutorialButton = new CommonButton(viewModel.getTutorialText());
        CommonButton settingsButton = new CommonButton(viewModel.getSettingsText());
        CommonButton helpButton = new CommonButton(viewModel.getHelpText());
        playButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        tutorialButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        settingsButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        helpButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);

        controller.bindActions(
            playButton,
            tutorialButton,
            settingsButton,
            helpButton,
            playEnvelope,
            tutorialEnvelope,
            settingEnvelope,
            helpEnvelope);

        HomeSelectionAnimationManager selectionAnimationManager = new HomeSelectionAnimationManager(
            sceneRoot,
            titleBanner,
            envelopeStack,
            playButton,
            tutorialButton,
            settingsButton,
            helpButton);

        playButton.setOnAction(event -> selectionAnimationManager.play(
            HomeSelectionAnimationManager.ButtonKey.PLAY,
            controller::navigateToPlay));
        tutorialButton.setOnAction(event -> selectionAnimationManager.play(
            HomeSelectionAnimationManager.ButtonKey.TUTORIAL,
            controller::navigateToTutorial));
        settingsButton.setOnAction(event -> selectionAnimationManager.play(
            HomeSelectionAnimationManager.ButtonKey.SETTINGS,
            controller::navigateToSettings));
        helpButton.setOnAction(event -> selectionAnimationManager.play(
            HomeSelectionAnimationManager.ButtonKey.HELP,
            controller::navigateToHelp));

        VBox buttonColumn = new VBox(20);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);
        buttonColumn.getStyleClass().add("home-button-column");
        buttonColumn.getChildren().addAll(playButton, tutorialButton, settingsButton, helpButton);

        Region contentSpacer = new Region();
        contentSpacer.setMinWidth(56);

        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getStyleClass().add("home-content-box");
        contentBox.getChildren().addAll(envelopeStack, contentSpacer, buttonColumn);

        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        new HomeEntranceAnimationManager(
            sceneRoot,
            titleBanner,
            envelopeStack,
            List.of(playButton, tutorialButton, settingsButton, helpButton))
            .install();

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root);
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

        Label titleLabel = new Label("It seems you play for the first time!");
        titleLabel.getStyleClass().add("home-tutorial-title");

        Label bodyLabel = new Label(
            "It is recommended to complete the tutorial first.\nThe tutorial will introduce basic actions and rules.");
        bodyLabel.getStyleClass().add("home-tutorial-body");
        bodyLabel.setWrapText(true);

        CommonButton startButton = new CommonButton("Start Tutorial");
        CommonButton skipButton = new CommonButton("Skip for Now");
        startButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        skipButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        startButton.applyTemplateSize(280);
        skipButton.applyTemplateSize(280);
        startButton.setOnAction(event -> controller.startTutorialFromPrompt());
        skipButton.setOnAction(event -> {
            controller.dismissTutorialPrompt();
            overlay.setVisible(false);
            overlay.setManaged(false);
        });

        VBox buttonBox = new VBox(20, startButton, skipButton);
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
