package com.kotva.lan.udp;

import java.nio.charset.StandardCharsets;

/**
 * Encodes and decodes the older UDP LAN discovery packet format.
 */
public final class LanDiscoveryCodec {
    public static final String PROTOCOL_PREFIX = "SCRABBLE_LAN_V1";
    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = "\\|";

    /**
     * Prevents creating this utility class.
     */
    private LanDiscoveryCodec() {
    }

    /**
     * Encodes room information into a UDP packet payload.
     *
     * @param room room to encode
     * @return UTF-8 packet bytes
     */
    public static byte[] encode(DiscoveredRoom room) {
        String payload = String.join(
                SEPARATOR,
                PROTOCOL_PREFIX,
                safe(room.sessionId()),
                safe(room.roomName()),
                safe(room.hostPlayerName()),
                safe(room.hostIp()),
                String.valueOf(room.tcpPort()),
                String.valueOf(room.currentPlayers()),
                String.valueOf(room.maxPlayers()),
                safe(room.dictionaryLabel()),
                safe(room.timeLabel()));

        return payload.getBytes(StandardCharsets.UTF_8);
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

    /**
     * Checks whether a packet belongs to this discovery protocol.
     *
     * @param rawMessage raw packet text
     * @return {@code true} if it is a discovery packet
     */
    public static boolean isDiscoveryPacket(String rawMessage) {
        return rawMessage != null && rawMessage.startsWith(PROTOCOL_PREFIX + SEPARATOR);
    }

    /**
     * Decodes a discovery packet into room information.
     *
     * @param rawMessage raw packet text
     * @return discovered room, or {@code null} if invalid
     */
    public static DiscoveredRoom decode(String rawMessage) {
        if (!isDiscoveryPacket(rawMessage)) {
            return null;
        }

        String[] parts = rawMessage.split(SEPARATOR_REGEX, -1);
        if (parts.length < 9) {
            return null;
        }

        try {
            return new DiscoveredRoom(
                    parts[1], // sessionId
                    parts.length >= 10 ? parts[2] : parts[2], // roomName
                    parts.length >= 10 ? parts[3] : parts[2], // hostPlayerName
                    parts.length >= 10 ? parts[4] : parts[3], // hostIp
                    Integer.parseInt(parts.length >= 10 ? parts[5] : parts[4]), // tcpPort
                    Integer.parseInt(parts.length >= 10 ? parts[6] : parts[5]), // currentPlayers
                    Integer.parseInt(parts.length >= 10 ? parts[7] : parts[6]), // maxPlayers
                    parts.length >= 10 ? parts[8] : parts[7], // dictionaryLabel
                    parts.length >= 10 ? parts[9] : parts[8], // timeLabel
                    System.currentTimeMillis());
        } catch (NumberFormatException exception) {
            // If numeric fields are broken, the packet is invalid.
            return null;
        }
    }




}
