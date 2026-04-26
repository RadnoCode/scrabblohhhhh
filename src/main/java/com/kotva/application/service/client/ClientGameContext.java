package com.kotva.application.service.client;

import com.kotva.application.session.GameConfig;
import com.kotva.application.session.GameSessionSnapshot;
import com.kotva.application.session.GameSessionSnapshotFactory;
import com.kotva.policy.DictionaryType;
import com.kotva.policy.SessionStatus;
import java.util.Objects;

/**
 * Holds the local client's latest game snapshot and identity.
 */
public class ClientGameContext {
    private final String localPlayerId;
    private final GameConfig config;
    private final String sessionId;
    private GameSessionSnapshot latestSnapshot;
    private long locallyElapsedSinceReceiptMillis;

    /**
     * Creates a client game context.
     *
     * @param config local game config
     * @param initialSnapshot first snapshot received from the host
     * @param localPlayerId id of the local player
     */
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

    /**
     * Replaces the latest authoritative snapshot.
     *
     * @param snapshot new snapshot from the host
     */
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

    /**
     * Advances local elapsed time used for clock prediction.
     *
     * @param elapsedMillis elapsed time in milliseconds
     */
    public void advanceLocalClock(long elapsedMillis) {
        if (elapsedMillis < 0L) {
            throw new IllegalArgumentException("elapsedMillis cannot be negative.");
        }
        if (elapsedMillis == 0L) {
            return;
        }
        locallyElapsedSinceReceiptMillis += elapsedMillis;
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
     * Gets the local game config.
     *
     * @return game config
     */
    public GameConfig getConfig() {
        return config;
    }

    /**
     * Gets the selected dictionary.
     *
     * @return dictionary type
     */
    public DictionaryType getDictionaryType() {
        return config.getDictionaryType();
    }

    /**
     * Gets the session id.
     *
     * @return session id
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the latest snapshot with local clock prediction applied.
     *
     * @return latest snapshot
     */
    public GameSessionSnapshot getLatestSnapshot() {
        return GameSessionSnapshotFactory.withLocalClockPrediction(
                latestSnapshot,
                locallyElapsedSinceReceiptMillis);
    }

    /**
     * Gets the latest authoritative turn number.
     *
     * @return turn number
     */
    public int getTurnNumber() {
        return latestSnapshot.getTurnNumber();
    }

    /**
     * Gets the latest session status.
     *
     * @return session status
     */
    public SessionStatus getSessionStatus() {
        return latestSnapshot.getSessionStatus();
    }

    /**
     * Checks whether the local player owns the current turn.
     *
     * @return {@code true} if local player's turn
     */
    public boolean isLocalPlayerTurn() {
        return Objects.equals(localPlayerId, latestSnapshot.getCurrentPlayerId());
    }
}
