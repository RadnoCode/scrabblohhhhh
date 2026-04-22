
package com.kotva.lan.udp;

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

    public String displayRoomName() {
        if (roomName == null || roomName.isBlank()) {
            if (hostPlayerName == null || hostPlayerName.isBlank()) {
                return "LAN Room";
            }
            return hostPlayerName + "'s Room";
        }
        return roomName;
    }

    public String createEndpoint() {
        return hostIp + ":" + tcpPort;
    }

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

    public String uniqueKey() {
        return sessionId + "@" + hostIp + ":" + tcpPort;
    }

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
