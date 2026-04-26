package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.function.Consumer;

/**
 * Defines the runtime work needed for AI turns.
 */
public interface AiTurnRuntime extends AutoCloseable {

    /**
     * Starts an AI turn request when no request is pending.
     *
     * @param session active game session
     * @param controller AI player controller
     * @param completionConsumer consumer called when the request completes
     */
    void requestTurnIfIdle(
        GameSession session,
        PlayerController controller,
        Consumer<AiSessionRuntime.TurnCompletion> completionConsumer);

    /**
     * Cancels any pending AI turn request.
     */
    void cancelPending();

    /**
     * Checks whether a completed AI turn still matches the current turn.
     *
     * @param completion completed AI turn request
     * @param session active game session
     * @param currentPlayer current player
     * @param controller AI player controller
     * @return true when the completion is still current
     */
    boolean matchesCurrentTurn(
        AiSessionRuntime.TurnCompletion completion,
        GameSession session,
        Player currentPlayer,
        PlayerController controller);

    /**
     * Applies an AI move to the game session.
     *
     * @param controller AI player controller
     * @param gameApplicationService game action service
     * @param session active game session
     * @param move AI move to apply
     * @return attempt result
     */
    AiTurnAttemptResult applyMove(
        PlayerController controller,
        GameApplicationService gameApplicationService,
        GameSession session,
        AiMove move);

    @Override
    void close();
}
