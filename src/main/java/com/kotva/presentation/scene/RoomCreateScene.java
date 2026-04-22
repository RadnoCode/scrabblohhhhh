package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.InputButton;
import com.kotva.presentation.component.SwitchButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.RoomCreateController;
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

/**
 * RoomCreateScene builds the create-room page.
 * Its current structure intentionally matches LocalMultiplayerSetupScene.
 */
public class RoomCreateScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String VICE_TITLE_IMAGE_PATH = "/images/vice-title/nickname.png";
    private static final double VICE_TITLE_WIDTH = 180;
    private static final double VICE_TITLE_HEIGHT = 90;
    private static final double DICTIONARY_TRIGGER_WIDTH = 232;
    private static final double DICTIONARY_TRIGGER_HEIGHT = 40;

    public RoomCreateScene(RoomCreateController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(RoomCreateController controller) {
        StackPane sceneRoot = new StackPane();
        GameBranchSetupViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        TransientMessageView messageView = new TransientMessageView();
        VBox topBox = new VBox(10, titleBanner, messageView);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(42, 100, 18, 100));
        root.setTop(topBox);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(360, 270);
        cardStackIconView.installPlayBeforeButtonActions(sceneRoot);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText(), VICE_TITLE_IMAGE_PATH);
        viceTitleBanner.setPrefSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMinSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);
        viceTitleBanner.setMaxSize(VICE_TITLE_WIDTH, VICE_TITLE_HEIGHT);

        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(400);
        viceTitleBox.setMinWidth(400);
        viceTitleBox.setMaxWidth(400);

        InputButton roomNameButton = new InputButton("Room Name");
        InputButton firstButton = new InputButton(viewModel.getFirstOptionText());
        firstButton.enableNumericOnlyInput();
        InputButton stepTimeButton = new InputButton("Select Step Time (s)");
        stepTimeButton.enableNumericOnlyInput();
        SwitchButton secondButton = new SwitchButton(viewModel.getSecondOptionText());
        secondButton.setSwitchTriggerSize(DICTIONARY_TRIGGER_WIDTH, DICTIONARY_TRIGGER_HEIGHT);
        SwitchButton thirdButton = new SwitchButton(viewModel.getThirdOptionText());
        thirdButton.getStyleClass().add("compact-setting-label");
        CommonButton goButton = new CommonButton("Go!");
        firstButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        stepTimeButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        secondButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        thirdButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        goButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        controller.bindActions(
            roomNameButton,
            firstButton,
            stepTimeButton,
            secondButton,
            thirdButton,
            goButton,
            messageView);

        VBox buttonColumn = new VBox(10);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.getChildren().addAll(
            viceTitleBox,
            roomNameButton,
            firstButton,
            stepTimeButton,
            secondButton,
            thirdButton,
            goButton);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, buttonColumn);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        new OptionSceneEntranceAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(viceTitleBox, roomNameButton, firstButton, stepTimeButton, secondButton, thirdButton, goButton))
            .install();

        OptionSceneExitAnimationManager exitAnimationManager = new OptionSceneExitAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(viceTitleBox, roomNameButton, firstButton, stepTimeButton, secondButton, thirdButton, goButton));
        goButton.setOnAction(event -> {
            var playerNameSetupContext = controller.preparePlayerNameSetupContext(
                roomNameButton,
                firstButton,
                stepTimeButton,
                messageView);
            if (playerNameSetupContext != null) {
                exitAnimationManager.play(
                    goButton,
                    () -> controller.navigateToPlayerNameSetup(playerNameSetupContext));
            }
        });

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, backButton);
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/mode.css").toExternalForm());
    }
}
