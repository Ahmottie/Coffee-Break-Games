package seda_project.control_alt_defeat.gamebox.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public final class LanHost {

    private LanHost() {}

    public static NetworkLayer host(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(0); // wait forever for a client
            Socket client = server.accept(); // blocks until client connects and then reutrns the socket
            return new LanSession(client); // wrap in lansession and return as NetworkLayer
        }
    }

    // async wrapper to prevent blocking ux so we run it in the background and use cb
    public static void hostAsync(int port,
                                 Consumer<NetworkLayer> onReady, // called with NetworkLayer once client is connected
                                 Consumer<Throwable>    onError) { // called when anything goes wrong
        Thread t = new Thread(() -> {
            try {
                // since server.accept blocks,
                NetworkLayer layer = host(port);
                onReady.accept(layer);
            } catch (Throwable e) {
                onError.accept(e);
            }
        }, "lan-host-accept");
        t.setDaemon(true);
        t.start();
    }
}