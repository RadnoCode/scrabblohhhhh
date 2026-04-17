package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.RoomPanelView;
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
        BorderPane.setMargin(titleBanner, new Insets(60, 110, 30, 110));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(420, 320);

        RoomPanelView roomPanelView = RoomPanelView.createWaitingPanel();
        Label roomSummaryLabel = new Label();
        roomSummaryLabel.getStyleClass().add("room-summary-label");
        controller.bindRoomSummaryLabel(roomSummaryLabel);

        ListView<String> playerListView = new ListView<>();
        playerListView.getStyleClass().add("room-list-view");
        playerListView.setPrefSize(420, 180);
        controller.bindPlayerList(playerListView);

        StackPane waitingPanelBox = new StackPane(roomPanelView, playerListView);
        waitingPanelBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label(viewModel.getWaitingHintText());
        statusLabel.getStyleClass().add("room-status-label");
        controller.bindStatusLabel(statusLabel);

        CommonButton primaryActionButton = new CommonButton("Start Game");
        controller.bindPrimaryAction(primaryActionButton);

        VBox rightColumn = new VBox(18, roomSummaryLabel, waitingPanelBox, statusLabel, primaryActionButton);
        rightColumn.setAlignment(Pos.TOP_CENTER);

        Region spacer = new Region();
        spacer.setMinWidth(80);

        HBox contentBox = new HBox(cardStackIconView, spacer, rightColumn);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(40, 110, 100, 110));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(10, 0, 0, 30));

        sceneRoot.getChildren().addAll(root, backButton);
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
