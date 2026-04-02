package com.kotva.application.service;

import com.kotva.application.session.GameSession;

public interface ClockService {
    void startTurnClock(GameSession session);

    void stopTurnClock(GameSession session);

    void tick(GameSession session, long elapsedMillis);

    boolean isCurrentPlayerTimeout(GameSession session);

    void handleTimeout(GameSession session);
}
