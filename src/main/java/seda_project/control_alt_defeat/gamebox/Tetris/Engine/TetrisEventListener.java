package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.util.List;

public interface TetrisEventListener {
    default void onTick(TetrisEngine.GameState snapshot) {}
    default void onBlockLocked(int playerNum, TetrisEngine.GameState snapshot) {}
    default void onLinesCleared(int playerNum, int lineCount, TetrisEngine.GameState snapshot) {}
    default void onPlayerLost(int playerNum, TetrisEngine.GameState snapshot) {}
    default void onLevelChanged(int newLevel, long newTickIntervalMs, TetrisEngine.GameState snapshot) {}
    default void onGameOver(TetrisEngine.GameState snapshot) {}
    default void onReset(TetrisEngine.GameState snapshot) {}
    default void onPowerUpTriggered(int triggeringPlayer, TetrisEngine.GameState snapshot) {}
    default void onPowerUpSpawned(TetrisEngine.GameState snapshot){}
    default void onStopped(TetrisEngine.GameState snapStopped){};
}