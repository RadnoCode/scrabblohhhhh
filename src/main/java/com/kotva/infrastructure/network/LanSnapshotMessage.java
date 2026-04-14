package com.kotva.infrastructure.network;

import com.kotva.application.session.GameSessionSnapshot;
import java.util.Objects;

public record LanSnapshotMessage(GameSessionSnapshot snapshot) implements LanInboundMessage {
    public LanSnapshotMessage {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }
}
