package com.kotva.lan.message;

import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

/**
 * Message sent by the host with an updated game snapshot.
 */
public class SnapshotUpdateMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final GameSessionSnapshot snapshot;

    /**
     * Creates a snapshot update message.
     *
     * @param snapshot latest game snapshot
     */
    public SnapshotUpdateMessage(GameSessionSnapshot snapshot) {
        super(MessageType.SNAPSHOT_UPDATE);
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    /**
     * Gets the game snapshot.
     *
     * @return game snapshot
     */
    public GameSessionSnapshot getSnapshot() {
        return snapshot;
    }
}
