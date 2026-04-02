package com.kotva.application.coordinator;

import com.kotva.application.service.TurnTransitionResult;
import com.kotva.application.session.GameSession;

public interface TurnCoordinator {
    TurnTransitionResult onDraftSubmitted(GameSession session);

    TurnTransitionResult onPass(GameSession session);

    TurnTransitionResult onTimeout(GameSession session);

    void confirmHotSeatHandoff(GameSession session);
}
