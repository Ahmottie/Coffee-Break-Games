package seda_project.control_alt_defeat.gamebox.engine;

import java.util.List;

public record GameSnapshot(
        List<Card> cards,
        int score1,
        int score2,
        String activePlayer,
        List<Integer> flippedIds,
        boolean gameOver
) implements java.io.Serializable {}