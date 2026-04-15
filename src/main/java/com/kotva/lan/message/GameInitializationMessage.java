package com.kotva.lan.message;

import java.util.List;

import com.kotva.lan.LocalGameSession.PlayerBrief;

public class GameInitializationMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;
    //initialization snapshot sent by server to client when a player joins a session. Contains the current state of the game, including the list of players in the session and any relevant game data needed for the client to initialize its local game state.
    private final String sessionId;
    private final String hostPlayerId;
    private final List<PlayerBrief> players;
    
    public GameInitializationMessage(String sessionId, String hostPlayerId, List<PlayerBrief> players) {
        super(MessageType.GAME_INITIALIZATION);
        this.sessionId = sessionId;
        this.hostPlayerId = hostPlayerId;
        this.players = players;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public List<PlayerBrief> getPlayers() {
        return players;
    }
}
