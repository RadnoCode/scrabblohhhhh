package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.ai.AiMoveOptionSet;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Default AI runtime that manages one asynchronous AI turn request.
 */
public final class AiSessionRuntime implements AiTurnRuntime {
    private final AiTurnCoordinator aiTurnCoordinator;
    private CompletableFuture<AiMoveOptionSet> pendingAiMove;
    private long requestToken;

    /**
     * Creates an AI session runtime.
     *
     * @param aiTurnCoordinator coordinator used to ask AI for moves
     */
    public AiSessionRuntime(AiTurnCoordinator aiTurnCoordinator) {
        this.aiTurnCoordinator = Objects.requireNonNull(aiTurnCoordinator, "aiTurnCoordinator cannot be null.");
    }

    /**
     * Starts an AI turn request if the runtime is idle.
     *
     * @param session game session
     * @param controller AI player controller
     * @param completionConsumer callback receiving the result
     */
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

    /**
     * Cancels the current pending request.
     */
    @Override
    public synchronized void cancelPending() {
        requestToken++;
        pendingAiMove = null;
    }

    /**
     * Checks whether a completed AI result still matches the current turn.
     *
     * @param completion AI completion
     * @param session current session
     * @param currentPlayer current player
     * @param controller AI controller
     * @return {@code true} if it still matches
     */
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

    /**
     * Applies an AI move through the player controller.
     *
     * @param controller AI controller
     * @param gameApplicationService application service
     * @param session game session
     * @param move AI move
     * @return attempt result
     */
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

    /**
     * Cancels pending work and closes the coordinator.
     */
    @Override
    public synchronized void close() {
        cancelPending();
        aiTurnCoordinator.close();
    }

    /**
     * Data returned when an asynchronous AI turn completes.
     *
     * @param expectedSessionId session id captured when request started
     * @param expectedPlayerId player id captured when request started
     * @param requestToken request token used to reject stale completions
     * @param moveOptions AI move options
     * @param error error raised by the AI request
     */
    public record TurnCompletion(
        String expectedSessionId,
        String expectedPlayerId,
        long requestToken,
        AiMoveOptionSet moveOptions,
        Throwable error) {

        /**
         * Validates captured session and player ids.
         */
        public TurnCompletion {
            expectedSessionId = Objects.requireNonNull(expectedSessionId, "expectedSessionId cannot be null.");
            expectedPlayerId = Objects.requireNonNull(expectedPlayerId, "expectedPlayerId cannot be null.");
        }
    }
}
