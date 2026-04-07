package com.kotva.mode;

import java.util.Objects;

import com.kotva.application.service.GameApplicationService;
import com.kotva.application.service.SubmitDraftResult;
import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;
import com.kotva.policy.PlayerType;

final class LANPlayerController extends PlayerController {
    // TODO: 补充字段

    LANPlayerController(String playerId) {
        super(playerId, PlayerType.LAN);

    }
    private CommandEnvelope buildCommand(PlayerAction action,GameSession session){
        
    } {

    }

    public SubmitDraftResult submitDraft(GameSession session) {
        return requireService(service).submitDraft(session);
    }

    public TurnTransitionResult passTurn(GameApplicationService service, GameSession session) {
        return requireService(service).passTurn(session);
    }
    private GameApplicationService requireService(GameApplicationService service) {
        return Objects.requireNonNull(service, "service cannot be null.");
    }
}
