package com.kotva.presentation.renderer;

import com.kotva.presentation.component.ActionPanelView;
import com.kotva.presentation.component.AiStatusBannerView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.PlayerInfoCardView;
import com.kotva.presentation.component.PreviewPanelView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.component.RackHandoffOverlayView;
import com.kotva.presentation.component.TimerView;
import com.kotva.presentation.component.TransientMessageView;
import com.kotva.presentation.component.TutorialOverlayView;
import com.kotva.presentation.interaction.GameDraftState;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import java.util.Objects;

public class GameRenderer {
    private final BoardView boardView;
    private final RackView rackView;
    private final ActionPanelView actionPanelView;
    private final PreviewPanelView previewPanelView;
    private final AiStatusBannerView aiStatusBannerView;
    private final TransientMessageView transientMessageView;
    private final TimerView stepTimerView;
    private final TimerView totalTimerView;
    private final RackHandoffOverlayView rackHandoffOverlayView;
    private final TutorialOverlayView tutorialOverlayView;
    private final List<PlayerInfoCardView> playerCards;
    private final BoardRenderer boardRenderer;
    private final RackRenderer rackRenderer;
    private GameViewModel lastViewModel;
    private long renderedTransientMessageVersion;

    public GameRenderer(
        BoardView boardView,
        RackView rackView,
        ActionPanelView actionPanelView,
        PreviewPanelView previewPanelView,
        AiStatusBannerView aiStatusBannerView,
        TransientMessageView transientMessageView,
        TimerView stepTimerView,
        TimerView totalTimerView,
        RackHandoffOverlayView rackHandoffOverlayView,
        TutorialOverlayView tutorialOverlayView,
        List<PlayerInfoCardView> playerCards,
        GameDraftState draftState,
        PreviewRenderer previewRenderer) {
        this.boardView = Objects.requireNonNull(boardView, "boardView cannot be null.");
        this.rackView = Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.actionPanelView = Objects.requireNonNull(actionPanelView, "actionPanelView cannot be null.");
        this.previewPanelView = Objects.requireNonNull(
            previewPanelView,
            "previewPanelView cannot be null.");
        this.aiStatusBannerView =
        Objects.requireNonNull(aiStatusBannerView, "aiStatusBannerView cannot be null.");
        this.transientMessageView =
        Objects.requireNonNull(transientMessageView, "transientMessageView cannot be null.");
        this.stepTimerView = Objects.requireNonNull(stepTimerView, "stepTimerView cannot be null.");
        this.totalTimerView = Objects.requireNonNull(totalTimerView, "totalTimerView cannot be null.");
        this.rackHandoffOverlayView = Objects.requireNonNull(
            rackHandoffOverlayView,
            "rackHandoffOverlayView cannot be null.");
        this.tutorialOverlayView = Objects.requireNonNull(
            tutorialOverlayView,
            "tutorialOverlayView cannot be null.");
        this.playerCards = List.copyOf(Objects.requireNonNull(playerCards, "playerCards cannot be null."));
        this.boardRenderer = new BoardRenderer(boardView, draftState, previewRenderer);
        this.rackRenderer = new RackRenderer(rackView, draftState, previewRenderer);
    }

    public void render(GameViewModel viewModel) {
        this.lastViewModel = Objects.requireNonNull(viewModel, "viewModel cannot be null.");
        applyRender(viewModel);
    }

    public void refresh() {
        if (lastViewModel != null) {
            applyRender(lastViewModel);
        }
    }

    public void showRackHandoffOverlay() {
        rackHandoffOverlayView.showOverlay();
    }

    public void hideRackHandoffOverlay() {
        rackHandoffOverlayView.hideOverlay();
    }

    private void applyRender(GameViewModel viewModel) {
        stepTimerView.setTitle(viewModel.getStepTimerTitle());
        stepTimerView.setTimeText(viewModel.getStepTimerText());
        totalTimerView.setTitle(viewModel.getTotalTimerTitle());
        totalTimerView.setTimeText(viewModel.getTotalTimerText());
        if (viewModel.getTransientMessageVersion() != renderedTransientMessageVersion) {
            renderedTransientMessageVersion = viewModel.getTransientMessageVersion();
            if (!viewModel.getTransientMessageText().isBlank()) {
                transientMessageView.showMessage(viewModel.getTransientMessageText());
            }
        }
        boardRenderer.render();
        boardView.setWordOutline(viewModel.getWordOutline());
        rackRenderer.render();
        previewPanelView.setModel(viewModel.getPreviewPanel());
        tutorialOverlayView.setModel(viewModel.getTutorialOverlay());
        boardView.setDisable(viewModel.isInteractionLocked());
        rackView.setDisable(viewModel.isInteractionLocked());
        actionPanelView.applyModel(viewModel.getActionPanel(), viewModel.isInteractionLocked());
        if (viewModel.getAiErrorSummary().isBlank()) {
            aiStatusBannerView.clear();
        } else {
            aiStatusBannerView.showMessage(
                viewModel.getAiErrorSummary(),
                viewModel.getAiErrorDetails());
        }

        List<GameViewModel.PlayerCardModel> cardModels = viewModel.getPlayerCards();
        for (int index = 0; index < playerCards.size(); index++) {
            PlayerInfoCardView playerCardView = playerCards.get(index);
            if (index < cardModels.size()) {
                GameViewModel.PlayerCardModel playerCardModel = cardModels.get(index);
                playerCardView.setPlayer(
                    playerCardModel.getPlayerName(),
                    playerCardModel.getPlayerId(),
                    playerCardModel.getScore(),
                    playerCardModel.getStepMarkText(),
                    playerCardModel.isCurrentTurn(),
                    playerCardModel.isActive());
            } else {
                playerCardView.clear();
            }
        }
    }
}
