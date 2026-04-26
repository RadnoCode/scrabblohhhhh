package com.kotva.lan.message;

import java.util.Objects;

/**
 * Message sent by the host when a player disconnects.
 */
public class PlayerDisconnectedMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String playerId;
    private final String playerName;
    private final String reason;

    /**
     * Creates a player disconnected message.
     *
     * @param playerId disconnected player id
     * @param playerName disconnected player name
     * @param reason disconnect reason
     */
    public PlayerDisconnectedMessage(String playerId, String playerName, String reason) {
        super(MessageType.PLAYER_DISCONNECTED);
        this.playerId = playerId;
        this.playerName = playerName;
        this.reason = Objects.requireNonNullElse(reason, "");
    }

    /**
     * Gets the disconnected player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the disconnected player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets the disconnect reason.
     *
     * @return reason
     */
    public String getReason() {
        return reason;
    }
}
