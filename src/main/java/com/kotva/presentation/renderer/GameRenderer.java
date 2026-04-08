package com.kotva.presentation.renderer;

import com.kotva.presentation.component.PlayerInfoCardView;
import com.kotva.presentation.component.BoardView;
import com.kotva.presentation.component.RackView;
import com.kotva.presentation.component.TimerView;
import com.kotva.presentation.interaction.GameDraftState;
import com.kotva.presentation.viewmodel.GameViewModel;
import java.util.List;
import java.util.Objects;

/**
 * GameRenderer 负责把控制器产出的 ViewModel 真正落到界面组件上。
 */
public class GameRenderer {
    private final TimerView stepTimerView;
    private final TimerView totalTimerView;
    private final List<PlayerInfoCardView> playerCards;
    private final GameDraftState draftState;
    private final BoardRenderer boardRenderer;
    private final RackRenderer rackRenderer;
    private GameViewModel lastViewModel;

    public GameRenderer(
            BoardView boardView,
            RackView rackView,
            TimerView stepTimerView,
            TimerView totalTimerView,
            List<PlayerInfoCardView> playerCards,
            GameDraftState draftState,
            PreviewRenderer previewRenderer) {
        Objects.requireNonNull(rackView, "rackView cannot be null.");
        this.stepTimerView = Objects.requireNonNull(stepTimerView, "stepTimerView cannot be null.");
        this.totalTimerView = Objects.requireNonNull(totalTimerView, "totalTimerView cannot be null.");
        this.playerCards = List.copyOf(Objects.requireNonNull(playerCards, "playerCards cannot be null."));
        this.draftState = Objects.requireNonNull(draftState, "draftState cannot be null.");
        this.boardRenderer = new BoardRenderer(boardView, draftState, previewRenderer);
        this.rackRenderer = new RackRenderer(rackView, draftState, previewRenderer);
    }

    public void render(GameViewModel viewModel) {
        // render 会缓存最后一份 ViewModel，方便拖拽时只重刷局部状态。
        this.lastViewModel = Objects.requireNonNull(viewModel, "viewModel cannot be null.");
        applyRender(viewModel);
    }

    public void refresh() {
        if (lastViewModel != null) {
            applyRender(lastViewModel);
        }
    }

    private void applyRender(GameViewModel viewModel) {
        // 先把最新快照同步进真实草稿层。
        draftState.syncSnapshot(viewModel.getRackTiles(), viewModel.getBoardTiles());
        // 再刷新计时器文本。
        stepTimerView.setTitle(viewModel.getStepTimerTitle());
        stepTimerView.setTimeText(viewModel.getStepTimerText());
        totalTimerView.setTitle(viewModel.getTotalTimerTitle());
        totalTimerView.setTimeText(viewModel.getTotalTimerText());
        // 棋盘和牌架都基于“快照 + 草稿 + 预览隐藏信息”来渲染。
        boardRenderer.render();
        rackRenderer.render();

        // 玩家卡片则直接按顺序投影到四个固定槽位。
        List<GameViewModel.PlayerCardModel> cardModels = viewModel.getPlayerCards();
        for (int index = 0; index < playerCards.size(); index++) {
            PlayerInfoCardView playerCardView = playerCards.get(index);
            if (index < cardModels.size()) {
                GameViewModel.PlayerCardModel playerCardModel = cardModels.get(index);
                playerCardView.setPlayer(
                        playerCardModel.getPlayerName(),
                        playerCardModel.getPlayerId(),
                        playerCardModel.getScore(),
                        playerCardModel.isCurrentTurn(),
                        playerCardModel.isActive());
            } else {
                playerCardView.clear();
            }
        }
    }
}
