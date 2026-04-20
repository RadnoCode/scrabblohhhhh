package com.kotva.lan.message;

import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

public class SnapshotUpdateMessage extends LocalGameMessage {
    private static final long serialVersionUID = 1L;

    private final GameSessionSnapshot snapshot;

    public SnapshotUpdateMessage(GameSessionSnapshot snapshot) {
        super(MessageType.SNAPSHOT_UPDATE);
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    public GameSessionSnapshot getSnapshot() {
        return snapshot;
    }
}
