package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.ai.AiMoveService;
import com.kotva.ai.AiPositionSnapshot;
import com.kotva.ai.AiTurnMapper;
import com.kotva.ai.QuackleNativeBridge;
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

    public CompletableFuture<AiMoveOptionSet> requestMove(GameSession session) {
        Objects.requireNonNull(session, "session cannot be null.");
        return aiMoveService.requestMove(AiPositionSnapshot.fromSession(session));
    }

    public AiTurnAttemptResult applyMove(
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
        AiTurnMapper.ResolvedMove resolvedMove;
        try {
            resolvedMove = AiTurnMapper.resolve(currentPlayer, move);
        } catch (RuntimeException exception) {
            return AiTurnAttemptResult.rejected(
                move,
                "MOVE_MAPPING_FAILED",
                "Failed to map the AI move onto the current rack state.",
                exception);
        }

        if (resolvedMove.action() == AiMove.Action.PASS) {
            try {
                GameActionResult result = controller.passTurn(gameApplicationService, session);
                if (result.isSuccess()) {
                    return AiTurnAttemptResult.accepted(move, 0, result.getNextPlayerId());
                }
                return AiTurnAttemptResult.rejected(
                    move,
                    "PASS_REJECTED",
                    result.getMessage(),
                    null);
            } catch (RuntimeException exception) {
                return AiTurnAttemptResult.rejected(
                    move,
                    "PASS_FAILED",
                    "Applying the AI pass move failed unexpectedly.",
                    exception);
            }
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

            GameActionResult result = controller.submitDraft(gameApplicationService, session);
            if (result.isSuccess()) {
                return AiTurnAttemptResult.accepted(
                    move,
                    result.getAwardedScore(),
                    result.getNextPlayerId());
            }
            rollbackDraft(controller, gameApplicationService, session, assignedBlankTiles);
            return AiTurnAttemptResult.rejected(
                move,
                "SUBMIT_REJECTED",
                result.getMessage(),
                null);
        } catch (RuntimeException exception) {
            rollbackDraft(controller, gameApplicationService, session, assignedBlankTiles);
            return AiTurnAttemptResult.rejected(
                move,
                "APPLY_FAILED",
                "Applying the AI move failed unexpectedly.",
                exception);
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
        }
    }

}