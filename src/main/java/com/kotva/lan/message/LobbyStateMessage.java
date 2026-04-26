package com.kotva.lan.message;

import com.kotva.lan.LanLobbySnapshot;
import java.util.Objects;

/**
 * Message sent by the host to update a client's lobby state.
 */
public class LobbyStateMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String localPlayerId;
    private final LanLobbySnapshot snapshot;

    /**
     * Creates a lobby state message.
     *
     * @param localPlayerId player id assigned to the receiving client
     * @param snapshot latest lobby snapshot
     */
    public LobbyStateMessage(String localPlayerId, LanLobbySnapshot snapshot) {
        super(MessageType.LOBBY_STATE);
        this.localPlayerId = localPlayerId;
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    /**
     * Gets the local player id.
     *
     * @return local player id
     */
    public String getLocalPlayerId() {
        return localPlayerId;
    }

    /**
     * Gets the lobby snapshot.
     *
     * @return lobby snapshot
     */
    public LanLobbySnapshot getSnapshot() {
        return snapshot;
    }
}
