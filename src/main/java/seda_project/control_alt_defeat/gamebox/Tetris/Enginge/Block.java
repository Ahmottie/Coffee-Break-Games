package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import javafx.scene.paint.Color;

public class Block {
    private boolean[][] blocks;
    private Color color;

    Block(boolean[][] blocks, Color color) {
        this.blocks = blocks;
        this.color = color;
    }
}
