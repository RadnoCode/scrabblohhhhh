package com.kotva.lan.message;

import com.kotva.lan.LanLobbySnapshot;
import java.util.Objects;

public class LobbyStateMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final String localPlayerId;
    private final LanLobbySnapshot snapshot;

    public LobbyStateMessage(String localPlayerId, LanLobbySnapshot snapshot) {
        super(MessageType.LOBBY_STATE);
        this.localPlayerId = localPlayerId;
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public LanLobbySnapshot getSnapshot() {
        return snapshot;
    }
}
