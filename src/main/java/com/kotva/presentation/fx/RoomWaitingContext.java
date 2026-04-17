package com.kotva.presentation.fx;

import com.kotva.lan.GameSessionBroker;
import com.kotva.lan.LanLobbyClientSession;
import com.kotva.lan.udp.LanHostBroadcaster;
import java.util.Objects;

/**
 * Shared navigation state for the LAN waiting room.
 */
public final class RoomWaitingContext {
    public enum Mode {
        HOST,
        CLIENT
    }

    private final Mode mode;
    private final String roomTitle;
    private final String gameTimeLabel;
    private final String languageLabel;
    private final String playerCountLabel;
    private final GameSessionBroker hostBroker;
    private final LanHostBroadcaster hostBroadcaster;
    private final LanLobbyClientSession clientSession;

    private RoomWaitingContext(
            Mode mode,
            String roomTitle,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel,
            GameSessionBroker hostBroker,
            LanHostBroadcaster hostBroadcaster,
            LanLobbyClientSession clientSession) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null.");
        this.roomTitle = Objects.requireNonNull(roomTitle, "roomTitle cannot be null.");
        this.gameTimeLabel = Objects.requireNonNull(gameTimeLabel, "gameTimeLabel cannot be null.");
        this.languageLabel = Objects.requireNonNull(languageLabel, "languageLabel cannot be null.");
        this.playerCountLabel = Objects.requireNonNull(playerCountLabel, "playerCountLabel cannot be null.");
        this.hostBroker = hostBroker;
        this.hostBroadcaster = hostBroadcaster;
        this.clientSession = clientSession;
    }

    public static RoomWaitingContext forHost(
            String roomTitle,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel,
            GameSessionBroker hostBroker,
            LanHostBroadcaster hostBroadcaster) {
        return new RoomWaitingContext(
                Mode.HOST,
                roomTitle,
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                Objects.requireNonNull(hostBroker, "hostBroker cannot be null."),
                Objects.requireNonNull(hostBroadcaster, "hostBroadcaster cannot be null."),
                null);
    }

    public static RoomWaitingContext forClient(
            String roomTitle,
            String gameTimeLabel,
            String languageLabel,
            String playerCountLabel,
            LanLobbyClientSession clientSession) {
        return new RoomWaitingContext(
                Mode.CLIENT,
                roomTitle,
                gameTimeLabel,
                languageLabel,
                playerCountLabel,
                null,
                null,
                Objects.requireNonNull(clientSession, "clientSession cannot be null."));
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isHost() {
        return mode == Mode.HOST;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public String getGameTimeLabel() {
        return gameTimeLabel;
    }

    public String getLanguageLabel() {
        return languageLabel;
    }

    public String getPlayerCountLabel() {
        return playerCountLabel;
    }

    public GameSessionBroker requireHostBroker() {
        return Objects.requireNonNull(hostBroker, "hostBroker cannot be null.");
    }

    public LanHostBroadcaster getHostBroadcaster() {
        return hostBroadcaster;
    }

    public LanLobbyClientSession requireClientSession() {
        return Objects.requireNonNull(clientSession, "clientSession cannot be null.");
    }

    public void closeForExit() {
        if (hostBroadcaster != null) {
            hostBroadcaster.stop();
        }
        if (hostBroker != null) {
            hostBroker.stopServer();
        }
        if (clientSession != null) {
            clientSession.disconnect();
        }
    }
}
