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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class RoomCreateScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

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

        VBox topBox = new VBox(12, titleBanner, messageView);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(60, 110, 30, 110));
        root.setTop(topBox);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(420, 320);

        ViceTitleBanner viceTitleBanner = new ViceTitleBanner(viewModel.getViceTitleText());

        HBox viceTitleBox = new HBox(viceTitleBanner);
        viceTitleBox.setAlignment(Pos.CENTER);
        viceTitleBox.setPrefWidth(420);
        viceTitleBox.setMinWidth(420);
        viceTitleBox.setMaxWidth(420);

        InputButton firstButton = new InputButton(viewModel.getFirstOptionText());
        firstButton.enableNumericOnlyInput();
        SwitchButton secondButton = new SwitchButton(viewModel.getSecondOptionText());
        SwitchButton thirdButton = new SwitchButton(viewModel.getThirdOptionText());
        thirdButton.getStyleClass().add("compact-setting-label");
        CommonButton goButton = new CommonButton("Go!");
        controller.bindActions(firstButton, secondButton, thirdButton, goButton, messageView);

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