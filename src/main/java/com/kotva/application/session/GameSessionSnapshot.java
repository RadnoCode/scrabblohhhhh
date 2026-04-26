package com.kotva.application.session;

import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.SettlementResult;
import com.kotva.application.service.GameActionResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.mode.GameMode;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Serializable view of a game session for UI and network transfer.
 */
public class GameSessionSnapshot implements Serializable {
    private final String sessionId;
    private final GameMode gameMode;
    private final SessionStatus sessionStatus;
    private final boolean gameEnded;
    private final GameEndReason gameEndReason;
    private final int turnNumber;
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final long currentPlayerMainTimeRemainingMillis;
    private final long currentPlayerByoYomiRemainingMillis;
    private final ClockPhase currentPlayerClockPhase;
    private final List<PlayerClockSnapshot> playerClockSnapshots;
    private final List<GamePlayerSnapshot> players;
    private final BoardSnapshot boardSnapshot;
    private final List<BoardCellRenderSnapshot> boardCells;
    private final List<RackTileSnapshot> currentRackTiles;
    private final List<DraftPlacementSnapshot> draftPlacements;
    private final PreviewSnapshot preview;
    private final TutorialSnapshot tutorial;
    private final GameActionResult latestActionResult;
    private final SettlementResult settlementResult;
    private final AiRuntimeSnapshot aiRuntimeSnapshot;
    private final ClientRuntimeSnapshot clientRuntimeSnapshot;
    private final long snapshotSentAtEpochMillis;
    private final long snapshotReceivedAtEpochMillis;

    /**
     * Creates a full game session snapshot.
     *
     * @param sessionId session id
     * @param gameMode game mode
     * @param sessionStatus current session status
     * @param gameEnded whether the game has ended
     * @param gameEndReason reason why the game ended
     * @param turnNumber current turn number
     * @param currentPlayerId id of the current player
     * @param currentPlayerName name of the current player
     * @param currentPlayerMainTimeRemainingMillis current player's main time
     * @param currentPlayerByoYomiRemainingMillis current player's per-turn time
     * @param currentPlayerClockPhase current player's clock phase
     * @param playerClockSnapshots all player clocks
     * @param players all player snapshots
     * @param boardSnapshot final-style board snapshot
     * @param boardCells board cells prepared for rendering
     * @param currentRackTiles rack tiles visible to the viewer
     * @param draftPlacements visible draft placements
     * @param preview visible preview data
     * @param tutorial tutorial data
     * @param latestActionResult latest action result
     * @param settlementResult settlement result after game end
     * @param aiRuntimeSnapshot AI runtime status
     * @param clientRuntimeSnapshot LAN client runtime status
     * @param snapshotSentAtEpochMillis send timestamp
     * @param snapshotReceivedAtEpochMillis receive timestamp
     */
    public GameSessionSnapshot(
            String sessionId,
            GameMode gameMode,
            SessionStatus sessionStatus,
            boolean gameEnded,
            GameEndReason gameEndReason,
            int turnNumber,
            String currentPlayerId,
            String currentPlayerName,
            long currentPlayerMainTimeRemainingMillis,
            long currentPlayerByoYomiRemainingMillis,
            ClockPhase currentPlayerClockPhase,
            List<PlayerClockSnapshot> playerClockSnapshots,
            List<GamePlayerSnapshot> players,
            BoardSnapshot boardSnapshot,
            List<BoardCellRenderSnapshot> boardCells,
            List<RackTileSnapshot> currentRackTiles,
            List<DraftPlacementSnapshot> draftPlacements,
            PreviewSnapshot preview,
            TutorialSnapshot tutorial,
            GameActionResult latestActionResult,
            SettlementResult settlementResult,
            AiRuntimeSnapshot aiRuntimeSnapshot,
            ClientRuntimeSnapshot clientRuntimeSnapshot,
            long snapshotSentAtEpochMillis,
            long snapshotReceivedAtEpochMillis) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        this.gameMode = Objects.requireNonNull(gameMode, "gameMode cannot be null.");
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
        this.gameEnded = gameEnded;
        this.gameEndReason = gameEndReason;
        this.turnNumber = turnNumber;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.currentPlayerMainTimeRemainingMillis = currentPlayerMainTimeRemainingMillis;
        this.currentPlayerByoYomiRemainingMillis = currentPlayerByoYomiRemainingMillis;
        this.currentPlayerClockPhase = Objects.requireNonNull(
                currentPlayerClockPhase,
                "currentPlayerClockPhase cannot be null.");
        this.playerClockSnapshots = List.copyOf(Objects.requireNonNull(
                playerClockSnapshots,
                "playerClockSnapshots cannot be null."));
        this.players = List.copyOf(Objects.requireNonNull(players, "players cannot be null."));
        this.boardSnapshot = Objects.requireNonNull(boardSnapshot, "boardSnapshot cannot be null.");
        this.boardCells = List.copyOf(Objects.requireNonNull(boardCells, "boardCells cannot be null."));
        this.currentRackTiles = List.copyOf(Objects.requireNonNull(
                currentRackTiles,
                "currentRackTiles cannot be null."));
        this.draftPlacements = List.copyOf(Objects.requireNonNull(
                draftPlacements,
                "draftPlacements cannot be null."));
        this.preview = preview;
        this.tutorial = tutorial;
        this.latestActionResult = latestActionResult;
        this.settlementResult = settlementResult;
        this.aiRuntimeSnapshot = aiRuntimeSnapshot;
        this.clientRuntimeSnapshot = clientRuntimeSnapshot;
        this.snapshotSentAtEpochMillis = snapshotSentAtEpochMillis;
        this.snapshotReceivedAtEpochMillis = snapshotReceivedAtEpochMillis;
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
     * Gets the game mode.
     *
     * @return game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    /**
     * Gets the session status.
     *
     * @return session status
     */
    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    /**
     * Checks whether the game has ended.
     *
     * @return {@code true} if ended
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * Gets the game end reason.
     *
     * @return end reason, or {@code null}
     */
    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    /**
     * Gets the current turn number.
     *
     * @return turn number
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    /**
     * Gets the current player id.
     *
     * @return current player id
     */
    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    /**
     * Gets the current player name.
     *
     * @return current player name
     */
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    /**
     * Gets the current player's remaining main time.
     *
     * @return main time in milliseconds
     */
    public long getCurrentPlayerMainTimeRemainingMillis() {
        return currentPlayerMainTimeRemainingMillis;
    }

    /**
     * Gets the current player's remaining byo-yomi time.
     *
     * @return byo-yomi time in milliseconds
     */
    public long getCurrentPlayerByoYomiRemainingMillis() {
        return currentPlayerByoYomiRemainingMillis;
    }

    /**
     * Gets the current player's clock phase.
     *
     * @return clock phase
     */
    public ClockPhase getCurrentPlayerClockPhase() {
        return currentPlayerClockPhase;
    }

    /**
     * Gets all player clock snapshots.
     *
     * @return player clock snapshots
     */
    public List<PlayerClockSnapshot> getPlayerClockSnapshots() {
        return playerClockSnapshots;
    }

    /**
     * Gets all player snapshots.
     *
     * @return player snapshots
     */
    public List<GamePlayerSnapshot> getPlayers() {
        return players;
    }

    /**
     * Gets the board snapshot.
     *
     * @return board snapshot
     */
    public BoardSnapshot getBoardSnapshot() {
        return boardSnapshot;
    }

    /**
     * Gets board cells prepared for rendering.
     *
     * @return render cell snapshots
     */
    public List<BoardCellRenderSnapshot> getBoardCells() {
        return boardCells;
    }

    /**
     * Gets the rack tiles visible in this snapshot.
     *
     * @return visible rack tiles
     */
    public List<RackTileSnapshot> getCurrentRackTiles() {
        return currentRackTiles;
    }

    /**
     * Gets the rack tiles visible in this snapshot.
     *
     * @return visible rack tiles
     */
    public List<RackTileSnapshot> getVisibleRackTiles() {
        return currentRackTiles;
    }

    /**
     * Gets visible draft placements.
     *
     * @return draft placement snapshots
     */
    public List<DraftPlacementSnapshot> getDraftPlacements() {
        return draftPlacements;
    }

    /**
     * Gets visible preview data.
     *
     * @return preview snapshot, or {@code null}
     */
    public PreviewSnapshot getPreview() {
        return preview;
    }

    /**
     * Gets tutorial data.
     *
     * @return tutorial snapshot, or {@code null}
     */
    public TutorialSnapshot getTutorial() {
        return tutorial;
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
     * Gets settlement result after game end.
     *
     * @return settlement result, or {@code null}
     */
    public SettlementResult getSettlementResult() {
        return settlementResult;
    }

    /**
     * Gets AI runtime status.
     *
     * @return AI runtime snapshot, or {@code null}
     */
    public AiRuntimeSnapshot getAiRuntimeSnapshot() {
        return aiRuntimeSnapshot;
    }

    /**
     * Gets LAN client runtime status.
     *
     * @return client runtime snapshot, or {@code null}
     */
    public ClientRuntimeSnapshot getClientRuntimeSnapshot() {
        return clientRuntimeSnapshot;
    }

    /**
     * Gets the timestamp when the snapshot was sent.
     *
     * @return sent time in epoch milliseconds
     */
    public long getSnapshotSentAtEpochMillis() {
        return snapshotSentAtEpochMillis;
    }

    /**
     * Gets the timestamp when the snapshot was received.
     *
     * @return received time in epoch milliseconds
     */
    public long getSnapshotReceivedAtEpochMillis() {
        return snapshotReceivedAtEpochMillis;
    }

    /**
     * Creates a copy with updated tutorial data.
     *
     * @param tutorialSnapshot tutorial snapshot to attach
     * @return copied session snapshot
     */
    public GameSessionSnapshot withTutorial(TutorialSnapshot tutorialSnapshot) {
        return new GameSessionSnapshot(
                sessionId,
                gameMode,
                sessionStatus,
                gameEnded,
                gameEndReason,
                turnNumber,
                currentPlayerId,
                currentPlayerName,
                currentPlayerMainTimeRemainingMillis,
                currentPlayerByoYomiRemainingMillis,
                currentPlayerClockPhase,
                playerClockSnapshots,
                players,
                boardSnapshot,
                boardCells,
                currentRackTiles,
                draftPlacements,
                preview,
                tutorialSnapshot,
                latestActionResult,
                settlementResult,
                aiRuntimeSnapshot,
                clientRuntimeSnapshot,
                snapshotSentAtEpochMillis,
                snapshotReceivedAtEpochMillis);
    }
}
