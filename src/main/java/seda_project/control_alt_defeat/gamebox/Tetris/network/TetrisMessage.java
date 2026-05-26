package seda_project.control_alt_defeat.gamebox.Tetris.network;

import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Message;

public sealed interface TetrisMessage extends Message
        permits TetrisMessage.Hello, TetrisMessage.Input, TetrisMessage.LinesCleared, TetrisMessage.LobbyInfo, TetrisMessage.Ready, TetrisMessage.Restart, TetrisMessage.StartCountdown, TetrisMessage.StateUpdate {

    record Hello(String playerName, int playerLevel) implements TetrisMessage {}

    record LobbyInfo(String hostName, String clientName, int hostLevel, int clientLevel) implements TetrisMessage {}

    record Ready(boolean ready) implements TetrisMessage {}

    record StartCountdown(long delayMs) implements TetrisMessage {}

    record Input(int playerNum, InputAction action) implements TetrisMessage {}

    record StateUpdate(TetrisEngine.GameState state) implements TetrisMessage {}

    record LinesCleared(int playerNum, int lineCount) implements TetrisMessage {}

    record Restart() implements TetrisMessage {}

    enum InputAction {
        LEFT, RIGHT, ROTATE, DROP
    }
}
