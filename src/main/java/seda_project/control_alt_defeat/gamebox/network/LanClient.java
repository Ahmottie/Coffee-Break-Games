package seda_project.control_alt_defeat.gamebox.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public final class LanClient {

    private LanClient() {}

    public static NetworkLayer join(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);
        return new LanSession(socket);
    }

    public static void joinAsync(String host, int port,
                                 Consumer<NetworkLayer> onReady,
                                 Consumer<Throwable>    onError) {
        Thread t = new Thread(() -> {
            try {
                NetworkLayer layer = join(host, port);
                onReady.accept(layer);
            } catch (Throwable e) {
                onError.accept(e);
            }
        }, "lan-client-connect");
        t.setDaemon(true);
        t.start();
    }
}