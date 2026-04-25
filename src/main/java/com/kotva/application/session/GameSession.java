package com.kotva.application.session;

import com.kotva.application.draft.TurnDraft;
import com.kotva.application.service.GameActionResult;
import com.kotva.application.service.SettlementService;
import com.kotva.application.service.SettlementServiceImpl;
import com.kotva.application.turn.TurnCoordinator;
import com.kotva.domain.model.GameState;
import com.kotva.policy.SessionStatus;
import java.io.Serializable;
import java.util.Objects;

/**
 * Holds the application state for one game session.
 */
public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    private final GameConfig config;
    private final GameState gameState;
    private TurnDraft turnDraft;
    private SessionStatus sessionStatus;
    private final SettlementService settlementService;
    private final TurnCoordinator turnCoordinator;
    private long nextActionSequence;
    private GameActionResult latestActionResult;

    /**
     * Creates a game session with the default settlement service.
     *
     * @param sessionId unique session id
     * @param config game config
     * @param gameState domain game state
     */
    public GameSession(String sessionId, GameConfig config, GameState gameState) {
        this(sessionId, config, gameState, new SettlementServiceImpl());
    }

    /**
     * Creates a game session.
     *
     * @param sessionId unique session id
     * @param config game config
     * @param gameState domain game state
     * @param settlementService service used when the game ends
     */
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
        this.nextActionSequence = 1L;
    }

    /**
     * Gets the session id.
     *
     * @return session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the game config.
     *
     * @return game config
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * Gets the domain game state.
     *
     * @return game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the current turn draft.
     *
     * @return turn draft
     */
    public TurnDraft getTurnDraft() {
        return turnDraft;
    }

    /**
     * Replaces the current turn draft.
     *
     * @param turnDraft new turn draft
     */
    public void setTurnDraft(TurnDraft turnDraft) {
        this.turnDraft = Objects.requireNonNull(turnDraft, "turnDraft cannot be null.");
    }

    /**
     * Clears the current turn draft.
     */
    public void resetTurnDraft() {
        this.turnDraft = new TurnDraft();
    }

    /**
     * Gets the current session status.
     *
     * @return session status
     */
    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    /**
     * Updates the session status.
     *
     * @param sessionStatus new session status
     */
    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
    }

    /**
     * Gets the settlement service.
     *
     * @return settlement service
     */
    public SettlementService getSettlementService() {
        return settlementService;
    }

    /**
     * Gets the turn coordinator.
     *
     * @return turn coordinator
     */
    public TurnCoordinator getTurnCoordinator() {
        return turnCoordinator;
    }

    /**
     * Creates a unique id for the next action in this session.
     *
     * @return action id
     */
    public String issueActionId() {
        return sessionId + "-action-" + nextActionSequence++;
    }

    /**
     * Gets the latest action result.
     *
     * @return latest action result, or {@code null}
     */
    public GameActionResult getLatestActionResult() {
        return latestActionResult;
    }

    /**
     * Stores the latest action result.
     *
     * @param latestActionResult action result to store
     */
    public void setLatestActionResult(GameActionResult latestActionResult) {
        this.latestActionResult =
        Objects.requireNonNull(latestActionResult, "latestActionResult cannot be null.");
    }
}
