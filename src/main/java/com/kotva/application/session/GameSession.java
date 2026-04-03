package com.kotva.application.session;


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

    public GameConfig getConfig() {
        return config;
    }

    public GameState getGameState() {
        return gameState;
    }

    public TurnDraft getTurnDraft() {
        return turnDraft;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public List<PlayerController> getPlayerControllers() {
        return playerControllers;
    }

    public RoundPassTracker getRoundPassTracker() {
        return roundPassTracker;
    }

}
