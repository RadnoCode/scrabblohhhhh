package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.ModeSelectController;
import com.kotva.presentation.viewmodel.SetupViewModel;
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
 * Shows the mode select screen.
 */
public class ModeSelectScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double BUTTON_PANEL_OFFSET_X = 100;

    public ModeSelectScene(ModeSelectController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(ModeSelectController controller) {
        StackPane sceneRoot = new StackPane();

        SetupViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("mode-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(270, 202.5);

        CommonButton withFriendsButton = new CommonButton(viewModel.getWithFriendsText());
        CommonButton withRobotButton = new CommonButton(viewModel.getWithRobotText());
        CommonButton byLanButton = new CommonButton(viewModel.getByLanText());
        configureModeButton(
            withFriendsButton,
            "mode-with-friends-button",
            "/images/mode/buttons/bottom-with-friends.png");
        configureModeButton(
            withRobotButton,
            "mode-with-robot-button",
            "/images/mode/buttons/bottom-with-robot.png");
        configureModeButton(
            byLanButton,
            "mode-by-lan-button",
            "/images/mode/buttons/bottom-by-lan.png");
        controller.bindActions(withFriendsButton, withRobotButton, byLanButton);

        VBox buttonColumn = new VBox(20);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.setTranslateX(BUTTON_PANEL_OFFSET_X);
        buttonColumn.getChildren().addAll(withFriendsButton, withRobotButton, byLanButton);

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
            List.of(withFriendsButton, withRobotButton, byLanButton))
            .install();

        OptionSceneExitAnimationManager exitAnimationManager = new OptionSceneExitAnimationManager(
            sceneRoot,
            titleBanner,
            cardStackIconView,
            List.of(withFriendsButton, withRobotButton, byLanButton));
        withFriendsButton.setOnAction(event ->
            exitAnimationManager.play(withFriendsButton, controller::navigateToWithFriends));
        withRobotButton.setOnAction(event ->
            exitAnimationManager.play(withRobotButton, controller::navigateToWithRobot));
        byLanButton.setOnAction(event ->
            exitAnimationManager.play(byLanButton, controller::navigateToByLan));

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

    private static void configureModeButton(CommonButton button, String styleClass, String imagePath) {
        button.getStyleClass().addAll("mode-nav-button", styleClass);
        button.setCustomBackgroundImage(imagePath);
        button.applyTemplateSize(420);
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/mode.css").toExternalForm());
    }
}
