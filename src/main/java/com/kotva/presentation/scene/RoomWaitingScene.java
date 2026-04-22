package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.RoomWaitingController;
import com.kotva.presentation.viewmodel.RoomViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * RoomWaitingScene builds the waiting page shown after entering or creating a room.
 */
public class RoomWaitingScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    public RoomWaitingScene(RoomWaitingController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(RoomWaitingController controller) {
        StackPane sceneRoot = new StackPane();
        RoomViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("room-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(360, 270);

        Label roomSummaryLabel = new Label();
        roomSummaryLabel.getStyleClass().add("room-summary-label");
        controller.bindRoomSummaryLabel(roomSummaryLabel);

        Label joinAddressLabel = new Label();
        joinAddressLabel.getStyleClass().add("room-join-address-label");
        controller.bindJoinAddressLabel(joinAddressLabel);

        ListView<String> playerListView = new ListView<>();
        playerListView.getStyleClass().add("room-list-view");
        playerListView.setPrefSize(420, 160);
        controller.bindPlayerList(playerListView);

        StackPane waitingPanelBox = new StackPane(playerListView);
        waitingPanelBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label(viewModel.getWaitingHintText());
        statusLabel.getStyleClass().add("room-status-label");
        controller.bindStatusLabel(statusLabel);

        CommonButton primaryActionButton = new CommonButton("Start Game");
        primaryActionButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_3);
        controller.bindPrimaryAction(primaryActionButton);

        VBox rightColumn = new VBox(
                14,
                roomSummaryLabel,
                joinAddressLabel,
                waitingPanelBox,
                statusLabel,
                primaryActionButton);
        rightColumn.setAlignment(Pos.TOP_CENTER);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, rightColumn);
        contentBox.setAlignment(Pos.CENTER);
        HBox.setMargin(rightColumn, new Insets(64, 0, 0, 0));
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(10, 0, 0, 30));

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, backButton);
        controller.startMonitoring();
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/room.css").toExternalForm());
    }
}
