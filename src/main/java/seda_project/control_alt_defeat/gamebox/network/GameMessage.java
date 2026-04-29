package seda_project.control_alt_defeat.gamebox.network;

import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;

import java.io.Serializable;

// sealed interface = only the types listed in permits can implement this
// closes the other message types so nothing else can sneak in
// Serializable is a marker: enables Java's built-in binary serialization
// so we can ship these objects across a socket. Like JSON.stringify but
// for Java's native byte format
public sealed interface GameMessage extends Serializable
        permits GameMessage.Hello,
        GameMessage.LobbyConfig,
        GameMessage.Ready,
        GameMessage.StartCountdown,
        GameMessage.Flip,
        GameMessage.Heartbeat,
        GameMessage.Disconnect {

    // records are like classess that implements the interface
    // they auto gen  constructor, accessor method per field..
    record Hello(String playerName) implements GameMessage {}

    record LobbyConfig(GameConfig config, GameSetup setup) implements GameMessage {}

    record Ready(boolean ready) implements GameMessage {}

    record StartCountdown(long delayMs) implements GameMessage {}

    record Flip(int cardId) implements GameMessage {}

    record Heartbeat(long sentAt) implements GameMessage {}

    record Disconnect(String reason) implements GameMessage {}
}