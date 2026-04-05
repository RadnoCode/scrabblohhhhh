package com.kotva.application.session;

import com.kotva.policy.ClockPhase;
import com.kotva.policy.SessionStatus;
import java.util.List;
import java.util.Objects;

public class GameSessionSnapshot {
    private final SessionStatus sessionStatus;
    private final String currentPlayerId;
    private final String currentPlayerName;
    private final long currentPlayerMainTimeRemainingMillis;
    private final long currentPlayerByoYomiRemainingMillis;
    private final ClockPhase currentPlayerClockPhase;
    private final List<PlayerClockSnapshot> playerClockSnapshots;

    public GameSessionSnapshot(
            SessionStatus sessionStatus,
            String currentPlayerId,
            String currentPlayerName,
            long currentPlayerMainTimeRemainingMillis,
            long currentPlayerByoYomiRemainingMillis,
            ClockPhase currentPlayerClockPhase,
            List<PlayerClockSnapshot> playerClockSnapshots) {
        this.sessionStatus = Objects.requireNonNull(sessionStatus, "sessionStatus cannot be null.");
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
        this.currentPlayerMainTimeRemainingMillis = currentPlayerMainTimeRemainingMillis;
        this.currentPlayerByoYomiRemainingMillis = currentPlayerByoYomiRemainingMillis;
        this.currentPlayerClockPhase =
                Objects.requireNonNull(currentPlayerClockPhase, "currentPlayerClockPhase cannot be null.");
        this.playerClockSnapshots =
                List.copyOf(Objects.requireNonNull(playerClockSnapshots, "playerClockSnapshots cannot be null."));
    }

    public SessionStatus getSessionStatus() {
        return sessionStatus;
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
}
