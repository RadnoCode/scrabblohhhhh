package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.ViceTitleBanner;
import com.kotva.presentation.controller.OnlineSetupController;
import com.kotva.presentation.viewmodel.GameBranchSetupViewModel;
import java.util.List;
import javafx.event.ActionEvent;
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
 * OnlineSetupScene builds the second-level online setup page.
 */
public class OnlineSetupScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final String VICE_TITLE_IMAGE_PATH = "/images/vice-title/play-by-lan.png";
    private static final double VICE_TITLE_WIDTH = 180;
    private static final double VICE_TITLE_HEIGHT = 90;
    private static final double BUTTON_PANEL_OFFSET_X = 130;
    private static final Insets CONTENT_MARGIN = new Insets(2, 100, 48, 100);

    public OnlineSetupScene(OnlineSetupController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(OnlineSetupController controller) {
        StackPane sceneRoot = new StackPane();
        GameBranchSetupViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
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
        viceTitleBox.setPrefWidth(400);
        viceTitleBox.setMinWidth(400);
        viceTitleBox.setMaxWidth(400);

        CommonButton firstButton = new CommonButton(viewModel.getFirstOptionText());
        CommonButton secondButton = new CommonButton(viewModel.getSecondOptionText());
        firstButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        secondButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        controller.bindActions(firstButton, secondButton);

        VBox buttonColumn = new VBox(10);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.setTranslateX(BUTTON_PANEL_OFFSET_X);
        buttonColumn.setTranslateY(-10);
        buttonColumn.getChildren().addAll(viceTitleBox, firstButton, secondButton);

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
            List.of(viceTitleBox, firstButton, secondButton))
            .install();

        OptionSceneExitAnimationManager exitAnimationManager = new OptionSceneExitAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(viceTitleBox, firstButton, secondButton));
        firstButton.setOnAction(event ->
            exitAnimationManager.play(firstButton, controller::navigateToSearchRoom));
        secondButton.setOnAction(event ->
            exitAnimationManager.play(secondButton, controller::navigateToCreateRoom));

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        var backAction = backButton.getOnAction();
        if (backAction != null) {
            backButton.setOnAction(event ->
                exitAnimationManager.play(null, () -> backAction.handle(new ActionEvent(backButton, backButton))));
        }
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
