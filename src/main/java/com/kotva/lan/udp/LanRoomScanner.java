package com.kotva.lan.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Listens for UDP discovery broadcasts and maintains a live LAN room list.
 */
public class LanRoomScanner {
    private static final Logger logger =
            Logger.getLogger(LanRoomScanner.class.getName());

    /**
     * If we have not heard from a room for longer than this threshold,
     * we consider it expired and remove it from the room list.
     */
    private static final long ROOM_EXPIRE_MILLIS = 3000L;

    private final int discoveryPort;
    private final AtomicBoolean running;
    private final Map<String, DiscoveredRoom> roomsByKey;

    private DatagramSocket socket;
    private Thread workerThread;

    /**
     * Creates a room scanner.
     *
     * @param discoveryPort UDP discovery port
     */
    public LanRoomScanner(int discoveryPort) {
        this.discoveryPort = discoveryPort;
        this.running = new AtomicBoolean(false);
        this.roomsByKey = new ConcurrentHashMap<>();
    }

    /**
     * Starts scanning in the background.
     *
     * @param onUpdate callback receiving the current room list
     * @throws IOException if the UDP socket cannot be opened
     */
    public void startScanning(Consumer<List<DiscoveredRoom>> onUpdate) throws IOException {
        Objects.requireNonNull(onUpdate, "onUpdate cannot be null.");

        if (running.get()) {
            return;
        }

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.bind(new InetSocketAddress(discoveryPort));
        socket.setSoTimeout(1000);
        running.set(true);

        workerThread = new Thread(
                () -> scanLoop(onUpdate),
                "LAN-RoomScanner");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    /**
     * Stops scanning and closes the socket.
     */
    public void stop() {
        running.set(false);

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Main scan loop that receives packets and updates the room cache.
     *
     * @param onUpdate callback receiving the current room list
     */
    private void scanLoop(Consumer<List<DiscoveredRoom>> onUpdate) {
        byte[] buffer = new byte[1024];

        try {
            while (running.get()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String rawMessage = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8);

                    DiscoveredRoom decodedRoom = LanDiscoveryCodec.decode(rawMessage);
                    if (decodedRoom != null) {
                        DiscoveredRoom normalizedRoom = new DiscoveredRoom(
                                decodedRoom.sessionId(),
                                decodedRoom.roomName(),
                                decodedRoom.hostPlayerName(),
                                resolveAdvertisedHost(packet.getAddress(), decodedRoom.hostIp()),
                                decodedRoom.tcpPort(),
                                decodedRoom.currentPlayers(),
                                decodedRoom.maxPlayers(),
                                decodedRoom.dictionaryLabel(),
                                decodedRoom.timeLabel(),
                                System.currentTimeMillis());

                        roomsByKey.put(normalizedRoom.uniqueKey(), normalizedRoom);
                    }
                } catch (SocketTimeoutException ignored) {
                    // Timeout is expected. It gives us a chance to remove expired rooms.
                }

                removeExpiredRooms();
                onUpdate.accept(snapshotRooms());
            }
        } catch (Exception exception) {
            if (running.get()) {
                logger.warning(
                        "LAN room scanner stopped unexpectedly: "
                                + exception.getMessage());
            }
        } finally {
            stop();
        }
    }

    /**
     * Chooses a usable host address for a received room.
     *
     * @param packetSource address that sent the packet
     * @param payloadHost host address written in the packet
     * @return usable host address string
     */
    private String resolveAdvertisedHost(InetAddress packetSource, String payloadHost) {
        if (isUsableIpv4(packetSource)) {
            return packetSource.getHostAddress();
        }
        InetAddress payloadAddress = parseIpv4(payloadHost);
        if (isUsableIpv4(payloadAddress)) {
            return payloadAddress.getHostAddress();
        }
        return packetSource == null ? "" : packetSource.getHostAddress();
    }

    /**
     * Parses an IPv4 address.
     *
     * @param host host text
     * @return IPv4 address, or {@code null}
     */
    private InetAddress parseIpv4(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(host.trim());
            return address instanceof Inet4Address ? address : null;
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    /**
     * Checks whether an address can be used for LAN connection.
     *
     * @param address address to inspect
     * @return {@code true} if usable
     */
    private boolean isUsableIpv4(InetAddress address) {
        return address instanceof Inet4Address
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress();
    }

    /**
     * Removes rooms that have not broadcast recently.
     */
    private void removeExpiredRooms() {
        long now = System.currentTimeMillis();

        roomsByKey.entrySet().removeIf(entry ->
                now - entry.getValue().lastSeenAtMillis() > ROOM_EXPIRE_MILLIS);
    }

    /**
     * Creates a stable snapshot list for the UI layer.
     *
     * @return discovered room list
     */
    private List<DiscoveredRoom> snapshotRooms() {
        List<DiscoveredRoom> rooms = new ArrayList<>(roomsByKey.values());
        rooms.sort(Comparator.comparing(DiscoveredRoom::hostPlayerName));
        return rooms;
    }
}
