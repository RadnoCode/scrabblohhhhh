package com.kotva.lan.message;

import java.util.Objects;

public class PlayerDisconnectedMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String playerId;
    private final String playerName;
    private final String reason;

    public PlayerDisconnectedMessage(String playerId, String playerName, String reason) {
        super(MessageType.PLAYER_DISCONNECTED);
        this.playerId = playerId;
        this.playerName = playerName;
        this.reason = Objects.requireNonNullElse(reason, "");
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getReason() {
        return reason;
    }
}
