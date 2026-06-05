package seda_project.control_alt_defeat.gamebox.network;

import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessAnnouncer;
import seda_project.control_alt_defeat.gamebox.Tetris.network.TetrisAnnouncer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class Discovery {

    // UDP port
    public static final int PORT = 8766;

    static final long ANNOUNCE_INTERVAL_MS = 1500;
    static final long EXPIRY_MS            = 5000;
    static final int  MAX_PACKET_SIZE      = 1024;

    private Discovery() {}

    public static TetrisAnnouncer announceTetris(String name, int tcpPort, int level) {
        return new TetrisAnnouncer(name, tcpPort, level);
    }

    public static ChessAnnouncer announceChess(String name, int tcpPort) {
        return new ChessAnnouncer(name, tcpPort);
    }

    public static Listener listen() throws IOException {
        return new ListenerImpl();
    }

    static List<InetAddress> broadcastAddresses() {
        List<InetAddress> result = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress broadcast = ia.getBroadcast();
                    if (broadcast != null) result.add(broadcast);
                }
            }
        } catch (SocketException ignored) {}
        if (result.isEmpty()) {
            try { result.add(InetAddress.getByName("255.255.255.255")); }
            catch (UnknownHostException ignored) {}
        }
        return result;
    }

    static boolean isLocal(InetAddress addr) {
        if (addr.isLoopbackAddress() || addr.isAnyLocalAddress()) return true;
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface ni = ifaces.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    if (addr.equals(addrs.nextElement())) return true;
                }
            }
        } catch (SocketException ignored) {}
        return false;
    }
}
