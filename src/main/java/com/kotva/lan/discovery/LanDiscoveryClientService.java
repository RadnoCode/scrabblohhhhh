package com.kotva.lan.discovery;

import com.kotva.lan.udp.DiscoveredRoom;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface LanDiscoveryClientService {
    void startScanning(Consumer<List<DiscoveredRoom>> onUpdate) throws IOException;

    void stop();
}
