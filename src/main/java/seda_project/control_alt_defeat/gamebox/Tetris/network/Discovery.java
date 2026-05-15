package seda_project.control_alt_defeat.gamebox.Tetris.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public final class Discovery {

    // UDP port
    public static final int PORT = 8766;

    private static final long ANNOUNCE_INTERVAL_MS = 1500;
    private static final long EXPIRY_MS            = 5000;
    private static final int  MAX_PACKET_SIZE      = 1024;

    private Discovery() {}

    public record Advertisement(
            String name,
            String ipAddress,
            int    tcpPort,
            long   lastSeenMs
    ) implements Serializable {}

    public interface Announcer extends AutoCloseable {
        @Override void close();
    }

    public interface Listener extends AutoCloseable {
        List<Advertisement> currentHosts();

        void disableSelfFilter();

        @Override void close();
    }

    public static Announcer announce(String name, int tcpPort) {
        return new AnnouncerImpl(name, tcpPort);
    }

    public static Listener listen() throws IOException {
        return new ListenerImpl();
    }

    private record AdPayload(String name, int tcpPort) implements Serializable {}

    private static final class AnnouncerImpl implements Announcer {
        private final Thread thread;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        AnnouncerImpl(String name, int tcpPort) {
            this.thread = new Thread(() -> announceLoop(name, tcpPort), "discovery-announcer");
            this.thread.setDaemon(true);
            this.thread.start();
        }

        private void announceLoop(String name, int tcpPort) {
            byte[] payload;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(new AdPayload(name, tcpPort));
                }
                payload = baos.toByteArray();
            } catch (IOException e) {
                return; 
            }

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                while (!closed.get()) {
                    for (InetAddress addr : broadcastAddresses()) {
                        try {
                            socket.send(new DatagramPacket(payload, payload.length, addr, PORT));
                        } catch (IOException ignored) {}
                    }
                    try {
                        Thread.sleep(ANNOUNCE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            } catch (SocketException ignored) {}
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) return;
            thread.interrupt();
        }
    }

    private static final class ListenerImpl implements Listener {
        private final DatagramSocket socket;
        private final Thread thread;
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final ConcurrentHashMap<String, Advertisement> hosts = new ConcurrentHashMap<>();
        private volatile boolean filterSelf = true;

        ListenerImpl() throws IOException {
            this.socket = new DatagramSocket(null);
            this.socket.setReuseAddress(true);
            this.socket.bind(new java.net.InetSocketAddress(PORT));

            this.thread = new Thread(this::readLoop, "discovery-listener");
            this.thread.setDaemon(true);
            this.thread.start();
        }

        private void readLoop() {
            byte[] buf = new byte[MAX_PACKET_SIZE];
            while (!closed.get()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    return;
                }
                InetAddress src = packet.getAddress();
                if (filterSelf && isLocal(src)) continue;

                try (ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(packet.getData(), 0, packet.getLength()))) {
                    Object obj = ois.readObject();
                    if (obj instanceof AdPayload p) {
                        String ip = src.getHostAddress();
                        hosts.put(ip, new Advertisement(p.name(), ip, p.tcpPort(),
                                System.currentTimeMillis()));
                    }
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public List<Advertisement> currentHosts() {
            long now = System.currentTimeMillis();
            hosts.values().removeIf(a -> now - a.lastSeenMs() > EXPIRY_MS);
            return List.copyOf(hosts.values());
        }

        @Override
        public void disableSelfFilter() {
            this.filterSelf = false;
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) return;
            socket.close(); 
        }
    }

    private static List<InetAddress> broadcastAddresses() {
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

    private static boolean isLocal(InetAddress addr) {
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
