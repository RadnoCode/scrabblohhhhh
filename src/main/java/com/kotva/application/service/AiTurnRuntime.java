package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.function.Consumer;

/**
 * Runs and applies AI turns for a game.
 */
public interface AiTurnRuntime extends AutoCloseable {

    /**
     * Requests an AI turn if no AI request is already running.
     *
     * @param session game session
     * @param controller AI player controller
     * @param completionConsumer callback for the AI result
     */
    void requestTurnIfIdle(
        GameSession session,
        PlayerController controller,
        Consumer<AiSessionRuntime.TurnCompletion> completionConsumer);

    /**
     * Cancels the pending AI request, if any.
     */
    void cancelPending();

    /**
     * Checks whether an AI completion still belongs to the current turn.
     *
     * @param completion AI completion to check
     * @param session game session
     * @param currentPlayer current player
     * @param controller AI player controller
     * @return {@code true} if the completion matches the current turn
     */
    boolean matchesCurrentTurn(
        AiSessionRuntime.TurnCompletion completion,
        GameSession session,
        Player currentPlayer,
        PlayerController controller);

    /**
     * Applies an AI move to the game.
     *
     * @param controller AI player controller
     * @param gameApplicationService application service used to execute the move
     * @param session game session
     * @param move AI move to apply
     * @return AI turn attempt result
     */
    AiTurnAttemptResult applyMove(
        PlayerController controller,
        GameApplicationService gameApplicationService,
        GameSession session,
        AiMove move);

    /**
     * Closes resources used by this runtime.
     */
    @Override
    void close();
}
