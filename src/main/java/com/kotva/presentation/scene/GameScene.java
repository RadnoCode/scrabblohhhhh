package com.kotva.presentation.scene;

import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.AiStatusBannerView;
import com.kotva.presentation.component.BlankTilePickerView;
import com.kotva.presentation.component.BoardView;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameScene extends Scene {
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

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
        TransientMessageView messageView = new TransientMessageView();
        VBox topBox = new VBox(6, titleBanner, messageView);
        topBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(topBox, new Insets(18, 180, 8, 180));
        contentRoot.setTop(topBox);

        AiStatusBannerView aiStatusBannerView = new AiStatusBannerView();
        BoardView boardView = new BoardView();
        RackView rackView = new RackView();
        VBox boardColumn = new VBox(12, aiStatusBannerView, boardView, rackView);
        boardColumn.setAlignment(Pos.CENTER);
        boardColumn.getStyleClass().add("game-board-column");

        PlayerInfoCardView leftTopCard = new PlayerInfoCardView();
        PlayerInfoCardView leftBottomCard = new PlayerInfoCardView();
        TimerView stepTimerView = new TimerView("Step Time");
        TimerView totalTimerView = new TimerView("Total Time");
        PreviewPanelView previewPanelView = new PreviewPanelView();
        HBox timerRow = new HBox(8, stepTimerView, totalTimerView);
        timerRow.setAlignment(Pos.CENTER);
        timerRow.getStyleClass().add("game-timer-row");

        VBox leftColumn = new VBox(10, leftTopCard, leftBottomCard, timerRow, previewPanelView);
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.getStyleClass().add("game-side-column");

        PlayerInfoCardView rightTopCard = new PlayerInfoCardView();
        PlayerInfoCardView rightBottomCard = new PlayerInfoCardView();
        ActionPanelView actionPanel = new ActionPanelView();
        BlankTilePickerView blankTilePickerView = new BlankTilePickerView();
        RackHandoffOverlayView rackHandoffOverlayView = new RackHandoffOverlayView(rackView);
        blankTilePickerLayer.getChildren().add(blankTilePickerView);
        rackHandoffLayer.getChildren().add(rackHandoffOverlayView);

        VBox rightColumn = new VBox(12, rightTopCard, rightBottomCard, actionPanel);
        rightColumn.setAlignment(Pos.TOP_CENTER);
        rightColumn.getStyleClass().add("game-side-column");

        HBox contentBox = new HBox(22, leftColumn, boardColumn, rightColumn);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.getStyleClass().add("game-content-box");
        BorderPane.setMargin(contentBox, new Insets(0, 20, 8, 20));
        contentRoot.setCenter(contentBox);

        TutorialOverlayView tutorialOverlayView = new TutorialOverlayView();
        tutorialOverlayView.setOnAdvanceRequested(controller::onTutorialAdvanceRequested);
        tutorialOverlayView.setOnExitRequested(controller::onTutorialExitRequested);
        tutorialOverlayView.setOnReturnHomeRequested(controller::onTutorialReturnHomeRequested);

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
            blankTilePickerLayer,
            dragOverlay,
            rackHandoffLayer,
            tutorialOverlayView);

        return root;
    }

    private void loadStyleSheets() {
        getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/component.css").toExternalForm());
        getStylesheets().add(getClass().getResource("/css/game.css").toExternalForm());
    }
}
