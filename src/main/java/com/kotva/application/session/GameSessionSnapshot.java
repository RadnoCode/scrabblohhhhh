package com.kotva.application.session;

import com.kotva.application.result.BoardSnapshot;
import com.kotva.application.result.SettlementResult;
import com.kotva.domain.endgame.GameEndReason;
import com.kotva.mode.GameMode;
import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

public class GameSessionSnapshot {
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
    private final SettlementResult settlementResult;

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
            SettlementResult settlementResult) {
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
        this.currentPlayerClockPhase =
                Objects.requireNonNull(
                        currentPlayerClockPhase, "currentPlayerClockPhase cannot be null.");
        this.playerClockSnapshots =
                List.copyOf(
                        Objects.requireNonNull(
                                playerClockSnapshots, "playerClockSnapshots cannot be null."));
        this.players = List.copyOf(Objects.requireNonNull(players, "players cannot be null."));
        this.boardSnapshot = Objects.requireNonNull(boardSnapshot, "boardSnapshot cannot be null.");
        this.boardCells = List.copyOf(Objects.requireNonNull(boardCells, "boardCells cannot be null."));
        this.currentRackTiles =
                List.copyOf(
                        Objects.requireNonNull(currentRackTiles, "currentRackTiles cannot be null."));
        this.draftPlacements =
                List.copyOf(
                        Objects.requireNonNull(draftPlacements, "draftPlacements cannot be null."));
        this.preview = preview;
        this.settlementResult = settlementResult;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public GameEndReason getGameEndReason() {
        return gameEndReason;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public long getCurrentPlayerMainTimeRemainingMillis() {
        return currentPlayerMainTimeRemainingMillis;
    }

    public long getCurrentPlayerByoYomiRemainingMillis() {
        return currentPlayerByoYomiRemainingMillis;
    }

    public ClockPhase getCurrentPlayerClockPhase() {
        return currentPlayerClockPhase;
    }

    public List<PlayerClockSnapshot> getPlayerClockSnapshots() {
        return playerClockSnapshots;
    }

    public List<GamePlayerSnapshot> getPlayers() {
        return players;
    }

    public BoardSnapshot getBoardSnapshot() {
        return boardSnapshot;
    }

    public List<BoardCellRenderSnapshot> getBoardCells() {
        return boardCells;
    }

    public List<RackTileSnapshot> getCurrentRackTiles() {
        return currentRackTiles;
    }

    public List<DraftPlacementSnapshot> getDraftPlacements() {
        return draftPlacements;
    }

    public PreviewSnapshot getPreview() {
        return preview;
    }

    public SettlementResult getSettlementResult() {
        return settlementResult;
    }
}
