package com.kotva.application.session;

import java.util.Objects;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.domain.model.GameState;
import com.kotva.policy.SessionStatus;

public class GameSession {
    private final String sessionId;  
    private final GameConfig config;
    private final GameState gameState; 
    private TurnDraft turnDraft;
    private SessionStatus sessionStatus;
    private final RoundPassTracker roundPassTracker;
    private final SettlementService settlementService;

    public GameSession(String sessionId, GameConfig config, GameState gameState) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.config = Objects.requireNonNull(config, "config cannot be null.");
        this.gameState = Objects.requireNonNull(gameState, "gameState cannot be null.");
        this.turnDraft = new TurnDraft();
        this.sessionStatus = SessionStatus.WAITING_FOR_PLAYERS;
        this.roundPassTracker = new RoundPassTracker();
        this.settlementService = new SettlementServiceImpl();
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

    public void setTurnDraft(TurnDraft turnDraft) {
        this.turnDraft = Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");
    }

    public void resetTurnDraft() {
        this.turnDraft = new TurnDraft();
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
    }

    public RoundPassTracker getRoundPassTracker() {
        return roundPassTracker;
    }

    public SettlementService getSettlementService() {
        return settlementService;
    }

}
