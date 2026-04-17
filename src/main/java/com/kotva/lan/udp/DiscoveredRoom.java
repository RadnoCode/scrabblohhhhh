
package com.kotva.lan.udp;

public record DiscoveredRoom(
        String sessionId,
        String hostPlayerName,
        String hostIp,
        int tcpPort,
        int currentPlayers,
        int maxPlayers,
        String dictionaryLabel,
        String timeLabel,
        long lastSeenAtMillis) {

    public String createEndpoint() {
        return hostIp + ":" + tcpPort;
    }

    public String displayText() {
        return hostPlayerName
                + " | "
                + currentPlayers + "/" + maxPlayers
                + " | "
                + dictionaryLabel
                + " | "
                + timeLabel
                + " | "
                + createEndpoint();
    }

    public String uniqueKey() {
        return sessionId + "@" + hostIp + ":" + tcpPort;
    }

    public DiscoveredRoom withLastSeenAtMillis(long lastSeenAtMillis) {
        return new DiscoveredRoom(
                sessionId,
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