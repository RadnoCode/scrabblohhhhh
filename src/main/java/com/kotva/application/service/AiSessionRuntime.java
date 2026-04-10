package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AiSessionRuntime implements AutoCloseable {
    private final AiTurnCoordinator aiTurnCoordinator;
    private CompletableFuture<AiMove> pendingAiMove;
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

        CompletableFuture<AiMove> future;
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

    public synchronized void cancelPending() {
        requestToken++;
        pendingAiMove = null;
    }

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

    public AiTurnCoordinator.ExecutionResult applyTurn(
            PlayerController controller,
            GameApplicationService gameApplicationService,
            GameSession session,
            TurnCompletion completion) {
        Objects.requireNonNull(completion, "completion cannot be null.");
        if (completion.error() != null) {
            throw new IllegalStateException("AI turn completion contains an error.", completion.error());
        }

        return controller.applyAutomatedTurn(
                aiTurnCoordinator,
                gameApplicationService,
                session,
                completion.move());
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
            AiMove move,
            Throwable error) {
        public TurnCompletion {
            expectedSessionId = Objects.requireNonNull(expectedSessionId, "expectedSessionId cannot be null.");
            expectedPlayerId = Objects.requireNonNull(expectedPlayerId, "expectedPlayerId cannot be null.");
        }
    }
}
