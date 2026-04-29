package seda_project.control_alt_defeat.gamebox.network;

import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;

public final class Session {

    private static Session current;

    // some singleton stuff here to get exactly one instance for class.
    public static Session current() {
        if (current == null) current = new Session();
        return current;
    }

    // this makes the network connection go bum bum
    public static void clear() {
        if (current != null && current.network != null) {
            try { current.network.close(); } catch (Exception ignored) {}
        }
        current = null;
    }

    public NetworkLayer network;
    public GameConfig   config;
    public GameSetup    setup;
    public GameEngine   engine;
    public String       myName;
    public boolean      isHost;
    public boolean      localReady; // boolean java defaults to false
    public boolean      peerReady;
}