package com.kotva.lan.message;

public class JoinSessionMessage extends LocalGameMessage {
    // joining request sent by client to server when a player tries to join a session. Contains the player's chosen name.
    private static final long serialVersionUID = 1L;
    
    private final String playerId;
    private final String playerName;

    public JoinSessionMessage(String playerId, String playerName) {
        super(MessageType.JOIN_SESSION);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
