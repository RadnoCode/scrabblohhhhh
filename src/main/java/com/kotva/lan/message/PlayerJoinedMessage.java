package com.kotva.lan.message;

/**
 * Message sent by the host when a player joins the session.
 */
public class PlayerJoinedMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;
    private final String playerId;
    private final String playerName;

    /**
     * Creates a player joined message.
     *
     * @param playerId joined player id
     * @param playerName joined player name
     */
    public PlayerJoinedMessage(String playerId, String playerName) {
        super(MessageType.PLAYER_JOINED);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    /**
     * Gets the joined player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the joined player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }
}
