package com.kotva.application.session;


import com.kotva.domain.model.GameState;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.TurnCoordinator;

public class GameSession {
    private String sessionId;
    private GameConfig config;
    private GameState state;
    private TurnDraft currentDraft;
    private SessionStatus sessionStatus;
    private TurnCoordinator turnCoordinator;

    public String getSessionId() {
        return sessionId;
    }

}
