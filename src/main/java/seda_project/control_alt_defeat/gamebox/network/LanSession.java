package seda_project.control_alt_defeat.gamebox.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

final class LanSession implements NetworkLayer {

    private static final long HEARTBEAT_INTERVAL_MS = 1500L;
    private static final int  READ_TIMEOUT_MS       = 5000;

    private final Socket             socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;

    private final List<NetworkListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean         closed    = new AtomicBoolean(false);

    private final Thread reader;
    private final Thread heartbeat;

    LanSession(Socket socket) throws IOException {
        this.socket = socket;
        this.socket.setSoTimeout(READ_TIMEOUT_MS);
        this.socket.setTcpNoDelay(true);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());

        this.reader    = new Thread(this::readLoop,      "lan-reader");
        this.heartbeat = new Thread(this::heartbeatLoop, "lan-heartbeat");
        this.reader.setDaemon(true);
        this.heartbeat.setDaemon(true);

        this.reader.start();
        this.heartbeat.start();
    }

    @Override
    public void send(GameMessage msg) {
        if (closed.get()) return;
        try {
            synchronized (out) {
                out.writeObject(msg);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            fireError(e);
            disconnect("send failed: " + e.getClass().getSimpleName());
        }
    }

    private void readLoop() {
        try {
            while (!closed.get()) {
                Object obj = in.readObject();
                if (!(obj instanceof GameMessage msg)) continue;
                if (msg instanceof GameMessage.Heartbeat) continue;
                if (msg instanceof GameMessage.Disconnect d) {
                    disconnect("peer: " + d.reason());
                    return;
                }
                for (NetworkListener l : listeners) {
                    try { l.onMessage(msg); }
                    catch (Throwable t) { fireError(t); }
                }
            }
        } catch (SocketTimeoutException e) {
            disconnect("peer unresponsive (>5s)");
        } catch (Exception e) {
            if (!closed.get()) {
                disconnect("read failed: " + e.getClass().getSimpleName());
            }
        }
    }

    private void heartbeatLoop() {
        try {
            while (!closed.get()) {
                Thread.sleep(HEARTBEAT_INTERVAL_MS);
                if (closed.get()) return;
                send(new GameMessage.Heartbeat(System.currentTimeMillis()));
            }
        } catch (InterruptedException ignored) {}
    }

    private void disconnect(String reason) {
        if (!closed.compareAndSet(false, true)) return;
        try { socket.close(); } catch (IOException ignored) {}
        for (NetworkListener l : listeners) {
            try { l.onDisconnected(reason); }
            catch (Throwable ignored) {}
        }
    }

    private void fireError(Throwable t) {
        for (NetworkListener l : listeners) {
            try { l.onError(t); }
            catch (Throwable ignored) {}
        }
    }

    @Override public void addListener(NetworkListener l)    { listeners.add(l); }
    @Override public void removeListener(NetworkListener l) { listeners.remove(l); }
    @Override public boolean isConnected()                  { return !closed.get(); }

    @Override
    public void close() {
        if (closed.get()) return;
        try { send(new GameMessage.Disconnect("graceful")); }
        catch (Exception ignored) {}
        disconnect("local close");
    }
}