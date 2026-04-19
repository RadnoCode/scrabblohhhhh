package com.kotva.application.service;

import com.kotva.ai.AiMove;
import com.kotva.application.session.GameSession;
import com.kotva.domain.model.Player;
import com.kotva.mode.PlayerController;
import java.util.function.Consumer;

public interface AiTurnRuntime extends AutoCloseable {
    void requestTurnIfIdle(
            GameSession session,
            PlayerController controller,
            Consumer<AiSessionRuntime.TurnCompletion> completionConsumer);

    void cancelPending();

    boolean matchesCurrentTurn(
            AiSessionRuntime.TurnCompletion completion,
            GameSession session,
            Player currentPlayer,
            PlayerController controller);

    AiTurnAttemptResult applyMove(
            PlayerController controller,
            GameApplicationService gameApplicationService,
            GameSession session,
            AiMove move);

    @Override
    void close();
}
