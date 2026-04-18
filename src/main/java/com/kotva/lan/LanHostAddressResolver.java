package com.kotva.lan;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedHashSet;
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

    public static Set<InetAddress> resolvePreferredBroadcastAddresses() {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        InetAddress preferredAddress = resolvePreferredIpv4Address();

        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!isCandidateInterface(networkInterface)) {
                    continue;
                }

                boolean matchesPreferredInterface = preferredAddress == null;
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (!(address instanceof Inet4Address) || !(broadcast instanceof Inet4Address)) {
                        continue;
                    }
                    if (!isUsableIpv4Address(address)) {
                        continue;
                    }
                    if (preferredAddress != null && preferredAddress.equals(address)) {
                        matchesPreferredInterface = true;
                    }
                }

                if (!matchesPreferredInterface) {
                    continue;
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (address instanceof Inet4Address
                            && broadcast instanceof Inet4Address
                            && isUsableIpv4Address(address)) {
                        addresses.add(broadcast);
                    }
                }
            }
        } catch (SocketException exception) {
            logger.fine("Failed to resolve LAN broadcast addresses: " + exception.getMessage());
        }

        return addresses;
    }

    public static InetAddress resolvePreferredIpv4Address() {
        InetAddress routedAddress = resolveRoutedIpv4Address();
        if (isUsableIpv4Address(routedAddress)) {
            return routedAddress;
        }

        InetAddress bestAddress = null;
        int bestScore = Integer.MIN_VALUE;
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

                    int score = scoreInterface(networkInterface);
                    if (score > bestScore) {
                        bestScore = score;
                        bestAddress = address;
                    }
                }
            }
        } catch (SocketException exception) {
            logger.fine("Failed to enumerate LAN interfaces: " + exception.getMessage());
        }
        return bestAddress;
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
        return networkInterface.isUp()
                && !networkInterface.isLoopback()
                && !networkInterface.isPointToPoint()
                && !networkInterface.isVirtual()
                && scoreInterface(networkInterface) > 0;
    }

    private static boolean isUsableIpv4Address(InetAddress address) {
        return address instanceof Inet4Address
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isLinkLocalAddress();
    }

    private static int scoreInterface(NetworkInterface networkInterface) throws SocketException {
        String descriptor = (
                networkInterface.getName() + " " + networkInterface.getDisplayName()).toLowerCase();

        int score = 0;
        if (descriptor.contains("wifi") || descriptor.contains("wi-fi") || descriptor.contains("wlan")) {
            score += 50;
        }
        if (descriptor.contains("wireless")) {
            score += 45;
        }
        if (descriptor.contains("ethernet") || descriptor.contains("eth")) {
            score += 35;
        }
        if (descriptor.contains("vmware")
                || descriptor.contains("virtualbox")
                || descriptor.contains("hyper-v")
                || descriptor.contains("docker")
                || descriptor.contains("tailscale")
                || descriptor.contains("utun")
                || descriptor.contains("vpn")
                || descriptor.contains("bluetooth")) {
            score -= 100;
        }
        if (networkInterface.supportsMulticast()) {
            score += 10;
        }
        if (!networkInterface.isVirtual()) {
            score += 5;
        }
        return score;
    }
}
