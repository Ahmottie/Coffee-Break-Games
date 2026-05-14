package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public record GameState(
        String[][] p1Grid,
        String[][] p2Grid,
        Block      p1ActiveBlock,
        Block      p2ActiveBlock,
        int        p1Score,
        int        p2Score,
        String     p1Name,
        String     p2Name,
        boolean    p1Lost,
        boolean    p2Lost,
        boolean    gameOver,
        int        level
) implements Serializable {}