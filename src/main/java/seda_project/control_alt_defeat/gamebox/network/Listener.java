package seda_project.control_alt_defeat.gamebox.network;

import java.util.List;

public interface Listener extends AutoCloseable {
    List<Advertisement> currentHosts();

    void disableSelfFilter();

    @Override void close();
}
