package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveService;
import com.kotva.ai.AiPositionSnapshot;
import com.kotva.ai.AiTurnMapper;
import com.kotva.ai.QuackleNativeBridge;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.domain.model.Tile;
import com.kotva.mode.PlayerController;
import com.kotva.policy.AiDifficulty;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.PlayerType;
import com.kotva.policy.SessionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class AiTurnCoordinator implements AutoCloseable {
    private final AiMoveService aiMoveService;

    public AiTurnCoordinator(
            QuackleNativeBridge bridge, DictionaryType dictionaryType, AiDifficulty difficulty) {
        this.aiMoveService = new AiMoveService(
                Objects.requireNonNull(bridge, "bridge cannot be null."),
                Objects.requireNonNull(dictionaryType, "dictionaryType cannot be null."),
                Objects.requireNonNull(difficulty, "difficulty cannot be null."));
    }

    public CompletableFuture<AiMove> requestMove(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        return aiMoveService.requestMove(AiPositionSnapshot.fromSession(session));
    }

    public ExecutionResult applyMove(
            PlayerController controller,
            GameApplicationService gameApplicationService,
            GameSession session,
            AiMove move) {
        Objects.requireNonNull(controller, "controller cannot be null.");
        Objects.requireNonNull(gameApplicationService, "gameApplicationService cannot be null.");
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(move, "move cannot be null.");
        ensureAiTurn(controller, session);

        if (!session.getTurnDraft().getPlacements().isEmpty()) {
            controller.recallAllDraftTiles(gameApplicationService, session);
        }

        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        AiTurnMapper.ResolvedMove resolvedMove = AiTurnMapper.resolve(currentPlayer, move);
        if (resolvedMove.action() == AiMove.Action.PASS) {
            TurnTransitionResult result = controller.passTurn(gameApplicationService, session);
            return new ExecutionResult(move, result.isSuccess(), 0, result.getNextPlayerId());
        }

        List<Tile> assignedBlankTiles = new ArrayList<>();
        try {
            for (AiTurnMapper.ResolvedPlacement placement : resolvedMove.placements()) {
                if (placement.assignedLetter() != null) {
                    placement.tile().setAssignedLetter(placement.assignedLetter());
                    assignedBlankTiles.add(placement.tile());
                }

                controller.placeDraftTile(
                        gameApplicationService,
                        session,
                        placement.tile().getTileID(),
                        placement.position());
            }

            SubmitDraftResult result = controller.submitDraft(gameApplicationService, session);
            return new ExecutionResult(
                    move,
                    result.isSuccess(),
                    result.getAwardedScore(),
                    result.getNextPlayerId());
        } catch (RuntimeException exception) {
            rollbackDraft(controller, gameApplicationService, session, assignedBlankTiles);
            throw exception;
        }
    }

    @Override
    public void close() {
        aiMoveService.close();
    }

    private static void ensureAiTurn(PlayerController controller, GameSession session) {
        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is not in progress.");
        }

        Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
        if (currentPlayer.getPlayerType() != PlayerType.AI) {
            throw new IllegalStateException("Current player is not AI.");
        }
        if (!Objects.equals(currentPlayer.getPlayerId(), controller.getPlayerId())) {
            throw new IllegalStateException(
                    "Controller playerId="
                            + controller.getPlayerId()
                            + " does not match current playerId="
                            + currentPlayer.getPlayerId());
        }
    }

    private static void rollbackDraft(
            PlayerController controller,
            GameApplicationService gameApplicationService,
            GameSession session,
            List<Tile> assignedBlankTiles) {
        for (Tile tile : assignedBlankTiles) {
            tile.clearAssignedLetter();
        }

        if (session.getSessionStatus() != SessionStatus.IN_PROGRESS) {
            return;
        }

        try {
            controller.recallAllDraftTiles(gameApplicationService, session);
        } catch (RuntimeException ignored) {
            // Keep the original failure as the visible error.
        }
    }

    public record ExecutionResult(AiMove move, boolean success, int awardedScore, String nextPlayerId) {
        public ExecutionResult {
            move = Objects.requireNonNull(move, "move cannot be null.");
        }
    }
}
