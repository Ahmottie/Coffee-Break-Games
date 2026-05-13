package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import java.io.Serializable;

public record GameState(
        boolean[][] player1Grid,
        boolean[][] player2Grid,
        Block player1ActiveBlock,
        Block player2ActiveBlock,
        int player1Score,
        int player2Score,
        String player1Name,
        String player2Name,
        boolean player1Lost,
        boolean player2Lost,
        boolean isGameOver
) implements Serializable {}