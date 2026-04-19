package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.nio.charset.StandardCharsets;

public final class LanDiscoveryCodec {
    public static final String PROTOCOL_PREFIX = "SCRABBLE_LAN_V2";
    public static final String PROTOCOL_VERSION = "2";
    private static final String TYPE_DISCOVER_REQUEST = "DISCOVER_REQUEST";
    private static final String TYPE_DISCOVER_RESPONSE = "DISCOVER_RESPONSE";
    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = "\\|";

    private LanDiscoveryCodec() {
    }

    public static byte[] encodeRequest(String requestId) {
        String payload = String.join(
                SEPARATOR,
                PROTOCOL_PREFIX,
                TYPE_DISCOVER_REQUEST,
                PROTOCOL_VERSION,
                safe(requestId));
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    public static boolean isDiscoverRequest(String rawMessage) {
        return hasPrefix(rawMessage, TYPE_DISCOVER_REQUEST);
    }

    public static byte[] encodeResponse(DiscoveredRoom room) {
        String payload = String.join(
                SEPARATOR,
                PROTOCOL_PREFIX,
                TYPE_DISCOVER_RESPONSE,
                PROTOCOL_VERSION,
                safe(room.sessionId()),
                safe(room.hostPlayerName()),
                String.valueOf(room.tcpPort()),
                String.valueOf(room.currentPlayers()),
                String.valueOf(room.maxPlayers()),
                safe(room.dictionaryLabel()),
                safe(room.timeLabel()));
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    public static DiscoveredRoom decodeResponse(String rawMessage) {
        if (!hasPrefix(rawMessage, TYPE_DISCOVER_RESPONSE)) {
            return null;
        }

        String[] parts = rawMessage.split(SEPARATOR_REGEX, -1);
        if (parts.length < 10 || !PROTOCOL_VERSION.equals(parts[2])) {
            return null;
        }

        try {
            return new DiscoveredRoom(
                    parts[3],
                    parts[4],
                    "",
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]),
                    parts[8],
                    parts[9],
                    System.currentTimeMillis());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static boolean hasPrefix(String rawMessage, String expectedType) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return false;
        }
        String[] parts = rawMessage.split(SEPARATOR_REGEX, 4);
        return parts.length >= 3
                && PROTOCOL_PREFIX.equals(parts[0])
                && expectedType.equals(parts[1])
                && PROTOCOL_VERSION.equals(parts[2]);
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(SEPARATOR, "/");
    }
}
