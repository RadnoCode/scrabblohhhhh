package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AiSessionRuntime implements AiTurnRuntime {
    private final AiTurnCoordinator aiTurnCoordinator;
    private CompletableFuture<AiMoveOptionSet> pendingAiMove;
    private long requestToken;

    public AiSessionRuntime(AiTurnCoordinator aiTurnCoordinator) {
        this.aiTurnCoordinator = Objects.requireNonNull(aiTurnCoordinator, "aiTurnCoordinator cannot be null.");
    }

    public void requestTurnIfIdle(
        GameSession session,
        PlayerController controller,
        Consumer<TurnCompletion> completionConsumer) {
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(controller, "controller cannot be null.");
        Objects.requireNonNull(completionConsumer, "completionConsumer cannot be null.");

        CompletableFuture<AiMoveOptionSet> future;
        long token;
        String expectedSessionId;
        String expectedPlayerId;
        synchronized (this) {
            if (pendingAiMove != null) {
                return;
            }

            Player currentPlayer = session.getGameState().requireCurrentActivePlayer();
            expectedSessionId = session.getSessionId();
            expectedPlayerId = currentPlayer.getPlayerId();
            token = ++requestToken;
            future = controller.requestAutomatedTurn(aiTurnCoordinator, session);
            pendingAiMove = future;
        }

        future.whenComplete((move, error) -> {
                synchronized (this) {
                    if (pendingAiMove == future) {
                        pendingAiMove = null;
                    }
                }
                completionConsumer.accept(new TurnCompletion(expectedSessionId, expectedPlayerId, token, move, error));
            });
    }

        @Override
    public synchronized void cancelPending() {
        requestToken++;
        pendingAiMove = null;
    }

        @Override
    public synchronized boolean matchesCurrentTurn(
        TurnCompletion completion,
        GameSession session,
        Player currentPlayer,
        PlayerController controller) {
        Objects.requireNonNull(completion, "completion cannot be null.");
        Objects.requireNonNull(session, "session cannot be null.");
        Objects.requireNonNull(currentPlayer, "currentPlayer cannot be null.");
        Objects.requireNonNull(controller, "controller cannot be null.");

        return completion.requestToken() == requestToken
        && Objects.equals(session.getSessionId(), completion.expectedSessionId())
        && Objects.equals(currentPlayer.getPlayerId(), completion.expectedPlayerId())
        && controller.supportsAutomatedTurn();
    }

        @Override
    public AiTurnAttemptResult applyMove(
        PlayerController controller,
        GameApplicationService gameApplicationService,
        GameSession session,
        AiMove move) {
        return controller.applyAutomatedTurn(
            aiTurnCoordinator,
            gameApplicationService,
            session,
            Objects.requireNonNull(move, "move cannot be null."));
    }

        @Override
    public synchronized void close() {
        cancelPending();
        aiTurnCoordinator.close();
    }

    public record TurnCompletion(
        String expectedSessionId,
        String expectedPlayerId,
        long requestToken,
        AiMoveOptionSet moveOptions,
        Throwable error) {

        public TurnCompletion {
            expectedSessionId = Objects.requireNonNull(expectedSessionId, "expectedSessionId cannot be null.");
            expectedPlayerId = Objects.requireNonNull(expectedPlayerId, "expectedPlayerId cannot be null.");
        }
    }
}