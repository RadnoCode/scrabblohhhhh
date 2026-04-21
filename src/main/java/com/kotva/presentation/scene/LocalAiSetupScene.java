package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.LocalAiSetupController;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LocalAiSetupScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String VICE_TITLE_IMAGE_PATH = "/images/mode/play-with-robot.png";
    private static final double VICE_TITLE_WIDTH = 187.5;
    private static final double VICE_TITLE_HEIGHT = 123.75;
    private static final Insets CONTENT_MARGIN = new Insets(2, 100, 48, 100);
    private static final Insets MESSAGE_MARGIN = new Insets(172, 0, 0, 0);

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
        TransientMessageView messageView = new TransientMessageView();
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(360, 270);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText(), VICE_TITLE_IMAGE_PATH);
        viceTitleBanner.setPrefSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMinSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMaxSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);

        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(400);
        viceTitleBox.setMinWidth(400);
        viceTitleBox.setMaxWidth(400);

        InputButton firstButton = new InputButton(viewModel.getFirstOptionText());
        firstButton.enableNumericOnlyInput();
        InputButton stepTimeButton = new InputButton("Select Step Time (s)");
        stepTimeButton.enableNumericOnlyInput();
        SwitchButton secondButton = new SwitchButton(viewModel.getSecondOptionText());
        SwitchButton thirdButton = new SwitchButton(viewModel.getThirdOptionText());
        CommonButton goButton = new CommonButton("Go!");
        firstButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        stepTimeButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        secondButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        thirdButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        goButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        controller.bindActions(
            firstButton,
            stepTimeButton,
            secondButton,
            thirdButton,
            goButton,
            messageView);

        VBox buttonColumn = new VBox(20);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.setTranslateX(30);
        buttonColumn.setTranslateY(-10);
        buttonColumn.getChildren().addAll(
            viceTitleBox,
            firstButton,
            stepTimeButton,
            secondButton,
            thirdButton,
            goButton);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, buttonColumn);
        contentBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(contentBox, CONTENT_MARGIN);
        root.setCenter(contentBox);

        new OptionSceneEntranceAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(viceTitleBox, firstButton, stepTimeButton, secondButton, thirdButton, goButton))
            .install();

        OptionSceneExitAnimationManager exitAnimationManager = new OptionSceneExitAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(viceTitleBox, firstButton, stepTimeButton, secondButton, thirdButton, goButton));
        goButton.setOnAction(event -> {
            if (controller.validateGameSetup(firstButton, stepTimeButton, messageView)) {
                exitAnimationManager.play(goButton, () -> controller.navigateToGame(firstButton, stepTimeButton));
            }
        });

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(10, 0, 0, 30));

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
}
