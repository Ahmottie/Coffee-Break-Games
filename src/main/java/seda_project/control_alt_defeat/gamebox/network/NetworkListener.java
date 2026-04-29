package seda_project.control_alt_defeat.gamebox.network;

public interface NetworkListener {
    default void onConnected(String peerInfo) {}

    default void onMessage(GameMessage msg) {}

    default void onDisconnected(String reason) {}

    default void onError(Throwable t) {}
}
