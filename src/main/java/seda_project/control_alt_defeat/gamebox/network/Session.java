package seda_project.control_alt_defeat.gamebox.network;

import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;

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

    // Tetris-specific. tetrisEngine is host-only; the client renders
    // received GameState snapshots directly and never owns an engine
    public TetrisEngine tetrisEngine;

    // identity / lobby state shared by both games
    public String       myName;
    public String       peerName;
    public int          myLevel;
    public int          peerLevel;
    public boolean      isHost;
    public boolean      localReady;
    public boolean      peerReady;

    public boolean      lanVertical = true;
    public seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine chessEngine;
}