package com.kotva.application.session;

import com.kotva.policy.PlayerType;
import java.io.Serializable;
import java.util.Objects;

public class GamePlayerSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final PlayerType playerType;
    private final int score;
    private final boolean active;
    private final boolean currentTurn;
    private final int rackTileCount;
    private final PlayerClockSnapshot clockSnapshot;

    public GamePlayerSnapshot(
        String playerId,
        String playerName,
        PlayerType playerType,
        int score,
        boolean active,
        boolean currentTurn,
        int rackTileCount,
        PlayerClockSnapshot clockSnapshot) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.playerType = Objects.requireNonNull(playerType, "playerType cannot be null.");
        this.score = score;
        this.active = active;
        this.currentTurn = currentTurn;
        this.rackTileCount = rackTileCount;
        this.clockSnapshot = Objects.requireNonNull(clockSnapshot, "clockSnapshot cannot be null.");
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public int getScore() {
        return score;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCurrentTurn() {
        return currentTurn;
    }

    public int getRackTileCount() {
        return rackTileCount;
    }

    public PlayerClockSnapshot getClockSnapshot() {
        return clockSnapshot;
    }
}
