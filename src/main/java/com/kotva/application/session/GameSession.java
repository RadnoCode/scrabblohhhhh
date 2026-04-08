package com.kotva.application.session;

import com.kotva.application.TurnCoordinator;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.domain.model.GameState;
import com.kotva.policy.SessionStatus;
import java.util.Objects;

public class GameSession {
    private final String sessionId;
    private final GameConfig config;
    private final GameState gameState;
    private TurnDraft turnDraft;
    private SessionStatus sessionStatus;
    private final SettlementService settlementService;
    private final TurnCoordinator turnCoordinator;

    public GameSession(String sessionId, GameConfig config, GameState gameState) {
        this(sessionId, config, gameState, new SettlementServiceImpl());
    }

    public GameSession(
            String sessionId,
            GameConfig config,
            GameState gameState,
            SettlementService settlementService) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.config = Objects.requireNonNull(config, "config cannot be null.");
        this.gameState = Objects.requireNonNull(gameState, "gameState cannot be null.");
        this.turnDraft = new TurnDraft();
        this.sessionStatus = SessionStatus.WAITING_FOR_PLAYERS;
        this.settlementService =
                Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.turnCoordinator = new TurnCoordinator(gameState, this.settlementService);
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

    public SettlementService getSettlementService() {
        return settlementService;
    }

    public TurnCoordinator getTurnCoordinator() {
        return turnCoordinator;
    }
}
