package seda_project.control_alt_defeat.gamebox.network;

import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;

public sealed interface GameMessage extends Message
        permits GameMessage.Flip,
                GameMessage.Hello,
                GameMessage.LobbyConfig,
                GameMessage.NewGame,
                GameMessage.Ready,
                GameMessage.StartCountdown {

    record Hello(String playerName) implements GameMessage {}

    record LobbyConfig(GameConfig config, GameSetup setup) implements GameMessage {}

    record Ready(boolean ready) implements GameMessage {}

    record StartCountdown(long delayMs) implements GameMessage {}

    record Flip(int cardId) implements GameMessage {}

    record NewGame(GameConfig config, GameSetup setup) implements GameMessage {}
}
