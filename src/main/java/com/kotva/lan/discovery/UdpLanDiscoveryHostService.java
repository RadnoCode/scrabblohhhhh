package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class UdpLanDiscoveryHostService implements LanDiscoveryHostService {
    public static final int DISCOVERY_PORT = 5051;
    private static final int SOCKET_TIMEOUT_MILLIS = 1_000;

    private static final Logger logger =
            Logger.getLogger(UdpLanDiscoveryHostService.class.getName());

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final int discoveryPort;

    private DatagramSocket socket;
    private Thread workerThread;

    public UdpLanDiscoveryHostService() {
        this(DISCOVERY_PORT);
    }

    UdpLanDiscoveryHostService(int discoveryPort) {
        this.discoveryPort = discoveryPort;
    }

    @Override
    public void startHosting(Supplier<DiscoveredRoom> roomSupplier) throws IOException {
        Objects.requireNonNull(roomSupplier, "roomSupplier cannot be null.");
        if (running.get()) {
            throw new IllegalStateException("LAN discovery host service is already running.");
        }

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.setBroadcast(true);
        socket.bind(new InetSocketAddress(discoveryPort));
        socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
        running.set(true);

        workerThread = new Thread(() -> hostLoop(roomSupplier), "LAN-DiscoveryHost");
        workerThread.setDaemon(true);
        workerThread.start();
        logger.info("LAN discovery host is listening on UDP port " + discoveryPort + ".");
    }

    private void hostLoop(Supplier<DiscoveredRoom> roomSupplier) {
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
                    if (!LanDiscoveryCodec.isDiscoverRequest(rawMessage)) {
                        continue;
                    }

                    DiscoveredRoom room = roomSupplier.get();
                    if (room == null) {
                        continue;
                    }

                    byte[] responsePayload = LanDiscoveryCodec.encodeResponse(room);
                    DatagramPacket responsePacket = new DatagramPacket(
                            responsePayload,
                            responsePayload.length,
                            packet.getAddress(),
                            packet.getPort());
                    socket.send(responsePacket);
                } catch (SocketTimeoutException ignored) {
                    // Allows stop() to terminate without blocking forever.
                }
            }
        } catch (IOException exception) {
            if (running.get()) {
                logger.warning("LAN discovery host stopped unexpectedly: " + exception.getMessage());
            }
        } finally {
            stop();
        }
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
    }
}
