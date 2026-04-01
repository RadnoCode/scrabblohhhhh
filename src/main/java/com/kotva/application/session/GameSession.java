package com.kotva.application.session;

import java.util.List;

import com.kotva.domain.model.GameState;
import com.kotva.application.draft.TurnDraft;

public class GameSession {
    private String sessionId;
    private GameConfig config;
    private GameState state;
    private TurnDraft currentDraft;
    private SessionStatus sessionStatus;
    private List<PlayerController> playerControllers;
    private RoundPassTracker roundPassTracker;

    public String getSessionId() {
        return sessionId;
    }

}
