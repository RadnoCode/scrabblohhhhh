package com.kotva.presentation.scene;

import com.kotva.presentation.component.BackButton;
import com.kotva.presentation.component.CardStackIconView;
import com.kotva.presentation.component.RoomPanelView;
import com.kotva.presentation.controller.RoomWaitingController;
import com.kotva.presentation.viewmodel.RoomViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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

        CardStackIconView cardStackIconView = new CardStackIconView();
        cardStackIconView.setPrefSize(420, 320);

        RoomPanelView roomPanelView = RoomPanelView.createWaitingPanel();

        Label waitingHintLabel = new Label(viewModel.getWaitingHintText());
        waitingHintLabel.getStyleClass().add("room-waiting-hint");

        StackPane waitingTicketBox = new StackPane(roomPanelView, waitingHintLabel);
        waitingTicketBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        spacer.setMinWidth(80);

        HBox contentBox = new HBox(cardStackIconView, spacer, waitingTicketBox);
        contentBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(contentBox, new Insets(120, 110, 100, 110));
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
        getStylesheets().add(getClass().getResource("/css/room.css").toExternalForm());
    }
}
