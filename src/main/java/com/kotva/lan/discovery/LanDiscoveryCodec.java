package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.nio.charset.StandardCharsets;

/**
 * Encodes and decodes the request-response UDP discovery protocol.
 */
public final class LanDiscoveryCodec {
    public static final String PROTOCOL_PREFIX = "SCRABBLE_LAN_V2";
    public static final String PROTOCOL_VERSION = "2";
    private static final String TYPE_DISCOVER_REQUEST = "DISCOVER_REQUEST";
    private static final String TYPE_DISCOVER_RESPONSE = "DISCOVER_RESPONSE";
    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = "\\|";

    /**
     * Prevents creating this utility class.
     */
    private LanDiscoveryCodec() {
    }

    /**
     * Encodes a discovery request.
     *
     * @param requestId unique request id
     * @return UTF-8 packet bytes
     */
    public static byte[] encodeRequest(String requestId) {
        String payload = String.join(
                SEPARATOR,
                PROTOCOL_PREFIX,
                TYPE_DISCOVER_REQUEST,
                PROTOCOL_VERSION,
                safe(requestId));
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Checks whether a raw packet is a discovery request.
     *
     * @param rawMessage raw packet text
     * @return {@code true} if discovery request
     */
    public static boolean isDiscoverRequest(String rawMessage) {
        return hasPrefix(rawMessage, TYPE_DISCOVER_REQUEST);
    }

    /**
     * Encodes a discovery response.
     *
     * @param room room information
     * @return UTF-8 packet bytes
     */
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
                safe(room.timeLabel()),
                safe(room.roomName()));
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Decodes a discovery response.
     *
     * @param rawMessage raw packet text
     * @return discovered room, or {@code null}
     */
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
                    parts.length >= 11 ? parts[10] : parts[4],
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

    /**
     * Checks the protocol prefix, type, and version.
     *
     * @param rawMessage raw packet text
     * @param expectedType expected packet type
     * @return {@code true} if header matches
     */
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

    /**
     * Escapes null and separator characters.
     *
     * @param value value to escape
     * @return safe value
     */
    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(SEPARATOR, "/");
    }
}
