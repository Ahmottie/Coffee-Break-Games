package seda_project.control_alt_defeat.gamebox.Memory.engine;

import java.util.ArrayList;
import java.util.List;

public class GameEngineImpl implements GameEngine {

    private GameConfig config;
    private List<Card> board;
    private int score1;
    private int score2;
    private String activePlayer;
    private List<Integer> flippedIds;
    private boolean gameOver;

    // UI and Network listeners
    private final List<GameEventListener> listeners = new ArrayList<>();

    @Override
    public void start(GameConfig config, GameSetup setup) {
        this.config = config;
        this.board = new ArrayList<>(setup.initialDeck());
        this.activePlayer = setup.firstPlayer();
        this.flippedIds = new ArrayList<>();
        this.score1 = 0;
        this.score2 = 0;
        this.gameOver = false;
    }

    @Override
    public GameSnapshot getSnapshot() {
        return new GameSnapshot(
                List.copyOf(board),
                score1,
                score2,
                activePlayer,
                List.copyOf(flippedIds),
                gameOver
        );
    }

    @Override
    public void addListener(GameEventListener listener) {

    }

    public boolean canFlip(int cardId) {
        if (cardId < 0 || cardId >= board.size() || gameOver) return false;
        Card card = board.get(cardId);
        return !card.removed() && !card.faceUp();
    }

    @Override
    public FlipResult flip(int cardId) {
        // Check if flip is possible
        if (!canFlip(cardId)) {
            return null;
        }

        // Mark card face-up and add to flippedIds
        Card oldCard = board.get(cardId);
        board.set(cardId, new Card(oldCard.id(), oldCard.symbolId(), true, false));
        flippedIds.add(cardId);

        // Notify listeners that a card was flipped
        GameSnapshot snap = getSnapshot();
        for (GameEventListener listener : listeners) {
            listener.onCardFlipped(cardId, snap);
        }

        // Check if flipped exactly K cards
        if (flippedIds.size() == config.k()) {

            // Check if all flipped cards share the same symbol
            boolean isMatch = true;
            int firstSymbol = board.get(flippedIds.getFirst()).symbolId();

            for (int id : flippedIds) {
                if (board.get(id).symbolId() != firstSymbol) {
                    isMatch = false;
                    break;
                }
            }

            // Save the IDs before clearing
            List<Integer> idsToResolve = List.copyOf(flippedIds);

            if (isMatch) {
                // Add 10 points to the active player
                if (activePlayer.equals(config.player1Name())) {
                    score1 += 10;
                } else {
                    score2 += 10;
                }

                // Remove the matched cards
                for (int id : flippedIds) {
                    Card c = board.get(id);
                    board.set(id, new Card(c.id(), c.symbolId(), c.faceUp(), true));
                }

                flippedIds.clear();

                // Check for Game Over
                gameOver = true;
                for (Card c : board) {
                    if (!c.removed()) {
                        gameOver = false;
                        break;
                    }
                }

                // Notify listeners of the match
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMatch(idsToResolve, activePlayer, 10, snap);
                    if (gameOver) {
                        listener.onGameOver(score1, score2, getOutcome(config.player1Name()), getOutcome(config.player2Name()));
                    }
                }

                return new FlipResult(
                        gameOver ? TurnResult.GAME_OVER : TurnResult.MATCH,
                        idsToResolve,
                        activePlayer,
                        10
                );

            } else {
                // Switch player
                if (activePlayer.equals(config.player1Name())) {
                    activePlayer = config.player2Name();
                } else {
                    activePlayer = config.player1Name();
                }

                // Notify listeners of the mismatch before flipping cards down
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMismatch(idsToResolve, snap);
                }

                // Face down
                for (int id : flippedIds) {
                    Card c = board.get(id);
                    board.set(id, new Card(c.id(), c.symbolId(), false, false));
                }

                flippedIds.clear();

                // Notify listeners that cards flipped back and turn changed
                snap = getSnapshot();
                for (GameEventListener listener : listeners) {
                    listener.onMismatchFlipback(idsToResolve, snap);
                    listener.onTurnChanged(activePlayer, snap);
                }

                return new FlipResult(
                        TurnResult.MISMATCH,
                        idsToResolve,
                        activePlayer,
                        0
                );
            }
        }
        return null;
    }

    @Override
    public GameOutcome getOutcome(String playerName) {
        if (!gameOver) throw new IllegalStateException("Game is not over yet.");

        if (score1 == score2) return GameOutcome.DRAW;
        boolean isP1 = playerName.equals(config.player1Name());
        if (isP1) {
            return score1 > score2 ? GameOutcome.WIN : GameOutcome.LOSE;
        } else {
            return score2 > score1 ? GameOutcome.WIN : GameOutcome.LOSE;
        }
    }

    @Override
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getActivePlayer() {
        return activePlayer;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public void resetWith(GameSetup newSetup) {
        start(this.config, newSetup);
    }
}