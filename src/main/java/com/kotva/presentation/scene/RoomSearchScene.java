package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.SearchIconView;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.controller.RoomSearchController;
import com.kotva.presentation.viewmodel.RoomViewModel;
import com.kotva.lan.udp.DiscoveredRoom;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * RoomSearchScene builds the online room search page.
 */
public class RoomSearchScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double ROOM_LIST_WIDTH = 420;
    private static final double ROOM_LIST_HEIGHT = 168;
    private static final double RIGHT_COLUMN_OFFSET_X = 28;
    private static final double RIGHT_COLUMN_OFFSET_Y = 50;

    public RoomSearchScene(RoomSearchController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(RoomSearchController controller) {
        StackPane sceneRoot = new StackPane();
        RoomViewModel viewModel = controller.getViewModel();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("room-root");

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        BorderPane.setMargin(titleBanner, new Insets(42, 100, 18, 100));
        root.setTop(titleBanner);

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(270, 202.5);
        cardStackIconView.installPlayBeforeButtonActions(sceneRoot);

        SearchIconView searchIconView = new SearchIconView();
        TextField searchField = new TextField();
        searchField.getStyleClass().add("room-search-field");
        controller.bindSearchField(searchField);

        HBox searchBox = new HBox(10, searchIconView, searchField);
        searchBox.getStyleClass().add("room-search-box");
        searchBox.setAlignment(Pos.CENTER_LEFT);

        ListView<DiscoveredRoom> roomListView = new ListView<>();
        roomListView.getStyleClass().add("room-list-view");
        roomListView.setPrefSize(ROOM_LIST_WIDTH, ROOM_LIST_HEIGHT);
        roomListView.setMinSize(ROOM_LIST_WIDTH, ROOM_LIST_HEIGHT);
        roomListView.setMaxSize(ROOM_LIST_WIDTH, ROOM_LIST_HEIGHT);
        controller.bindRoomList(roomListView);

        Label statusLabel = new Label(viewModel.getStatusText());
        statusLabel.getStyleClass().add("room-status-label");
        controller.bindStatusLabel(statusLabel);

        CommonButton joinButton = new CommonButton("Join Selected");
        CommonButton refreshButton = new CommonButton("Refresh");
        joinButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_2);
        refreshButton.setTemplateState(CommonButton.TemplateState.TEMPLATE_1);
        controller.bindJoinAction(joinButton);
        controller.bindRefreshAction(refreshButton);

        VBox buttonBox = new VBox(20, joinButton, refreshButton);
        buttonBox.setAlignment(Pos.TOP_CENTER);

        VBox rightColumn = new VBox(14, searchBox, roomListView, statusLabel, buttonBox);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPrefWidth(440);
        rightColumn.setMinWidth(440);
        rightColumn.setMaxWidth(440);
        rightColumn.setTranslateX(RIGHT_COLUMN_OFFSET_X);
        rightColumn.setTranslateY(RIGHT_COLUMN_OFFSET_Y);

        Region spacer = new Region();
        spacer.setMinWidth(56);

        HBox contentBox = new HBox(cardStackIconView, spacer, rightColumn);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(8, 100, 48, 100));
        root.setCenter(contentBox);

        BackButton backButton = new BackButton();
        controller.bindBackAction(backButton);
        StackPane.setAlignment(backButton, Pos.TOP_LEFT);
        StackPane.setMargin(backButton, new Insets(50, 0, 0, 20));

        sceneRoot.getChildren().addAll(SceneBackgroundLayer.createFor(sceneRoot), root, backButton);
        controller.startScanning();
        return sceneRoot;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/room.css").toExternalForm());
    }
}
