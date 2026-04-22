package com.kotva.lan.discovery;

import com.kotva.lan.LanHostAddressResolver;
import com.kotva.lan.LanHostAddressResolver.BroadcastEndpoint;
import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class UdpLanDiscoveryClientService implements LanDiscoveryClientService {
    private static final Logger logger =
            Logger.getLogger(UdpLanDiscoveryClientService.class.getName());

    private static final int SOCKET_TIMEOUT_MILLIS = 250;
    private static final long DISCOVER_INTERVAL_MILLIS = 1_000L;
    private static final long ROOM_EXPIRE_MILLIS = DISCOVER_INTERVAL_MILLIS * 3L;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<String, DiscoveredRoom> roomsByKey = new ConcurrentHashMap<>();
    private final int discoveryPort;

    private DatagramSocket socket;
    private List<BroadcastEndpoint> broadcastEndpoints;
    private Thread workerThread;

    public UdpLanDiscoveryClientService() {
        this(UdpLanDiscoveryHostService.DISCOVERY_PORT);
    }

    UdpLanDiscoveryClientService(int discoveryPort) {
        this.discoveryPort = discoveryPort;
    }

    @Override
    public void startScanning(Consumer<List<DiscoveredRoom>> onUpdate) throws IOException {
        Objects.requireNonNull(onUpdate, "onUpdate cannot be null.");
        if (running.get()) {
            return;
        }

        socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        broadcastEndpoints = List.copyOf(LanHostAddressResolver.resolveBroadcastEndpoints());
        logBroadcastPlan();
        running.set(true);

        workerThread = new Thread(() -> scanLoop(onUpdate), "LAN-DiscoveryClient");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void scanLoop(Consumer<List<DiscoveredRoom>> onUpdate) {
        byte[] buffer = new byte[1024];
        long nextDiscoverAtMillis = 0L;

        try {
            while (running.get()) {
                long now = System.currentTimeMillis();
                if (now >= nextDiscoverAtMillis) {
                    sendDiscoverRequests();
                    nextDiscoverAtMillis = now + DISCOVER_INTERVAL_MILLIS;
                }

                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String rawMessage = new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8);
                    DiscoveredRoom decodedRoom = LanDiscoveryCodec.decodeResponse(rawMessage);
                    if (decodedRoom != null) {
                        DiscoveredRoom resolvedRoom = new DiscoveredRoom(
                                decodedRoom.sessionId(),
                                decodedRoom.roomName(),
                                decodedRoom.hostPlayerName(),
                                packet.getAddress().getHostAddress(),
                                decodedRoom.tcpPort(),
                                decodedRoom.currentPlayers(),
                                decodedRoom.maxPlayers(),
                                decodedRoom.dictionaryLabel(),
                                decodedRoom.timeLabel(),
                                System.currentTimeMillis());
                        roomsByKey.put(resolvedRoom.uniqueKey(), resolvedRoom);
                    }
                } catch (SocketTimeoutException ignored) {
                    // Expected so we can refresh state between probe rounds.
                }

                removeExpiredRooms();
                onUpdate.accept(snapshotRooms());
            }
        } catch (IOException exception) {
            if (running.get()) {
                logger.warning("LAN discovery client stopped unexpectedly: " + exception.getMessage());
            }
        } finally {
            stop();
        }
    }

    private void sendDiscoverRequests() throws IOException {
        byte[] payload = LanDiscoveryCodec.encodeRequest(UUID.randomUUID().toString());
        for (InetAddress broadcastAddress : resolveBroadcastTargets()) {
            DatagramPacket packet = new DatagramPacket(
                    payload,
                    payload.length,
                    broadcastAddress,
                    discoveryPort);
            socket.send(packet);
        }
    }

    private List<InetAddress> resolveBroadcastTargets() throws IOException {
        if (!broadcastEndpoints.isEmpty()) {
            List<InetAddress> targets = new ArrayList<>(broadcastEndpoints.size());
            for (BroadcastEndpoint endpoint : broadcastEndpoints) {
                targets.add(endpoint.broadcastAddress());
            }
            return targets;
        }

        List<InetAddress> targets = new ArrayList<>(1);
        targets.add(InetAddress.getByName("255.255.255.255"));
        return targets;
    }

    private void removeExpiredRooms() {
        long now = System.currentTimeMillis();
        roomsByKey.entrySet().removeIf(entry -> now - entry.getValue().lastSeenAtMillis() > ROOM_EXPIRE_MILLIS);
    }

    private List<DiscoveredRoom> snapshotRooms() {
        List<DiscoveredRoom> rooms = new ArrayList<>(roomsByKey.values());
        rooms.sort(
                Comparator.comparing(
                                DiscoveredRoom::displayRoomName,
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(DiscoveredRoom::hostPlayerName, String.CASE_INSENSITIVE_ORDER));
        return rooms;
    }

    @Override
    public void stop() {
        running.set(false);
        if (workerThread != null) {
            workerThread.interrupt();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        broadcastEndpoints = List.of();
    }

    private void logBroadcastPlan() {
        if (!broadcastEndpoints.isEmpty()) {
            String targets = broadcastEndpoints.stream()
                    .map(endpoint -> endpoint.localAddress().getHostAddress()
                            + " -> "
                            + endpoint.broadcastAddress().getHostAddress())
                    .collect(java.util.stream.Collectors.joining(", "));
            logger.info(
                    "LAN discovery client using subnet broadcast target(s): "
                            + targets);
            return;
        }
        logger.warning(
                "No subnet broadcast address was found. Falling back to limited broadcast 255.255.255.255.");
    }
}
