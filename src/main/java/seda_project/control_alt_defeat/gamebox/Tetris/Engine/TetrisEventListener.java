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
    /** Dual-engine LAN: an opponent-targeted effect was triggered on my board and
     *  must be sent over the network for the opponent to apply to their own board. */
    default void onOpponentAttack(AttackType type) {}
    /** Dual-engine LAN: a PORTAL sent this block to the opponent's board; deliver it. */
    default void onPortalOut(Block block) {}
    /** Dual-engine LAN: I cleared lines (board-change); tell the opponent to shrink by N. */
    default void onBoardShrinkOut(int rows) {}
    /** Dual-engine LAN: swap-active-blocks handshake. */
    default void onSwapActiveRequest(Block[] blocks) {}
    default void onSwapActiveResponse(Block[] blocks) {}
    /** Dual-engine LAN: swap-boards handshake. */
    default void onSwapBoardsRequest(String[][] grid, Block[] blocks) {}
    default void onSwapBoardsResponse(String[][] grid, Block[] blocks) {}
    default void radialBomb() {}
    default void lockSound() {}
    default void columnBomb() {}
}
