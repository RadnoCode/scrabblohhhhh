package com.kotva.lan.message;

public class PlayerJoinedMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;
    // sent by server to all clients in a session (except the new player themselves) when a new player joins the session. Contains the new player's id and name.
    private final String playerId;
    private final String playerName;

    public PlayerJoinedMessage(String playerId, String playerName) {
        super(MessageType.PLAYER_JOINED);
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