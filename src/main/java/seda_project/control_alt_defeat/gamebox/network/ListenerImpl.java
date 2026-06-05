package seda_project.control_alt_defeat.gamebox.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

final class ListenerImpl implements Listener {
    private final DatagramSocket socket;
    private final Thread thread;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, Advertisement> hosts = new ConcurrentHashMap<>();
    private volatile boolean filterSelf = true;

    ListenerImpl() throws IOException {
        this.socket = new DatagramSocket(null);
        this.socket.setReuseAddress(true);
        this.socket.bind(new java.net.InetSocketAddress(Discovery.PORT));

        this.thread = new Thread(this::readLoop, "discovery-listener");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    private void readLoop() {
        byte[] buf = new byte[Discovery.MAX_PACKET_SIZE];
        while (!closed.get()) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                return;
            }
            InetAddress src = packet.getAddress();
            if (filterSelf && Discovery.isLocal(src)) continue;

            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(packet.getData(), 0, packet.getLength()))) {
                Object obj = ois.readObject();
                if (obj instanceof AdPayload p) {
                    String ip = src.getHostAddress();
                    hosts.put(ip, new Advertisement(p.name(), p.level(), ip, p.tcpPort(),
                            System.currentTimeMillis(), p.gameMode()));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public List<Advertisement> currentHosts() {
        long now = System.currentTimeMillis();
        hosts.values().removeIf(a -> now - a.lastSeenMs() > Discovery.EXPIRY_MS);
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
