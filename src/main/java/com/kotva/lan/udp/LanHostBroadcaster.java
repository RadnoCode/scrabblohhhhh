package com.kotva.lan.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
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
            while (running.get()) {
                DiscoveredRoom room = roomSupplier.get();
                if (room != null) {
                    byte[] payload = LanDiscoveryCodec.encode(room);
                    for (InetAddress broadcastAddress : resolveBroadcastAddresses()) {
                        DatagramPacket packet = new DatagramPacket(
                                payload,
                                payload.length,
                                broadcastAddress,
                                discoveryPort);
                        socket.send(packet);
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

    public void stop() {
        running.set(false);

        if (workerThread != null) {
            workerThread.interrupt();
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private Set<InetAddress> resolveBroadcastAddresses() throws SocketException, IOException {
        Set<InetAddress> targets = new LinkedHashSet<>();
        for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isPointToPoint()) {
                continue;
            }
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                InetAddress address = interfaceAddress.getAddress();
                InetAddress broadcast = interfaceAddress.getBroadcast();
                if (address instanceof Inet4Address && broadcast instanceof Inet4Address) {
                    targets.add(broadcast);
                }
            }
        }
        targets.add(InetAddress.getByName("255.255.255.255"));
        return targets;
    }


}
