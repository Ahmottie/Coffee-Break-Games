package seda_project.control_alt_defeat.gamebox.HexChess.Network;

import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceType;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Message;

public sealed interface ChessMessage extends Message
        permits ChessMessage.DrawDeclined, ChessMessage.DrawOffer, ChessMessage.GameEnded, ChessMessage.Hello, ChessMessage.Input, ChessMessage.LobbyInfo, ChessMessage.PromotionChoice, ChessMessage.Ready, ChessMessage.Restart, ChessMessage.StartCountdown, ChessMessage.StateUpdate {

    record Hello(String playerName) implements ChessMessage {}

    record LobbyInfo(String hostName, String clientName) implements ChessMessage {}

    record Ready(boolean ready) implements ChessMessage {}

    record StartCountdown(long delayMs) implements ChessMessage {}

    record Input(String fromId, String toId) implements ChessMessage {}

    record Restart() implements ChessMessage {}

    record StateUpdate(String fromId, String toId, String capturedPieceId) implements ChessMessage {}

    record GameEnded(String reason, int winner) implements ChessMessage {}

    record PromotionChoice(String coordId, PieceType pieceType) implements ChessMessage {}

    final class DrawOffer implements ChessMessage {
        private final int proposing; // 1 for P1, 2 for P2
        public DrawOffer(int proposing) { this.proposing = proposing; }
        public int getProposing() { return proposing; }
    }

    final class DrawDeclined implements ChessMessage {
        private final int proposing;
        public DrawDeclined(int proposing) { this.proposing = proposing; }
        public int getProposing() { return proposing; }
    }
}
