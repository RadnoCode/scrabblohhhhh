package com.kotva.lan.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class LanHostBroadcaster {
    public static final int BROADCAST_PORT = 5051;
    public static final long BROADCAST_INTERVAL_MILLIS = 1000L;
    private static final Logger logger =
            Logger.getLogger(LanHostBroadcaster.class.getName());

    private final int discoveryPort;
    private final AtomicBoolean running;

    private DatagramSocket socket;
    private Thread workerThread;

    public LanHostBroadcaster() {
        this.discoveryPort = BROADCAST_PORT;
        this.running = new AtomicBoolean(false);
    }

    public void startBroadcasting(Supplier<DiscoveredRoom> roomSupplier) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Broadcaster is already running.");
        }

        socket = new DatagramSocket();
        socket.setBroadcast(true);
        running.set(true);

        workerThread = new Thread(() -> broadcastLoop(roomSupplier),"LAN-HostBroadcasterThread");
        workerThread.setDaemon(true);
        workerThread.start();

    }

    private void broadcastLoop(Supplier<DiscoveredRoom> roomSupplier) {
        try {
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            while (running.get()) {
                DiscoveredRoom room = roomSupplier.get();
                if (room != null) {
                    byte[] payload = LanDiscoveryCodec.encode(room);
                    DatagramPacket packet = new DatagramPacket(
                            payload,
                            payload.length,
                            broadcastAddress,
                            discoveryPort);
                    socket.send(packet);
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

    public void stop() {
        running.set(false);

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }


}
