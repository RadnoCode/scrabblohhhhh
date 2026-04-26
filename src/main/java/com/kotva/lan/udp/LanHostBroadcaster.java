package com.kotva.lan.udp;

import com.kotva.lan.LanHostAddressResolver;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Periodically broadcasts the host room over UDP.
 */
public class LanHostBroadcaster {
    public static final int BROADCAST_PORT = 5051;
    public static final long BROADCAST_INTERVAL_MILLIS = 1000L;
    private static final Logger logger =
            Logger.getLogger(LanHostBroadcaster.class.getName());

    private final int discoveryPort;
    private final AtomicBoolean running;

    private DatagramSocket socket;
    private DatagramSocket fallbackSocket;
    private List<LanHostAddressResolver.BroadcastEndpoint> broadcastEndpoints;
    private Thread workerThread;

    /**
     * Creates a broadcaster using the default discovery port.
     */
    public LanHostBroadcaster() {
        this.discoveryPort = BROADCAST_PORT;
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts broadcasting room information in a background thread.
     *
     * @param roomSupplier supplies the current room information
     * @throws IOException if the UDP socket cannot be opened
     */
    public void startBroadcasting(Supplier<DiscoveredRoom> roomSupplier) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Broadcaster is already running.");
        }

        broadcastEndpoints = LanHostAddressResolver.resolveBroadcastEndpoints();
        socket = new DatagramSocket();
        if (!broadcastEndpoints.isEmpty()) {
            String targets = broadcastEndpoints.stream()
                    .map(endpoint -> endpoint.localAddress().getHostAddress()
                            + " -> "
                            + endpoint.broadcastAddress().getHostAddress())
                    .collect(java.util.stream.Collectors.joining(", "));
            logger.info(
                    "Broadcasting LAN room announcements on "
                            + broadcastEndpoints.size()
                            + " subnet broadcast interface(s): "
                            + targets);
        } else {
            logger.warning("No preferred LAN subnet broadcast interface was found. Falling back to 255.255.255.255 only.");
        }
        socket.setBroadcast(true);
        if (broadcastEndpoints.isEmpty()) {
            fallbackSocket = new DatagramSocket();
            fallbackSocket.setBroadcast(true);
        }
        running.set(true);

        workerThread = new Thread(() -> broadcastLoop(roomSupplier),"LAN-HostBroadcasterThread");
        workerThread.setDaemon(true);
        workerThread.start();

    }

    /**
     * Background loop that sends room packets repeatedly.
     *
     * @param roomSupplier supplies the current room information
     */
    private void broadcastLoop(Supplier<DiscoveredRoom> roomSupplier) {
        try {
            while (running.get()) {
                DiscoveredRoom room = roomSupplier.get();
                if (room != null) {
                    for (LanHostAddressResolver.BroadcastEndpoint broadcastEndpoint : broadcastEndpoints) {
                        DiscoveredRoom interfaceRoom =
                                withHostIp(room, broadcastEndpoint.localAddress().getHostAddress());
                        byte[] payload = LanDiscoveryCodec.encode(interfaceRoom);
                        DatagramPacket packet = new DatagramPacket(
                                payload,
                                payload.length,
                                broadcastEndpoint.broadcastAddress(),
                                discoveryPort);
                        socket.send(packet);
                    }

                    if (fallbackSocket != null) {
                        byte[] fallbackPayload = LanDiscoveryCodec.encode(room);
                        DatagramPacket fallbackPacket = new DatagramPacket(
                                fallbackPayload,
                                fallbackPayload.length,
                                InetAddress.getByName("255.255.255.255"),
                                discoveryPort);
                        fallbackSocket.send(fallbackPacket);
                    }
                }

                Thread.sleep(BROADCAST_INTERVAL_MILLIS);
            }
        } catch (Exception exception) {
            if (running.get()) {
                logger.warning("LAN host broadcaster stopped unexpectedly: "
                        + exception.getMessage());
            }
        } finally {
            stop();
        }
    }

    /**
     * Stops broadcasting and closes sockets.
     */
    public void stop() {
        running.set(false);

        if (workerThread != null) {
            workerThread.interrupt();
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (fallbackSocket != null && !fallbackSocket.isClosed()) {
            fallbackSocket.close();
        }
        broadcastEndpoints = List.of();
    }

    /**
     * Creates a room copy using an interface-specific host IP.
     *
     * @param room source room
     * @param hostIp host IP for this packet
     * @return room copy
     */
    private DiscoveredRoom withHostIp(DiscoveredRoom room, String hostIp) {
        return new DiscoveredRoom(
                room.sessionId(),
                room.roomName(),
                room.hostPlayerName(),
                hostIp,
                room.tcpPort(),
                room.currentPlayers(),
                room.maxPlayers(),
                room.dictionaryLabel(),
                room.timeLabel(),
                room.lastSeenAtMillis());
    }

}
