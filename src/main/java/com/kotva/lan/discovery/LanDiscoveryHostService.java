package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Host-side LAN discovery service.
 */
public interface LanDiscoveryHostService {
    /**
     * Starts answering LAN room discovery requests.
     *
     * @param roomSupplier supplies current room information
     * @throws IOException if hosting cannot start
     */
    void startHosting(Supplier<DiscoveredRoom> roomSupplier) throws IOException;

    /**
     * Stops the discovery service.
     */
    void stop();
}
