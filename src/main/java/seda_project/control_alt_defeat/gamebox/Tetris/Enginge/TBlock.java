package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import javafx.scene.paint.Color;

public interface TBlock {
    boolean[][] getShape();
    Color getColor();
    Block toPiece();
}
