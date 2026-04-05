package com.kotva.application.service;

import com.kotva.application.session.GameSession;
import com.kotva.domain.model.GameState;
import com.kotva.domain.model.Player;

public interface ClockService {
    void startTurnClock(GameSession session, String playerId);

    void stopTurnClock(GameSession session, String playerId);

    void tick(GameSession session, String playerId);

    void handleTimeout(GameSession session, String playerId);
}
class ClockServiceImpl implements ClockService {
    String currentPlayerId;
    long turnStartTime;
    long turnEndTime;
    long remainingTime;
    long elapsedTime;
    long curTime;
    public void startTurnClock(GameSession session, String playerId) {
        GameState state = session.getGameState();
        Player curPlayer = state.getPlayerById(playerId);
        if (curPlayer == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        this.currentPlayerId = playerId;
        this.remainingTime = curPlayer.getClockTimeRemaining();
        this.turnStartTime = System.currentTimeMillis();
    }
    public void stopTurnClock(GameSession session, String playerId){
        GameState state = session.getGameState();
        Player curPlayer = state.getPlayerById(playerId);

        if (curPlayer == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        elapsedTime = System.currentTimeMillis() - turnStartTime;
        remainingTime -= elapsedTime;
        curPlayer.updateClockTimeRemaining(remainingTime);

    }

    public void tick(GameSession session, String playerId){
        GameState state = session.getGameState();
        Player curPlayer = state.getPlayerById(playerId);
        if (curPlayer == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        elapsedTime = System.currentTimeMillis() - turnStartTime;
        curTime = remainingTime - elapsedTime;
        if(curTime <= 0){
            handleTimeout(session, playerId);
        }
        curPlayer.updateClockTimeRemaining(curTime);

    }
    public void handleTimeout(GameSession session, String playerId){
        GameState state = session.getGameState();
        Player curPlayer = state.getPlayerById(playerId);
        if (curPlayer == null) {
            throw new IllegalArgumentException("Player not found: " + playerId);
        }
        //TODO: sent a timeout action to the controller, and advance to the next player.
    }
}
