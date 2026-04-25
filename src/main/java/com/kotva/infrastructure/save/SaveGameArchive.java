package com.kotva.infrastructure.save;

import com.kotva.application.session.GameSession;
import com.kotva.application.session.GameSessionSnapshot;
import java.io.Serializable;
import java.util.Objects;

public final class SaveGameArchive implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int version;
    private final long savedAtEpochMillis;
    private final GameSession session;
    private final GameSessionSnapshot snapshot;

    public SaveGameArchive(
        int version,
        long savedAtEpochMillis,
        GameSession session,
        GameSessionSnapshot snapshot) {
        this.version = version;
        this.savedAtEpochMillis = savedAtEpochMillis;
        this.session = Objects.requireNonNull(session, "session cannot be null.");
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot cannot be null.");
    }

    public int getVersion() {
        return version;
    }

    public long getSavedAtEpochMillis() {
        return savedAtEpochMillis;
    }

    public GameSession getSession() {
        return session;
    }

    public GameSessionSnapshot getSnapshot() {
        return snapshot;
    }
}
