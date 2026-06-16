package seda_project.control_alt_defeat.gamebox.HexChess.Network;

import seda_project.control_alt_defeat.gamebox.network.AbstractAnnouncer;
import seda_project.control_alt_defeat.gamebox.network.GameMode;

public final class ChessAnnouncer extends AbstractAnnouncer {

    public ChessAnnouncer(String name, int tcpPort, String boardState) {
        super(name, tcpPort, 0, GameMode.HEXCHESS, boardState);
    }
}
