package com.kotva.application.session;

public class TimeControlConfig {
    private final long mainTimeMillis;
    private final long byoYomiMillisPerTurn;

    public TimeControlConfig(long mainTimeMillis, long byoYomiMillisPerTurn) {
        if (mainTimeMillis <= 0) {
            throw new IllegalArgumentException("mainTimeMillis must be greater than 0.");
        }
        if (byoYomiMillisPerTurn <= 0) {
            throw new IllegalArgumentException("byoYomiMillisPerTurn must be greater than 0.");
        }
        this.mainTimeMillis = mainTimeMillis;
        this.byoYomiMillisPerTurn = byoYomiMillisPerTurn;
    }

    public long getMainTimeMillis() {
        return mainTimeMillis;
    }

    public long getByoYomiMillisPerTurn() {
        return byoYomiMillisPerTurn;
    }
}