package seda_project.control_alt_defeat.gamebox.network;

import java.net.*;
import java.util.Enumeration;

public final class Lan {
    private Lan() {}

    public static final int DEFAULT_PORT = 8765;

    public static String localIp() {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {}
        return "unknown";
    }

    public static boolean isValidIp(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            InetAddress.getByName(s.trim());
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
