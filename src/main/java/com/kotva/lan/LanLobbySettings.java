package com.kotva.lan;

import com.kotva.application.session.TimeControlConfig;
import com.kotva.policy.DictionaryType;
import java.io.Serializable;
import java.util.Objects;

public class LanLobbySettings implements Serializable {
    private final String roomName;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final int maxPlayers;

    public LanLobbySettings(
            String roomName,
            DictionaryType dictionaryType,
            TimeControlConfig timeControlConfig,
            int maxPlayers) {
        this.roomName = normalizeRoomName(roomName);
        this.dictionaryType = Objects.requireNonNull(
                dictionaryType,
                "dictionaryType cannot be null.");
        if (maxPlayers < 2 || maxPlayers > 4) {
            throw new IllegalArgumentException("maxPlayers must be between 2 and 4.");
        }
        this.timeControlConfig = timeControlConfig;
        this.maxPlayers = maxPlayers;
    }

    public String getRoomName() {
        return roomName;
    }

    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    private static String normalizeRoomName(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            return "LAN Room";
        }
        return roomName.trim();
    }
}
