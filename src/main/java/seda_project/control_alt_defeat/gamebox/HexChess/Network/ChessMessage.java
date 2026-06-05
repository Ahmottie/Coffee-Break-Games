package seda_project.control_alt_defeat.gamebox.HexChess.Network;

import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Message;

public sealed interface ChessMessage extends Message
        permits ChessMessage.Hello, ChessMessage.Input, ChessMessage.LobbyInfo, ChessMessage.Ready, ChessMessage.Restart, ChessMessage.StartCountdown, ChessMessage.StateUpdate {

    record Hello(String playerName) implements ChessMessage {}

    record LobbyInfo(String hostName, String clientName) implements ChessMessage {}

    record Ready(boolean ready) implements ChessMessage {}

    record StartCountdown(long delayMs) implements ChessMessage {}

    public record Input(int playerNum, int blockIndex, InputAction action) implements Message, ChessMessage {}

    record StateUpdate(TetrisEngine.GameState state) implements ChessMessage {}

    record Restart() implements ChessMessage {}

    enum InputAction {
        LEFT, RIGHT, ROTATE, TOGGLE, DROP
    }
}
