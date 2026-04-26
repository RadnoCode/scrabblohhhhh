package com.kotva.application.session;

import com.kotva.policy.ClockPhase;
import java.io.Serializable;
import java.util.Objects;

/**
 * Snapshot of one player's clock state.
 */
public class PlayerClockSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final long mainTimeRemainingMillis;
    private final long byoYomiRemainingMillis;
    private final ClockPhase phase;
    private final boolean active;

    /**
     * Creates a player clock snapshot.
     *
     * @param playerId player id
     * @param playerName player display name
     * @param mainTimeRemainingMillis remaining main time
     * @param byoYomiRemainingMillis remaining per-turn time
     * @param phase current clock phase
     * @param active whether this clock is currently active
     */
    public PlayerClockSnapshot(
        String playerId,
        String playerName,
        long mainTimeRemainingMillis,
        long byoYomiRemainingMillis,
        ClockPhase phase,
        boolean active) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null.");
        this.playerName = Objects.requireNonNull(playerName, "playerName cannot be null.");
        this.mainTimeRemainingMillis = mainTimeRemainingMillis;
        this.byoYomiRemainingMillis = byoYomiRemainingMillis;
        this.phase = Objects.requireNonNull(phase, "phase cannot be null.");
        this.active = active;
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
     * Gets remaining main time.
     *
     * @return remaining main time in milliseconds
     */
    public long getMainTimeRemainingMillis() {
        return mainTimeRemainingMillis;
    }

    /**
     * Gets remaining byo-yomi time.
     *
     * @return remaining byo-yomi time in milliseconds
     */
    public long getByoYomiRemainingMillis() {
        return byoYomiRemainingMillis;
    }

    /**
     * Gets the current clock phase.
     *
     * @return clock phase
     */
    public ClockPhase getPhase() {
        return phase;
    }

    /**
     * Checks whether this clock is active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return active;
    }
}
