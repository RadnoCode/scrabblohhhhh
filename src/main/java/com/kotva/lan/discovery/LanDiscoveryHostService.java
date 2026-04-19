package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.util.function.Supplier;

public interface LanDiscoveryHostService {
    void startHosting(Supplier<DiscoveredRoom> roomSupplier) throws IOException;

    void stop();
}
