package com.kotva.lan;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class LanHostAddressResolver {
    private static final Logger logger =
            Logger.getLogger(LanHostAddressResolver.class.getName());

    private LanHostAddressResolver() {
    }

    public static String resolveJoinEndpoint(int port) {
        InetAddress address = resolvePreferredIpv4Address();
        if (address == null) {
            return "";
        }
        return address.getHostAddress() + ":" + port;
    }

    public static BroadcastEndpoint resolvePreferredBroadcastEndpoint() {
        List<BroadcastEndpoint> endpoints = resolveBroadcastEndpoints();
        if (endpoints.isEmpty()) {
            return null;
        }
        return endpoints.get(0);
    }

    public static List<BroadcastEndpoint> resolveBroadcastEndpoints() {
        List<BroadcastEndpoint> endpoints = new ArrayList<>();
        for (InterfaceCandidate candidate : resolveIpv4Candidates()) {
            endpoints.add(new BroadcastEndpoint(candidate.address(), candidate.broadcast()));
        }
        return endpoints;
    }

    public static Set<InetAddress> resolvePreferredBroadcastAddresses() {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        for (BroadcastEndpoint endpoint : resolveBroadcastEndpoints()) {
            addresses.add(endpoint.broadcastAddress());
        }
        return addresses;
    }

    public static InetAddress resolvePreferredIpv4Address() {
        List<InterfaceCandidate> candidates = resolveIpv4Candidates();
        if (!candidates.isEmpty()) {
            return candidates.get(0).address();
        }

        InetAddress routedAddress = resolveRoutedIpv4Address();
        if (isUsableIpv4Address(routedAddress)) {
            return routedAddress;
        }
        return null;
    }

    private static List<InterfaceCandidate> resolveIpv4Candidates() {
        InetAddress routedAddress = resolveRoutedIpv4Address();
        List<InterfaceCandidate> candidates = new ArrayList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!isCandidateInterface(networkInterface)) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    if (!(address instanceof Inet4Address)
                            || !isUsableIpv4Address(address)
                            || !(interfaceAddress.getBroadcast() instanceof Inet4Address)) {
                        continue;
                    }

                    candidates.add(
                            new InterfaceCandidate(
                                    address,
                                    interfaceAddress.getBroadcast(),
                                    scoreCandidate(networkInterface, address, routedAddress)));
                }
            }
        } catch (SocketException exception) {
            logger.fine("Failed to enumerate LAN interfaces: " + exception.getMessage());
        }

        candidates.sort((left, right) -> Integer.compare(right.score(), left.score()));
        return candidates;
    }

    private static InetAddress resolveRoutedIpv4Address() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress address = socket.getLocalAddress();
            return isUsableIpv4Address(address) ? address : null;
        } catch (IOException exception) {
            return null;
        }
    }

    private static boolean isCandidateInterface(NetworkInterface networkInterface) throws SocketException {
        String descriptor = describeInterface(networkInterface);
        return networkInterface.isUp()
                && !networkInterface.isLoopback()
                && !networkInterface.isPointToPoint()
                && !isRejectedDescriptor(descriptor);
    }

    private static boolean isUsableIpv4Address(InetAddress address) {
        return address instanceof Inet4Address
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress();
    }

    private static int scoreInterface(NetworkInterface networkInterface) throws SocketException {
        String descriptor = describeInterface(networkInterface);

        int score = 0;
        if (isHotspotDescriptor(descriptor)) {
            score += 80;
        }
        if (descriptor.contains("wifi") || descriptor.contains("wi-fi") || descriptor.contains("wlan")) {
            score += 50;
        }
        if (descriptor.contains("wireless")) {
            score += 45;
        }
        if (descriptor.contains("ethernet") || descriptor.contains("eth")) {
            score += 35;
        }
        if (descriptor.contains("bluetooth")) {
            score -= 150;
        }
        if (descriptor.contains("vmware")
                || descriptor.contains("virtualbox")
                || descriptor.contains("hyper-v")
                || descriptor.contains("clash")
                || descriptor.contains("docker")
                || descriptor.contains("meta tunnel")
                || descriptor.contains("tailscale")
                || descriptor.contains("zerotier")
                || descriptor.contains("hamachi")
                || descriptor.contains("wireguard")
                || descriptor.contains("wintun")
                || descriptor.contains("tap")
                || descriptor.contains("tun")
                || descriptor.contains("utun")
                || descriptor.contains("iftype53")
                || descriptor.contains("vpn")) {
            score -= 200;
        }
        if (networkInterface.supportsMulticast()) {
            score += 10;
        }
        if (!networkInterface.isVirtual()) {
            score += 5;
        } else if (isHotspotDescriptor(descriptor)) {
            score += 20;
        } else {
            score -= 25;
        }
        return score;
    }

    private static int scoreCandidate(
            NetworkInterface networkInterface,
            InetAddress address,
            InetAddress routedAddress) throws SocketException {
        int score = scoreInterface(networkInterface);
        if (address.isSiteLocalAddress()) {
            score += 40;
        }
        if (routedAddress != null && routedAddress.equals(address)) {
            score += 10;
        }
        return score;
    }

    private static boolean isRejectedDescriptor(String descriptor) {
        return descriptor.contains("vmware")
                || descriptor.contains("virtualbox")
                || descriptor.contains("hyper-v")
                || descriptor.contains("clash")
                || descriptor.contains("docker")
                || descriptor.contains("meta tunnel")
                || descriptor.contains("tailscale")
                || descriptor.contains("zerotier")
                || descriptor.contains("hamachi")
                || descriptor.contains("wireguard")
                || descriptor.contains("wintun")
                || descriptor.contains("tap")
                || descriptor.contains("tun")
                || descriptor.contains("utun")
                || descriptor.contains("iftype53")
                || descriptor.contains("vpn")
                || descriptor.contains("bluetooth");
    }

    private static boolean isHotspotDescriptor(String descriptor) {
        return descriptor.contains("wi-fi direct")
                || descriptor.contains("wifi direct")
                || descriptor.contains("mobile hotspot");
    }

    private static String describeInterface(NetworkInterface networkInterface) {
        String name = networkInterface.getName() == null ? "" : networkInterface.getName();
        String displayName =
                networkInterface.getDisplayName() == null ? "" : networkInterface.getDisplayName();
        return (name + " " + displayName).toLowerCase();
    }

    public record BroadcastEndpoint(InetAddress localAddress, InetAddress broadcastAddress) {
    }

    private record InterfaceCandidate(InetAddress address, InetAddress broadcast, int score) {
    }
}
