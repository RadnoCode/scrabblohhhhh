package com.kotva.application.service.client;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;
import java.util.Objects;

public class ClientGameContext {
    private final String localPlayerId;
    private final GameConfig config;
    private final String sessionId;
    private GameSessionSnapshot latestSnapshot;
    private long locallyElapsedSinceReceiptMillis;

    public ClientGameContext(
            GameConfig config, GameSessionSnapshot initialSnapshot, String localPlayerId) {
        this.config = Objects.requireNonNull(config, "config cannot be null.");
        this.localPlayerId = Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null.");
        GameSessionSnapshot authoritativeSnapshot =
                Objects.requireNonNull(initialSnapshot, "initialSnapshot cannot be null.");
        this.latestSnapshot = GameSessionSnapshotFactory.withReceivedTimestamp(
                authoritativeSnapshot,
                System.currentTimeMillis());
        this.locallyElapsedSinceReceiptMillis = 0L;
        this.sessionId = initialSnapshot.getSessionId();

        if (this.config.getGameMode() != initialSnapshot.getGameMode()) {
            throw new IllegalArgumentException("Game config does not match initial snapshot mode.");
        }
    }

    public void updateSnapshot(GameSessionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot cannot be null.");
        if (!Objects.equals(sessionId, snapshot.getSessionId())) {
            throw new IllegalArgumentException("Snapshot belongs to a different session.");
        }
        if (config.getGameMode() != snapshot.getGameMode()) {
            throw new IllegalArgumentException("Snapshot game mode does not match client config.");
        }
        this.latestSnapshot =
                GameSessionSnapshotFactory.withReceivedTimestamp(
                        snapshot,
                        System.currentTimeMillis());
        this.locallyElapsedSinceReceiptMillis = 0L;
    }

    public void advanceLocalClock(long elapsedMillis) {
        if (elapsedMillis < 0L) {
            throw new IllegalArgumentException("elapsedMillis cannot be negative.");
        }
        if (elapsedMillis == 0L) {
            return;
        }
        locallyElapsedSinceReceiptMillis += elapsedMillis;
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    public GameConfig getConfig() {
        return config;
    }

    public DictionaryType getDictionaryType() {
        return config.getDictionaryType();
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameSessionSnapshot getLatestSnapshot() {
        return GameSessionSnapshotFactory.withLocalClockPrediction(
                latestSnapshot,
                locallyElapsedSinceReceiptMillis);
    }

    public int getTurnNumber() {
        return latestSnapshot.getTurnNumber();
    }

    public SessionStatus getSessionStatus() {
        return latestSnapshot.getSessionStatus();
    }

    public boolean isLocalPlayerTurn() {
        return Objects.equals(localPlayerId, latestSnapshot.getCurrentPlayerId());
    }
}
