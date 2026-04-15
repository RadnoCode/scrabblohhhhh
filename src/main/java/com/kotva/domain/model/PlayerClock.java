package com.kotva.domain.model;

import com.kotva.policy.ClockPhase;
import java.util.Objects;

public class PlayerClock {
    private long mainTimeRemainingMillis;
    private final long byoYomiPerTurnMillis;
    private long byoYomiRemainingMillis;
    private ClockPhase phase;

    public PlayerClock(
        long mainTimeRemainingMillis,
        long byoYomiPerTurnMillis,
        long byoYomiRemainingMillis,
        ClockPhase phase) {
        if (mainTimeRemainingMillis < 0) {
            throw new IllegalArgumentException("mainTimeRemainingMillis cannot be negative.");
        }
        if (byoYomiPerTurnMillis < 0) {
            throw new IllegalArgumentException("byoYomiPerTurnMillis cannot be negative.");
        }
        if (byoYomiRemainingMillis < 0) {
            throw new IllegalArgumentException("byoYomiRemainingMillis cannot be negative.");
        }
        this.mainTimeRemainingMillis = mainTimeRemainingMillis;
        this.byoYomiPerTurnMillis = byoYomiPerTurnMillis;
        this.byoYomiRemainingMillis = byoYomiRemainingMillis;
        this.phase = Objects.requireNonNull(phase, "phase cannot be null.");
    }

    public static PlayerClock disabled() {
        return new PlayerClock(0L, 0L, 0L, ClockPhase.DISABLED);
    }

    public static PlayerClock timed(long mainTimeMillis, long byoYomiMillisPerTurn) {
        return new PlayerClock(
            mainTimeMillis,
            byoYomiMillisPerTurn,
            byoYomiMillisPerTurn,
            ClockPhase.MAIN_TIME);
    }

    public long getMainTimeRemainingMillis() {
        return mainTimeRemainingMillis;
    }

    public void setMainTimeRemainingMillis(long mainTimeRemainingMillis) {
        if (mainTimeRemainingMillis < 0) {
            throw new IllegalArgumentException("mainTimeRemainingMillis cannot be negative.");
        }
        this.mainTimeRemainingMillis = mainTimeRemainingMillis;
    }

    public long getByoYomiPerTurnMillis() {
        return byoYomiPerTurnMillis;
    }

    public long getByoYomiRemainingMillis() {
        return byoYomiRemainingMillis;
    }

    public void setByoYomiRemainingMillis(long byoYomiRemainingMillis) {
        if (byoYomiRemainingMillis < 0) {
            throw new IllegalArgumentException("byoYomiRemainingMillis cannot be negative.");
        }
        this.byoYomiRemainingMillis = byoYomiRemainingMillis;
    }

    public ClockPhase getPhase() {
        return phase;
    }

    public void setPhase(ClockPhase phase) {
        this.phase = Objects.requireNonNull(phase, "phase cannot be null.");
    }

    public boolean isEnabled() {
        return phase != ClockPhase.DISABLED;
    }

    public void resetByoYomiTurn() {
        byoYomiRemainingMillis = byoYomiPerTurnMillis;
    }
}