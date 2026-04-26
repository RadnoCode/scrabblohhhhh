package com.kotva.application.session;

import com.kotva.policy.PlayerType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one player for the game UI.
 */
public class GamePlayerSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final PlayerType playerType;
    private final int score;
    private final boolean active;
    private final boolean currentTurn;
    private final int rackTileCount;
    private final PlayerClockSnapshot clockSnapshot;

    /**
     * Creates a player snapshot.
     *
     * @param playerId player id
     * @param playerName player display name
     * @param playerType player type
     * @param score current score
     * @param active whether the player is still active
     * @param currentTurn whether this player has the current turn
     * @param rackTileCount number of tiles in the rack
     * @param clockSnapshot clock status for this player
     */
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

    /**
     * Gets the player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the player type.
     *
     * @return player type
     */
    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * Gets the current score.
     *
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * Checks whether the player is active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks whether it is this player's turn.
     *
     * @return {@code true} if current turn
     */
    public boolean isCurrentTurn() {
        return currentTurn;
    }

    /**
     * Gets the number of rack tiles.
     *
     * @return rack tile count
     */
    public int getRackTileCount() {
        return rackTileCount;
    }

    /**
     * Gets this player's clock snapshot.
     *
     * @return clock snapshot
     */
    public PlayerClockSnapshot getClockSnapshot() {
        return clockSnapshot;
    }
}
