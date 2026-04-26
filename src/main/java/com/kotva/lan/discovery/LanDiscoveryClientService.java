package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client-side LAN discovery service.
 */
public interface LanDiscoveryClientService {
    /**
     * Starts scanning for LAN rooms.
     *
     * @param onUpdate callback receiving discovered rooms
     * @throws IOException if scanning cannot start
     */
    void startScanning(Consumer<List<DiscoveredRoom>> onUpdate) throws IOException;

    /**
     * Stops scanning.
     */
    void stop();
}
