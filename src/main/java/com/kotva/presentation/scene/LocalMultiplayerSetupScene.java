package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.LocalMultiplayerSetupController;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LocalMultiplayerSetupScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String VICE_TITLE_IMAGE_PATH = "/images/vice-title/play-with-friends.png";
    private static final String GAME_TIME_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-select-game-time.png";
    private static final String STEP_TIME_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-select-step-time.png";
    private static final String GAME_TYPE_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-game-type.png";
    private static final String TARGET_SCORE_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-target-score.png";
    private static final String DICTIONARY_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-dictionary.png";
    private static final String PLAYER_COUNT_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-number-of-player.png";
    private static final String CONTINUE_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-continue.png";
    private static final String LOAD_SAVE_BUTTON_IMAGE_PATH =
        "/images/local-multiplayer/buttons/bottom-load-save.png";
    private static final double BUTTON_SCALE = 0.63;
    private static final double VICE_TITLE_WIDTH = 180;
    private static final double VICE_TITLE_HEIGHT = 90;
    private static final double DEFAULT_SETUP_BUTTON_WIDTH = 420.0;
    private static final double DEFAULT_SETUP_BUTTON_HEIGHT = 420.0 / (1301.0 / 262.0);
    private static final double SETUP_BUTTON_WIDTH = DEFAULT_SETUP_BUTTON_WIDTH * BUTTON_SCALE;
    private static final double SETUP_BUTTON_HEIGHT = DEFAULT_SETUP_BUTTON_HEIGHT * BUTTON_SCALE;
    private static final double SETUP_INPUT_WIDTH = 176 * BUTTON_SCALE;
    private static final double SETUP_INPUT_HEIGHT = 40 * BUTTON_SCALE;
    private static final double DICTIONARY_TRIGGER_WIDTH = 232 * BUTTON_SCALE;
    private static final double DICTIONARY_TRIGGER_HEIGHT = 40 * BUTTON_SCALE;
    private static final double GAME_TYPE_TRIGGER_WIDTH = 240 * BUTTON_SCALE;
    private static final double PLAYER_COUNT_TRIGGER_WIDTH = 176 * BUTTON_SCALE;
    private static final double CONTINUE_BUTTON_WIDTH = (DEFAULT_SETUP_BUTTON_HEIGHT * 591.0 / 238.0) * BUTTON_SCALE;
    private static final double SETUP_GRID_COLUMN_GAP = 12;
    private static final double SETUP_GRID_WIDTH = SETUP_BUTTON_WIDTH * 2 + SETUP_GRID_COLUMN_GAP;
    private static final Insets CONTENT_MARGIN = new Insets(2, 100, 48, 100);
    private static final Insets MESSAGE_MARGIN = new Insets(172, 0, 0, 0);

    public LocalMultiplayerSetupScene(LocalMultiplayerSetupController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(LocalMultiplayerSetupController controller) {
        StackPane sceneRoot = new StackPane();
        GameBranchSetupViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        TransientMessageView messageView = new TransientMessageView();
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(270, 202.5);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText(), VICE_TITLE_IMAGE_PATH);
        viceTitleBanner.setPrefSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMinSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMaxSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);

        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(SETUP_GRID_WIDTH);
        viceTitleBox.setMinWidth(SETUP_GRID_WIDTH);
        viceTitleBox.setMaxWidth(SETUP_GRID_WIDTH);

        InputButton firstButton = new InputButton(viewModel.getFirstOptionText());
        firstButton.enableNumericOnlyInput();
        InputButton stepTimeButton = new InputButton("Enter Step Time(s)");
        stepTimeButton.enableNumericOnlyInput();
        SwitchButton rulesetButton = new SwitchButton("Game Type");
        InputButton targetScoreButton = new InputButton("Target Score");
        targetScoreButton.enableNumericOnlyInput();
        SwitchButton secondButton = new SwitchButton(viewModel.getSecondOptionText());
        SwitchButton thirdButton = new SwitchButton(viewModel.getThirdOptionText());
        thirdButton.getStyleClass().add("compact-setting-label");
        CommonButton goButton = new CommonButton("Go!");
        CommonButton loadButton = new CommonButton("Load Save");
        configureSetupButton(firstButton, GAME_TIME_BUTTON_IMAGE_PATH);
        configureSetupButton(stepTimeButton, STEP_TIME_BUTTON_IMAGE_PATH);
        configureSetupButton(rulesetButton, GAME_TYPE_BUTTON_IMAGE_PATH);
        rulesetButton.setSwitchTriggerSize(GAME_TYPE_TRIGGER_WIDTH, SETUP_INPUT_HEIGHT);
        configureSetupButton(targetScoreButton, TARGET_SCORE_BUTTON_IMAGE_PATH);
        configureSetupButton(secondButton, DICTIONARY_BUTTON_IMAGE_PATH);
        secondButton.setSwitchTriggerSize(DICTIONARY_TRIGGER_WIDTH, DICTIONARY_TRIGGER_HEIGHT);
        configureSetupButton(thirdButton, PLAYER_COUNT_BUTTON_IMAGE_PATH);
        thirdButton.setSwitchTriggerSize(PLAYER_COUNT_TRIGGER_WIDTH, SETUP_INPUT_HEIGHT);
        configureActionButton(goButton, CONTINUE_BUTTON_IMAGE_PATH);
        configureActionButton(loadButton, LOAD_SAVE_BUTTON_IMAGE_PATH);
        controller.bindActions(
            firstButton,
            stepTimeButton,
            rulesetButton,
            targetScoreButton,
            secondButton,
            thirdButton,
            goButton,
            loadButton,
            messageView);

        GridPane setupGrid = new GridPane();
        setupGrid.setHgap(SETUP_GRID_COLUMN_GAP);
        setupGrid.setVgap(7);
        setupGrid.setAlignment(Pos.CENTER);
        setupGrid.add(firstButton, 0, 0);
        setupGrid.add(rulesetButton, 1, 0);
        setupGrid.add(stepTimeButton, 0, 1);
        setupGrid.add(targetScoreButton, 1, 1);
        setupGrid.add(secondButton, 0, 2);
        setupGrid.add(thirdButton, 0, 3);
        setupGrid.add(goButton, 0, 4);
        setupGrid.add(loadButton, 1, 4);

        VBox buttonPanel = new VBox(7);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.getStyleClass().add("mode-button-column");
        buttonPanel.setTranslateX(100);
        buttonPanel.setTranslateY(-22);
        buttonPanel.getChildren().addAll(viceTitleBox, setupGrid);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, buttonPanel);
        contentBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(contentBox, CONTENT_MARGIN);
        root.setCenter(contentBox);

        new OptionSceneEntranceAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(
                viceTitleBox,
                firstButton,
                stepTimeButton,
                rulesetButton,
                targetScoreButton,
                secondButton,
                thirdButton,
                goButton,
                loadButton))
            .install();

        OptionSceneExitAnimationManager exitAnimationManager = new OptionSceneExitAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(
                viceTitleBox,
                firstButton,
                stepTimeButton,
                rulesetButton,
                targetScoreButton,
                secondButton,
                thirdButton,
                goButton,
                loadButton));
        goButton.setOnAction(event -> {
            if (controller.validateGameSetup(firstButton, stepTimeButton, targetScoreButton, messageView)) {
                exitAnimationManager.play(
                    goButton,
                    () -> controller.navigateToPlayerNameSetup(firstButton, stepTimeButton, targetScoreButton));
            }
        });

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        var backAction = backButton.getOnAction();
        if (backAction != null) {
            backButton.setOnAction(event ->
                exitAnimationManager.play(null, () -> backAction.handle(new ActionEvent(backButton, backButton))));
        }
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

        StackPane.setAlignment(messageView, Pos.TOP_CENTER);
        StackPane.setMargin(messageView, MESSAGE_MARGIN);

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, messageView, backButton);
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/mode.css").toExternalForm());
    }

    private static void configureSetupButton(CommonButton button, String imagePath) {
        button.getStyleClass().add("local-setup-button");
        button.setCustomBackgroundImage(imagePath);
        button.applyButtonSize(SETUP_BUTTON_WIDTH, SETUP_BUTTON_HEIGHT);
        if (button instanceof InputButton inputButton) {
            inputButton.setInputFieldTone(InputButton.InputFieldTone.DARK_SURFACE);
            inputButton.setInputFieldSize(SETUP_INPUT_WIDTH, SETUP_INPUT_HEIGHT);
        }
    }

    private static void configureActionButton(CommonButton button, String imagePath) {
        button.getStyleClass().add("local-setup-button");
        button.setCustomBackgroundImage(imagePath);
        button.setButtonContentAlignment(Pos.CENTER);
        button.applyButtonSize(CONTINUE_BUTTON_WIDTH, SETUP_BUTTON_HEIGHT);
    }

}
