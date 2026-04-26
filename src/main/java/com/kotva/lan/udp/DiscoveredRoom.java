
package com.kotva.lan.udp;

/**
 * Room information discovered through LAN UDP discovery.
 *
 * @param sessionId LAN session id
 * @param roomName room display name
 * @param hostPlayerName host player name
 * @param hostIp host IP address
 * @param tcpPort TCP port used to join the room
 * @param currentPlayers current player count
 * @param maxPlayers maximum player count
 * @param dictionaryLabel dictionary display label
 * @param timeLabel time setting display label
 * @param lastSeenAtMillis last time this room was seen
 */
public record DiscoveredRoom(
        String sessionId,
        String roomName,
        String hostPlayerName,
        String hostIp,
        int tcpPort,
        int currentPlayers,
        int maxPlayers,
        String dictionaryLabel,
        String timeLabel,
        long lastSeenAtMillis) {

    /**
     * Gets the room name shown to users.
     *
     * @return display room name
     */
    public String displayRoomName() {
        if (roomName == null || roomName.isBlank()) {
            if (hostPlayerName == null || hostPlayerName.isBlank()) {
                return "LAN Room";
            }
            return hostPlayerName + "'s Room";
        }
        return roomName;
    }

    /**
     * Creates a host:port endpoint string.
     *
     * @return endpoint string
     */
    public String createEndpoint() {
        return hostIp + ":" + tcpPort;
    }

    /**
     * Builds a readable room description.
     *
     * @return display text
     */
    public String displayText() {
        return displayRoomName()
                + " | "
                + hostPlayerName
                + " | "
                + currentPlayers + "/" + maxPlayers
                + " | "
                + dictionaryLabel
                + " | "
                + timeLabel;
    }

    /**
     * Builds a unique key for room cache maps.
     *
     * @return unique room key
     */
    public String uniqueKey() {
        return sessionId + "@" + hostIp + ":" + tcpPort;
    }

    /**
     * Creates a copy with a new last-seen timestamp.
     *
     * @param lastSeenAtMillis new last-seen timestamp
     * @return copied room
     */
    public DiscoveredRoom withLastSeenAtMillis(long lastSeenAtMillis) {
        return new DiscoveredRoom(
                sessionId,
                roomName,
                hostPlayerName,
                hostIp,
                tcpPort,
                currentPlayers,
                maxPlayers,
                dictionaryLabel,
                timeLabel,
                lastSeenAtMillis);
    }
}
