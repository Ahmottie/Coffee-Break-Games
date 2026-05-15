package seda_project.control_alt_defeat.gamebox.network;

// no "default" keyword here so its abstract methods
// AutoClosable -- built in java interface -- enables try-with-resources with spec syntax (similar to go defer layer.Close())
public interface NetworkLayer extends AutoCloseable {
    void send(Message msg);

    void addListener(NetworkListener listener);

    void removeListener(NetworkListener listener);

    boolean isConnected();

    @Override
    void close();
}
