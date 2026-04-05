package com.kotva.application.session;

import com.kotva.application.TurnCoordinator;
import com.kotva.application.draft.TurnDraft;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.domain.model.GameState;
import java.util.Objects;

public class GameSession {
    private final String sessionId;
    private final GameConfig config;
    private final GameState gameState;
    private final TurnDraft turnDraft;
    private final RoundPassTracker roundPassTracker;
    private final SettlementService settlementService;
    private final TurnCoordinator turnCoordinator;
    private SessionStatus sessionStatus;

    public GameSession(String sessionId, GameConfig config, GameState gameState) {
        this(
                sessionId,
                config,
                gameState,
                new TurnDraft(),
                SessionStatus.WAITING_FOR_PLAYERS,
                new SettlementServiceImpl());
    }

    public GameSession(
            String sessionId,
            GameConfig config,
            GameState gameState,
            TurnDraft turnDraft,
            SessionStatus sessionStatus) {
        this(sessionId, config, gameState, turnDraft, sessionStatus, new SettlementServiceImpl());
    }

    public GameSession(
            String sessionId,
            GameConfig config,
            GameState gameState,
            TurnDraft turnDraft,
            SessionStatus sessionStatus,
            SettlementService settlementService) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.config = Objects.requireNonNull(config, "config cannot be null.");
        this.gameState = Objects.requireNonNull(gameState, "gameState cannot be null.");
        this.turnDraft = Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
        this.settlementService =
                Objects.requireNonNull(settlementService, "settlementService cannot be null.");
        this.roundPassTracker = new RoundPassTracker();
        this.turnCoordinator = new TurnCoordinator(gameState, settlementService);
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

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
    }

    public RoundPassTracker getRoundPassTracker() {
        return roundPassTracker;
    }

    public SettlementService getSettlementService() {
        return settlementService;
    }

    public TurnCoordinator getTurnCoordinator() {
        return turnCoordinator;
    }
}
