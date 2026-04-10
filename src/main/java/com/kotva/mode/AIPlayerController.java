package com.kotva.mode;

import com.kotva.ai.AiMove;
import com.kotva.application.service.AiTurnCoordinator;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.session.GameSession;
import com.kotva.policy.PlayerType;
import java.util.concurrent.CompletableFuture;

final class AIPlayerController extends PlayerController {
    AIPlayerController(String playerId) {
        super(playerId, PlayerType.AI);
    }

    @Override
    public boolean supportsAutomatedTurn() {
        return true;
    }

    @Override
    public CompletableFuture<AiMove> requestAutomatedTurn(
            AiTurnCoordinator aiTurnCoordinator, GameSession session) {
        return aiTurnCoordinator.requestMove(session);
    }

    @Override
    public AiTurnCoordinator.ExecutionResult applyAutomatedTurn(
            AiTurnCoordinator aiTurnCoordinator,
            GameApplicationService gameApplicationService,
            GameSession session,
            AiMove move) {
        return aiTurnCoordinator.applyMove(this, gameApplicationService, session, move);
    }
}
