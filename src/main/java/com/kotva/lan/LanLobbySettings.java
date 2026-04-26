package com.kotva.lan;

import com.kotva.application.session.TimeControlConfig;
import com.kotva.policy.DictionaryType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Settings chosen by the LAN lobby host.
 */
public class LanLobbySettings implements Serializable {
    private final String roomName;
    private final DictionaryType dictionaryType;
    private final TimeControlConfig timeControlConfig;
    private final int maxPlayers;

    /**
     * Creates LAN lobby settings.
     *
     * @param roomName room display name
     * @param dictionaryType dictionary used by the game
     * @param timeControlConfig time settings
     * @param maxPlayers maximum number of players
     */
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

    /**
     * Gets the room name.
     *
     * @return room name
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Gets the selected dictionary.
     *
     * @return dictionary type
     */
    public DictionaryType getDictionaryType() {
        return dictionaryType;
    }

    /**
     * Gets the time control config.
     *
     * @return time control config
     */
    public TimeControlConfig getTimeControlConfig() {
        return timeControlConfig;
    }

    /**
     * Gets the maximum player count.
     *
     * @return max players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Trims a room name and provides a default name when it is blank.
     *
     * @param roomName room name from user input
     * @return normalized room name
     */
    private static String normalizeRoomName(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            return "LAN Room";
        }
        return roomName.trim();
    }
}
