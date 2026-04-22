package com.kotva.lan.discovery;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.kotva.lan.udp.DiscoveredRoom;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class LanDiscoveryCodecTest {
    @Test
    public void requestEncodingIncludesProtocolPrefixAndVersion() {
        byte[] payload = LanDiscoveryCodec.encodeRequest("req-123");

        assertArrayEquals(
                "SCRABBLE_LAN_V2|DISCOVER_REQUEST|2|req-123".getBytes(StandardCharsets.UTF_8),
                payload);
    }

    @Test
    public void responseRoundTripPreservesRoomMetadata() {
        DiscoveredRoom room = new DiscoveredRoom(
                "session-1",
                "Friday Room",
                "Host",
                "",
                5050,
                2,
                4,
                "British",
                "15min",
                1L);

        DiscoveredRoom decodedRoom = LanDiscoveryCodec.decodeResponse(
                new String(LanDiscoveryCodec.encodeResponse(room), StandardCharsets.UTF_8));

        assertNotNull(decodedRoom);
        assertEquals("session-1", decodedRoom.sessionId());
        assertEquals("Friday Room", decodedRoom.roomName());
        assertEquals("Host", decodedRoom.hostPlayerName());
        assertEquals(5050, decodedRoom.tcpPort());
        assertEquals(2, decodedRoom.currentPlayers());
        assertEquals(4, decodedRoom.maxPlayers());
        assertEquals("British", decodedRoom.dictionaryLabel());
        assertEquals("15min", decodedRoom.timeLabel());
    }

    @Test
    public void decodeResponseRejectsWrongVersionOrBrokenPayload() {
        assertNull(LanDiscoveryCodec.decodeResponse("SCRABBLE_LAN_V2|DISCOVER_RESPONSE|1|broken"));
        assertNull(LanDiscoveryCodec.decodeResponse("SCRABBLE_LAN_V2|DISCOVER_RESPONSE|2|x|y|abc"));
        assertNull(LanDiscoveryCodec.decodeResponse("not-a-protocol"));
    }
}
