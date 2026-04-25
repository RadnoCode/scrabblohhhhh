package com.kotva.presentation.scene;

import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.AiStatusBannerView;
import com.kotva.presentation.component.BlankTilePickerView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.CommonButton;
import com.kotva.presentation.component.PlayerInfoCardView;
import com.kotva.presentation.component.PreviewPanelView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.component.RackHandoffOverlayView;
import com.kotva.presentation.component.TimerView;
import com.kotva.presentation.component.TitleBanner;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.TutorialOverlayView;
import com.kotva.presentation.controller.GameController;
import com.kotva.presentation.interaction.GameInteractionCoordinator;
import com.kotva.presentation.renderer.GameRenderer;
import com.kotva.presentation.renderer.PreviewRenderer;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double SIDE_CARD_GAP = 12;
    private static final double SIDE_COLUMN_WIDTH = TutorialOverlayView.CARD_WIDTH;
    private static final double MESSAGE_OVERLAY_TOP_INSET = 96;
    private static final double MESSAGE_OVERLAY_HORIZONTAL_INSET = 180;

    public GameScene(GameController controller) {
        super(createRoot(controller), DEFAULT_WIDTH, DEFAULT_HEIGHT);
        loadStyleSheets();
    }

    private static Parent createRoot(GameController controller) {
        GameViewModel viewModel = controller.getViewModel();

        StackPane root = new StackPane();
        root.getStyleClass().add("game-root");
        root.setAlignment(Pos.TOP_CENTER);

        BorderPane contentRoot = new BorderPane();
        Pane blankTilePickerLayer = new Pane();
        blankTilePickerLayer.setPickOnBounds(false);
        Pane dragOverlay = new Pane();
        dragOverlay.setMouseTransparent(true);
        dragOverlay.setPickOnBounds(false);
        Pane rackHandoffLayer = new Pane();
        rackHandoffLayer.setPickOnBounds(false);

        TitleBanner titleBanner = new TitleBanner(viewModel.getTitleText());
        CommonButton saveButton = createUtilityButton("Save");
        CommonButton loadButton = createUtilityButton("Load");
        saveButton.setOnAction(event -> controller.onSaveGameRequested());
        loadButton.setOnAction(event -> controller.onLoadGameRequested());
        HBox saveLoadRow = new HBox(8, saveButton, loadButton);
        saveLoadRow.getStyleClass().add("game-save-load-row");
        saveLoadRow.setAlignment(Pos.CENTER);
        boolean showSaveLoad = controller.shouldShowSaveLoadControls();
        saveLoadRow.setVisible(showSaveLoad);
        saveLoadRow.setManaged(showSaveLoad);

        VBox topBox = new VBox(8, titleBanner, saveLoadRow);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(18, 180, 8, 180));
        contentRoot.setTop(topBox);

        TransientMessageView messageView = new TransientMessageView();
        StackPane transientMessageLayer = new StackPane(messageView);
        transientMessageLayer.setMouseTransparent(true);
        transientMessageLayer.setPickOnBounds(false);
        StackPane.setAlignment(messageView, Pos.TOP_CENTER);
        StackPane.setMargin(
            messageView,
            new Insets(
                MESSAGE_OVERLAY_TOP_INSET,
                MESSAGE_OVERLAY_HORIZONTAL_INSET,
                0,
                MESSAGE_OVERLAY_HORIZONTAL_INSET));

        AiStatusBannerView aiStatusBannerView = new AiStatusBannerView();
        BoardView boardView = new BoardView();
        RackView rackView = new RackView();
        double boardHeight = BoardView.BOARD_SIZE * BoardView.CELL_SIZE
            + (BoardView.BOARD_SIZE - 1) * BoardView.CELL_GAP;

        PlayerInfoCardView leftTopCard = new PlayerInfoCardView();
        PlayerInfoCardView leftBottomCard = new PlayerInfoCardView();
        TimerView stepTimerView = new TimerView("Step Time", TimerView.Variant.STEP);
        TimerView totalTimerView = new TimerView("Total Time", TimerView.Variant.TOTAL);
        PreviewPanelView previewPanelView = new PreviewPanelView();
        HBox timerRow = new HBox(8, stepTimerView, totalTimerView);
        timerRow.setAlignment(Pos.CENTER);
        timerRow.getStyleClass().add("game-timer-row");
        VBox leftBottomGroup = new VBox(10, timerRow, previewPanelView);
        leftBottomGroup.setAlignment(Pos.TOP_CENTER);
        Region leftSpacer = new Region();
        VBox.setVgrow(leftSpacer, Priority.ALWAYS);

        VBox leftColumn = new VBox(10, leftTopCard, leftBottomCard, leftSpacer, leftBottomGroup);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPrefWidth(SIDE_COLUMN_WIDTH);
        leftColumn.setMinWidth(SIDE_COLUMN_WIDTH);
        leftColumn.setMaxWidth(SIDE_COLUMN_WIDTH);
        leftColumn.setPrefHeight(boardHeight);
        leftColumn.setMinHeight(boardHeight);
        leftColumn.setMaxHeight(boardHeight);
        leftColumn.getStyleClass().add("game-side-column");

        PlayerInfoCardView rightTopCard = new PlayerInfoCardView();
        PlayerInfoCardView rightBottomCard = new PlayerInfoCardView();
        ActionPanelView actionPanel = new ActionPanelView();
        BlankTilePickerView blankTilePickerView = new BlankTilePickerView();
        RackHandoffOverlayView rackHandoffOverlayView = new RackHandoffOverlayView(rackView);
        TutorialOverlayView tutorialOverlayView = new TutorialOverlayView();
        tutorialOverlayView.setOnAdvanceRequested(controller::onTutorialAdvanceRequested);
        tutorialOverlayView.setOnExitRequested(controller::onTutorialExitRequested);
        tutorialOverlayView.setOnReturnHomeRequested(controller::onTutorialReturnHomeRequested);
        blankTilePickerLayer.getChildren().add(blankTilePickerView);
        rackHandoffLayer.getChildren().add(rackHandoffOverlayView);
        Region rightSpacer = new Region();
        VBox.setVgrow(rightSpacer, Priority.ALWAYS);

        VBox rightInfoCards = new VBox(SIDE_CARD_GAP, rightTopCard, rightBottomCard);
        rightInfoCards.setAlignment(Pos.TOP_CENTER);

        StackPane rightTopSlot = new StackPane(rightInfoCards, tutorialOverlayView);
        rightTopSlot.setAlignment(Pos.TOP_CENTER);
        rightTopSlot.setPrefSize(
            SIDE_COLUMN_WIDTH,
            PlayerInfoCardView.CARD_HEIGHT * 2 + SIDE_CARD_GAP);
        rightTopSlot.setMinSize(
            SIDE_COLUMN_WIDTH,
            PlayerInfoCardView.CARD_HEIGHT * 2 + SIDE_CARD_GAP);
        rightTopSlot.setMaxSize(
            SIDE_COLUMN_WIDTH,
            PlayerInfoCardView.CARD_HEIGHT * 2 + SIDE_CARD_GAP);

        VBox rightColumn = new VBox(12, rightTopSlot, rightSpacer, actionPanel);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.setPrefHeight(boardHeight);
        rightColumn.setMinHeight(boardHeight);
        rightColumn.setMaxHeight(boardHeight);
        rightColumn.getStyleClass().add("game-side-column");

        HBox boardPlayArea = new HBox(22, leftColumn, boardView, rightColumn);
        boardPlayArea.setAlignment(Pos.TOP_CENTER);
        boardPlayArea.getStyleClass().add("game-content-box");
        VBox boardColumn = new VBox(12, aiStatusBannerView, boardPlayArea, rackView);
        boardColumn.setAlignment(Pos.TOP_CENTER);
        boardColumn.setFillWidth(false);
        boardColumn.getStyleClass().add("game-board-column");
        BorderPane.setMargin(boardColumn, new Insets(0, 20, 8, 20));
        contentRoot.setCenter(boardColumn);

        PreviewRenderer previewRenderer = new PreviewRenderer(boardView, rackView, dragOverlay);
        GameRenderer renderer = new GameRenderer(
            boardView,
            rackView,
            actionPanel,
            previewPanelView,
            aiStatusBannerView,
            messageView,
            stepTimerView,
            totalTimerView,
            rackHandoffOverlayView,
            tutorialOverlayView,
            List.of(leftTopCard, rightTopCard, leftBottomCard, rightBottomCard),
            controller.getDraftState(),
            previewRenderer);
        GameInteractionCoordinator interactionCoordinator = new GameInteractionCoordinator(
            boardView,
            rackView,
            actionPanel,
            blankTilePickerView,
            blankTilePickerLayer,
            controller.getDraftState(),
            previewRenderer,
            renderer,
            controller);
        controller.bind(renderer, interactionCoordinator);

        root.getChildren().addAll(
            contentRoot,
            transientMessageLayer,
            blankTilePickerLayer,
            dragOverlay,
            rackHandoffLayer);

        return root;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
    }

    private static CommonButton createUtilityButton(String text) {
        CommonButton button = new CommonButton(text);
        button.getStyleClass().add("game-utility-button");
        button.setButtonContentAlignment(Pos.CENTER);
        button.applyButtonSize(112, 36);
        return button;
    }
}
