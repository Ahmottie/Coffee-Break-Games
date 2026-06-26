package seda_project.control_alt_defeat.gamebox.Tetris.network;

import seda_project.control_alt_defeat.gamebox.Tetris.Engine.AttackType;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.Block;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Message;

public sealed interface TetrisMessage extends Message
        permits TetrisMessage.Hello, TetrisMessage.Input, TetrisMessage.LinesCleared, TetrisMessage.LobbyInfo, TetrisMessage.Ready, TetrisMessage.Restart, TetrisMessage.StartCountdown, TetrisMessage.StateUpdate, TetrisMessage.BoardState, TetrisMessage.PlayerLost, TetrisMessage.Attack, TetrisMessage.SettingsSync, TetrisMessage.PortalBlock, TetrisMessage.BoardShrink, TetrisMessage.SwapActiveRequest, TetrisMessage.SwapActiveResponse, TetrisMessage.SwapBoardsRequest, TetrisMessage.SwapBoardsResponse {

    record Hello(String playerName, int playerLevel) implements TetrisMessage {}

    record LobbyInfo(String hostName, String clientName, int hostLevel, int clientLevel, boolean vertical) implements TetrisMessage {}

    record Ready(boolean ready) implements TetrisMessage {}

    record StartCountdown(long delayMs) implements TetrisMessage {}

    public record Input(int playerNum, int blockIndex, InputAction action) implements Message, TetrisMessage {}

    record StateUpdate(TetrisEngine.GameState state) implements TetrisMessage {}

    record LinesCleared(int playerNum, int lineCount) implements TetrisMessage {}

    record Restart() implements TetrisMessage {}

    // Dual-engine model: the sender simulates its own board and ships that board's
    // snapshot to the opponent for display. senderPlayer is the sender's player
    // number (host = 1, client = 2); the receiver renders only the sender's half.
    record BoardState(TetrisEngine.GameState state, int senderPlayer) implements TetrisMessage {}

    // Dual-engine model: announces that the sender's player has topped out, so the
    // peer can declare game-over once both players are lost. Carries final score and
    // lines so each side has both players' totals for the result screen.
    record PlayerLost(int playerNum, int finalScore, int finalLines) implements TetrisMessage {}

    // Dual-engine model: an opponent-targeted power-up effect the receiver applies
    // to its own board (the sender simulates only its own board).
    record Attack(AttackType type) implements TetrisMessage {}

    // Dual-engine model: the host's advanced settings (power-up types, spawn rates,
    // bombs, two-blocks, board-change) so the client builds an identical engine.
    record SettingsSync(TetrisAdvancedSettings settings) implements TetrisMessage {}

    // Dual-engine model: a PORTAL power-up sent this block from the sender's board to
    // the receiver's board, where it is queued as the next active block.
    record PortalBlock(Block block) implements TetrisMessage {}

    // Dual-engine model: board-change. The sender cleared rows and grew its board, so
    // the receiver shrinks its own board by the same amount.
    record BoardShrink(int rows) implements TetrisMessage {}

    // Dual-engine model: SWAPACTIVEBLOCKS handshake. Request carries the initiator's
    // active blocks; Response carries the responder's original active blocks.
    record SwapActiveRequest(Block[] blocks) implements TetrisMessage {}
    record SwapActiveResponse(Block[] blocks) implements TetrisMessage {}

    // Dual-engine model: SWAPBOARDS handshake. Each side ships its locked grid and
    // active blocks; the peer adopts them (rotated to its own orientation).
    record SwapBoardsRequest(String[][] grid, Block[] blocks) implements TetrisMessage {}
    record SwapBoardsResponse(String[][] grid, Block[] blocks) implements TetrisMessage {}

    enum InputAction {
        LEFT, RIGHT, ROTATE, TOGGLE, DROP
    }
}
