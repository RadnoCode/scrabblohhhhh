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

/**
 * Resolves LAN addresses used for joining and UDP broadcasting.
 */
public final class LanHostAddressResolver {
    private static final Logger logger =
            Logger.getLogger(LanHostAddressResolver.class.getName());

    /**
     * Prevents creating this utility class.
     */
    private LanHostAddressResolver() {
    }

    /**
     * Resolves the host endpoint that clients should join.
     *
     * @param port TCP port
     * @return host:port endpoint, or empty string
     */
    public static String resolveJoinEndpoint(int port) {
        InetAddress address = resolvePreferredIpv4Address();
        if (address == null) {
            return "";
        }
        return address.getHostAddress() + ":" + port;
    }

    /**
     * Gets the best broadcast endpoint.
     *
     * @return preferred broadcast endpoint, or {@code null}
     */
    public static BroadcastEndpoint resolvePreferredBroadcastEndpoint() {
        List<BroadcastEndpoint> endpoints = resolveBroadcastEndpoints();
        if (endpoints.isEmpty()) {
            return null;
        }
        return endpoints.get(0);
    }

    /**
     * Resolves broadcast endpoints for all usable LAN interfaces.
     *
     * @return broadcast endpoints
     */
    public static List<BroadcastEndpoint> resolveBroadcastEndpoints() {
        List<BroadcastEndpoint> endpoints = new ArrayList<>();
        for (InterfaceCandidate candidate : resolveIpv4Candidates()) {
            endpoints.add(new BroadcastEndpoint(candidate.address(), candidate.broadcast()));
        }
        return endpoints;
    }

    /**
     * Resolves preferred broadcast addresses.
     *
     * @return broadcast addresses
     */
    public static Set<InetAddress> resolvePreferredBroadcastAddresses() {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        for (BroadcastEndpoint endpoint : resolveBroadcastEndpoints()) {
            addresses.add(endpoint.broadcastAddress());
        }
        return addresses;
    }

    /**
     * Resolves the preferred IPv4 address for hosting.
     *
     * @return IPv4 address, or {@code null}
     */
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

    /**
     * Finds and scores usable IPv4 interface candidates.
     *
     * @return sorted interface candidates
     */
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

    /**
     * Finds the local address selected by normal routing.
     *
     * @return routed IPv4 address, or {@code null}
     */
    private static InetAddress resolveRoutedIpv4Address() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress address = socket.getLocalAddress();
            return isUsableIpv4Address(address) ? address : null;
        } catch (IOException exception) {
            return null;
        }
    }

    /**
     * Checks whether a network interface should be considered.
     *
     * @param networkInterface network interface
     * @return {@code true} if usable
     * @throws SocketException if interface info cannot be read
     */
    private static boolean isCandidateInterface(NetworkInterface networkInterface) throws SocketException {
        String descriptor = describeInterface(networkInterface);
        return networkInterface.isUp()
                && !networkInterface.isLoopback()
                && !networkInterface.isPointToPoint()
                && !isRejectedDescriptor(descriptor);
    }

    /**
     * Checks whether an address is a usable IPv4 address.
     *
     * @param address address to inspect
     * @return {@code true} if usable
     */
    private static boolean isUsableIpv4Address(InetAddress address) {
        return address instanceof Inet4Address
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress();
    }

    /**
     * Scores a network interface by likely LAN usefulness.
     *
     * @param networkInterface network interface
     * @return score
     * @throws SocketException if interface info cannot be read
     */
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

    /**
     * Scores one interface address candidate.
     *
     * @param networkInterface network interface
     * @param address IPv4 address
     * @param routedAddress routed IPv4 address
     * @return score
     * @throws SocketException if interface info cannot be read
     */
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

    /**
     * Checks whether an interface description should be rejected.
     *
     * @param descriptor lower-case interface description
     * @return {@code true} if rejected
     */
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

    /**
     * Checks whether an interface looks like a mobile hotspot.
     *
     * @param descriptor lower-case interface description
     * @return {@code true} if hotspot-like
     */
    private static boolean isHotspotDescriptor(String descriptor) {
        return descriptor.contains("wi-fi direct")
                || descriptor.contains("wifi direct")
                || descriptor.contains("mobile hotspot");
    }

    /**
     * Builds a lower-case description for an interface.
     *
     * @param networkInterface network interface
     * @return description
     */
    private static String describeInterface(NetworkInterface networkInterface) {
        String name = networkInterface.getName() == null ? "" : networkInterface.getName();
        String displayName =
                networkInterface.getDisplayName() == null ? "" : networkInterface.getDisplayName();
        return (name + " " + displayName).toLowerCase();
    }

    /**
     * Local and broadcast address pair for one LAN interface.
     *
     * @param localAddress local IPv4 address
     * @param broadcastAddress broadcast address
     */
    public record BroadcastEndpoint(InetAddress localAddress, InetAddress broadcastAddress) {
    }

    /**
     * Scored candidate interface address.
     *
     * @param address local IPv4 address
     * @param broadcast broadcast address
     * @param score selection score
     */
    private record InterfaceCandidate(InetAddress address, InetAddress broadcast, int score) {
    }
}
