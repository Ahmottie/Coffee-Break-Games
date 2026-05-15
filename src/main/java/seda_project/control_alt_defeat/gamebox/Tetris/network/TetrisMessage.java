package seda_project.control_alt_defeat.gamebox.Tetris.network;

import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Message;

public sealed interface TetrisMessage extends Message
        permits TetrisMessage.Hello,
                TetrisMessage.LobbyInfo,
                TetrisMessage.Ready,
                TetrisMessage.StartCountdown,
                TetrisMessage.Input,
                TetrisMessage.StateUpdate,
                TetrisMessage.LinesCleared,
                TetrisMessage.Restart {

    record Hello(String playerName) implements TetrisMessage {}

    record LobbyInfo(String hostName, String clientName) implements TetrisMessage {}

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
