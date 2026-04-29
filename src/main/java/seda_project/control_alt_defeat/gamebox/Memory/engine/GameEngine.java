package seda_project.control_alt_defeat.gamebox.Memory.engine;

public interface GameEngine {

    long MISMATCH_DELAY_MS = 1500L;                // How long to display a mismatched flip before flipping a card/cards back
    void start(GameConfig config, GameSetup setup);
    FlipResult flip(int cardId);                   // Player flips a card. Returns Null if the flip is not allowed.
    GameSnapshot getSnapshot();                    // Current state
    void addListener(GameEventListener listener);
    void removeListener(GameEventListener listener);
    String getActivePlayer();
    boolean isGameOver();                           // Check if the game is ended
    GameOutcome getOutcome(String playerName);      // Final outcome of the game for playerName
    void resetWith(GameSetup newSetup);             // Reset new game
}
