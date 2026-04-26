package com.kotva.application.service;

import com.kotva.application.session.GameSession;

/**
 * Defines clock operations for a game session.
 */
public interface ClockService {

    /**
     * Starts the clock for the current player.
     *
     * @param session game session
     */
    void startTurnClock(GameSession session);

    /**
     * Stops the current player's clock.
     *
     * @param session game session
     */
    void stopTurnClock(GameSession session);

    /**
     * Advances the clock by elapsed time.
     *
     * @param session game session
     * @param elapsedMillis elapsed time in milliseconds
     */
    void tick(GameSession session, long elapsedMillis);

    /**
     * Applies timeout handling when a player runs out of time.
     *
     * @param session game session
     */
    void handleTimeout(GameSession session);
}
