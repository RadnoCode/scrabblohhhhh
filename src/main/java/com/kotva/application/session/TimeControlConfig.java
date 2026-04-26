package com.kotva.application.session;

import java.io.Serializable;

/**
 * Time settings used for each player in a game.
 */
public class TimeControlConfig implements Serializable {
    private final long mainTimeMillis;
    private final long byoYomiMillisPerTurn;

    /**
     * Creates a time control config.
     *
     * @param mainTimeMillis total main time in milliseconds
     * @param byoYomiMillisPerTurn per-turn extra time in milliseconds
     */
    public TimeControlConfig(long mainTimeMillis, long byoYomiMillisPerTurn) {
        if (mainTimeMillis <= 0) {
            throw new IllegalArgumentException("mainTimeMillis must be greater than 0.");
        }
        if (byoYomiMillisPerTurn < 0) {
            throw new IllegalArgumentException("byoYomiMillisPerTurn cannot be negative.");
        }
        this.mainTimeMillis = mainTimeMillis;
        this.byoYomiMillisPerTurn = byoYomiMillisPerTurn;
    }

    /**
     * Gets the main time.
     *
     * @return main time in milliseconds
     */
    public long getMainTimeMillis() {
        return mainTimeMillis;
    }

    /**
     * Gets the per-turn byo-yomi time.
     *
     * @return byo-yomi time in milliseconds
     */
    public long getByoYomiMillisPerTurn() {
        return byoYomiMillisPerTurn;
    }
}
