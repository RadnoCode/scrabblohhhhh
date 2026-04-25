package com.kotva.application.service.lan;

import com.kotva.application.service.GameActionResult;
import com.kotva.application.service.GameApplicationService;
import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.domain.action.PlayerAction;
import com.kotva.infrastructure.network.CommandEnvelope;
import com.kotva.infrastructure.network.RemoteCommandResult;
import com.kotva.policy.SessionStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Host-side service for executing LAN client commands.
 */
public class LanHostService {
    private final GameSession session;
    private final GameApplicationService gameApplicationService;
    private final Map<String, RemoteCommandResult> cachedResultsByCommandId;

    /**
     * Creates a LAN host service.
     *
     * @param session host game session
     * @param gameApplicationService application service used to execute commands
     */
    public LanHostService(GameSession session, GameApplicationService gameApplicationService) {
        this.session = Objects.requireNonNull(session, "session cannot be null.");
        this.gameApplicationService =
                Objects.requireNonNull(gameApplicationService, "gameApplicationService cannot be null.");
        this.cachedResultsByCommandId = new LinkedHashMap<>();
    }

    /**
     * Handles a remote command and caches the result by command id.
     *
     * @param commandEnvelope command from a client
     * @return remote command result
     */
    public synchronized RemoteCommandResult handle(CommandEnvelope commandEnvelope) {
        Objects.requireNonNull(commandEnvelope, "commandEnvelope cannot be null.");
        RemoteCommandResult cachedResult = cachedResultsByCommandId.get(commandEnvelope.getCommandId());
        if (cachedResult != null) {
            return cachedResult;
        }

        RemoteCommandResult result = execute(commandEnvelope);
        cachedResultsByCommandId.put(commandEnvelope.getCommandId(), result);
        return result;
    }

    /**
     * Builds a viewer-specific snapshot.
     *
     * @param viewerPlayerId player who will view the snapshot
     * @return filtered snapshot
     */
    public synchronized GameSessionSnapshot snapshotForViewer(String viewerPlayerId) {
        return GameSessionSnapshotFactory.fromSessionForViewer(session, viewerPlayerId);
    }

    /**
     * Validates and executes a remote command.
     *
     * @param commandEnvelope command from a client
     * @return remote command result
     */
    private RemoteCommandResult execute(CommandEnvelope commandEnvelope) {
        try {
            validateCommandEnvelope(commandEnvelope);
            GameActionResult dispatchResult =
                    gameApplicationService.executeRemoteCommand(
                            session,
                            commandEnvelope.getAction(),
                            commandEnvelope.getCommandId());
            return new RemoteCommandResult(
                    commandEnvelope.getCommandId(),
                    dispatchResult.isSuccess(),
                    dispatchResult.getMessage(),
                    dispatchResult.getAwardedScore(),
                    dispatchResult.getNextPlayerId(),
                    dispatchResult.isGameEnded(),
                    session.getTurnCoordinator().getSettlementResult(),
                    snapshotForViewer(commandEnvelope.getPlayerId()));
        } catch (RuntimeException exception) {
            return new RemoteCommandResult(
                    commandEnvelope.getCommandId(),
                    false,
                    resolveMessage(exception),
                    0,
                    session.getGameState().getCurrentPlayer().getPlayerId(),
                    session.getSessionStatus() == SessionStatus.COMPLETED,
                    session.getTurnCoordinator().getSettlementResult(),
                    snapshotForViewer(commandEnvelope.getPlayerId()));
        }
    }

    /**
     * Validates command session, player id, and turn number.
     *
     * @param commandEnvelope command to validate
     */
    private void validateCommandEnvelope(CommandEnvelope commandEnvelope) {
        if (!Objects.equals(session.getSessionId(), commandEnvelope.getSessionId())) {
            throw new IllegalArgumentException("Command belongs to a different session.");
        }

        PlayerAction action = Objects.requireNonNull(commandEnvelope.getAction(), "action cannot be null.");
        if (!Objects.equals(commandEnvelope.getPlayerId(), action.playerId())) {
            throw new IllegalArgumentException("Command playerId does not match action playerId.");
        }

        if (commandEnvelope.getExpectedTurnNumber() != session.getTurnCoordinator().getTurnNumber()) {
            throw new IllegalStateException(
                    "Turn number mismatch. Expected "
                            + session.getTurnCoordinator().getTurnNumber()
                            + " but got "
                            + commandEnvelope.getExpectedTurnNumber()
                            + ".");
        }
    }

    /**
     * Converts an exception into a readable command rejection message.
     *
     * @param exception exception raised by command handling
     * @return readable message
     */
    private String resolveMessage(RuntimeException exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "Host rejected the command due to an unexpected error.";
        }
        return exception.getMessage();
    }
}
