package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

public interface TetrisEventListener {
    default void onTick(TetrisEngine.GameState snapshot, int player) {}
    default void onBlockLocked(int playerNum, TetrisEngine.GameState snapshot) {}
    default void onLinesCleared(int playerNum, int lineCount, TetrisEngine.GameState snapshot) {}
    default void onPlayerLost(int playerNum, TetrisEngine.GameState snapshot) {}
    default void onLevelChanged(long newTickIntervalMs, TetrisEngine.GameState snapshot, int player) {}
    default void onGameOver(TetrisEngine.GameState snapshot) {}
    default void onReset(TetrisEngine.GameState snapshot) {}
    default void onPowerUpTriggered(TetrisEngine.GameState snapshot, PowerUp p) {}
    default void onPowerUpSpawned(TetrisEngine.GameState snapshot){}
    default void onStopped(TetrisEngine.GameState snapStopped){};
    default void onBlockMovement(TetrisEngine.GameState snapshot, int player){};
    default void onBlockSwap(TetrisEngine.GameState snapshot){};
    default void clearPowerUps(){}
    default void onBoardSizeChange(int playerNum,int linesCleared,TetrisEngine.GameState snapshot) {}
    default void changeTickSpeed(int playerNum, long newTickSpeed) {}
    default void radialBomb() {}
    default void lockSound() {}
    default void columnBomb() {}
}
