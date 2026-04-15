package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.ModeSelectController;
import com.kotva.presentation.viewmodel.SetupViewModel;
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
 * ModeSelectScene builds the GameSetting page shown after clicking Play.
 */
public class ModeSelectScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

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
        BorderPane.setMargin(titleBanner, new Insets(60, 110, 30, 110));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(420, 320);

        CommonButton withFriendsButton = new CommonButton(viewModel.getWithFriendsText());
        CommonButton withRobotButton = new CommonButton(viewModel.getWithRobotText());
        CommonButton byLanButton = new CommonButton(viewModel.getByLanText());
        controller.bindActions(withFriendsButton, withRobotButton, byLanButton);

        VBox buttonColumn = new VBox(26);
        buttonColumn.setAlignment(Pos.CENTER_LEFT);
        buttonColumn.getStyleClass().add("mode-button-column");
        buttonColumn.getChildren().addAll(withFriendsButton, withRobotButton, byLanButton);

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
