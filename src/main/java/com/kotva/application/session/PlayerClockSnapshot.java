package com.kotva.application.session;

import com.kotva.policy.ClockPhase;
import java.io.Serializable;
import java.util.Objects;

public class PlayerClockSnapshot implements Serializable {
    private final String playerId;
    private final String playerName;
    private final long mainTimeRemainingMillis;
    private final long byoYomiRemainingMillis;
    private final ClockPhase phase;
    private final boolean active;

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

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getMainTimeRemainingMillis() {
        return mainTimeRemainingMillis;
    }

    public long getByoYomiRemainingMillis() {
        return byoYomiRemainingMillis;
    }

    public ClockPhase getPhase() {
        return phase;
    }

    public boolean isActive() {
        return active;
    }
}
