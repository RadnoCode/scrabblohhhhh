package com.kotva.lan.message;

/**
 * Message sent by a client when joining a LAN session.
 */
public class JoinSessionMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String playerId;
    private final String playerName;

    /**
     * Creates a join session message.
     *
     * @param playerId requested player id
     * @param playerName requested player name
     */
    public JoinSessionMessage(String playerId, String playerName) {
        super(MessageType.JOIN_SESSION);
        this.playerId = playerId;
        this.playerName = playerName;
    }

    /**
     * Gets the requested player id.
     *
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Gets the requested player name.
     *
     * @return player name
     */
    public String getPlayerName() {
        return playerName;
    }
}
