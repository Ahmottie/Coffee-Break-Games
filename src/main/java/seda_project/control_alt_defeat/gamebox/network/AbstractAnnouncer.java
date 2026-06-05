package seda_project.control_alt_defeat.gamebox.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractAnnouncer implements Announcer {

    private final Thread thread;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    protected AbstractAnnouncer(String name, int tcpPort, int level, GameMode gameMode) {
        this.thread = new Thread(() -> announceLoop(name, tcpPort, level, gameMode), "discovery-announcer");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    private void announceLoop(String name, int tcpPort, int level, GameMode gameMode) {
        byte[] payload;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new AdPayload(name, tcpPort, level, gameMode));
            }
            payload = baos.toByteArray();
        } catch (IOException e) {
            return;
        }

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            while (!closed.get()) {
                for (InetAddress addr : Discovery.broadcastAddresses()) {
                    try {
                        socket.send(new DatagramPacket(payload, payload.length, addr, Discovery.PORT));
                    } catch (IOException ignored) {}
                }
                try {
                    Thread.sleep(Discovery.ANNOUNCE_INTERVAL_MS);
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
