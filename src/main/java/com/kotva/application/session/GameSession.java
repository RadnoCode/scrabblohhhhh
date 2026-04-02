package com.kotva.application.session;

import java.util.List;

import com.kotva.application.draft.TurnDraft;
import com.kotva.domain.model.GameState;
import com.kotva.mode.PlayerController;

public class GameSession {
    private final String sessionId;  // Unique identifier for the session
    private final GameConfig config;  // Configuration for the game (mode, players, etc.)
    private GameState gameState;  // Current state of the game, including board, players, tile bag and current turn
    private TurnDraft turnDraft;  // Draft for the current turn, including proposed moves and player actions
    private SessionStatus sessionStatus;  // Status of the session (e.g., WAITING_FOR_PLAYERS, IN_PROGRESS, COMPLETED)
    private final List<PlayerController> playerControllers;  // Controllers for each player, managing their interactions and turns
    private final RoundPassTracker roundPassTracker;  // Tracker for players who have passed in the current round

    public GameSession
    (
        String sessionId,
        GameConfig config,
        GameState gameState,
        TurnDraft turnDraft,
        SessionStatus sessionStatus,
        List<PlayerController> playerControllers,
        RoundPassTracker roundPassTracker
    ) 
    {
        this.sessionId = sessionId;
        this.config = config;
        this.gameState = gameState;
        this.turnDraft = turnDraft;
        this.sessionStatus = sessionStatus;
        this.playerControllers = playerControllers;
        this.roundPassTracker = roundPassTracker;
    }

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
