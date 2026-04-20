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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LocalMultiplayerSetupScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

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

        VBox topBox = new VBox(10, titleBanner, messageView);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(42, 100, 18, 100));
        root.setTop(topBox);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(360, 270);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText());

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
        thirdButton.getStyleClass().add("compact-setting-label");
        CommonButton goButton = new CommonButton("Go!");
        controller.bindActions(
            firstButton,
            stepTimeButton,
            secondButton,
            thirdButton,
            goButton,
            messageView);

        VBox buttonColumn = new VBox(16);
        buttonColumn.setAlignment(Pos.CENTER);
        buttonColumn.getStyleClass().add("mode-button-column");
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
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
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
