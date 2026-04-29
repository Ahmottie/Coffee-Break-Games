package seda_project.control_alt_defeat.gamebox.engine;

import java.util.List;

public interface GameEventListener {

    // A card is flipped face-up
    default void onCardFlipped(
            int cardId,
            GameSnapshot snapshot
    ) {}

    // A flip completes a K-tuple match.
    default void onMatch(
            List<Integer> matchedIds,
            String scoringPlayer,
            int scoreAwarded,
            GameSnapshot snapshot
    ) {}

    // The K flipped cards are not a match.
    default void onMismatch(
            List<Integer> flippedIds,
            GameSnapshot snapshot
    ) {}

    // Cards are flipped back face-down.
    default void onMismatchFlipback(
            List<Integer> cardIds,
            GameSnapshot snapshot
    ) {}

    // The active player changes
    default void onTurnChanged(
            String newActivePlayer,
            GameSnapshot snapshot
    ) {}

    // last card pair/tuple is removed.
    default void onGameOver(
            int finalScore1,
            int finalScore2,
            GameOutcome outcomeForPlayer1,
            GameOutcome outcomeForPlayer2
    ) {}
}
