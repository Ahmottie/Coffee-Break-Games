package seda_project.control_alt_defeat.gamebox.Tetris.network;

import seda_project.control_alt_defeat.gamebox.network.AbstractAnnouncer;
import seda_project.control_alt_defeat.gamebox.network.GameMode;

public final class TetrisAnnouncer extends AbstractAnnouncer {

    public TetrisAnnouncer(String name, int tcpPort, int level) {
        super(name, tcpPort, level, GameMode.TETRIS);
    }
}
