package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import javafx.scene.paint.Color;
import java.io.Serializable;

public class Block implements Serializable {
    private int x, y;
    private boolean[][] blocks;
    private Color color;

    Block(boolean[][] blocks, Color color) {
        this.blocks = blocks;
        this.color = color;
        this.x = 3; // Tetris spawn X middle of a 10-width board
        this.y = 0; // Spawn at the top
    }

    public void moveDown() { y++; }
    public void moveUp() { y--; } // Needed for the inverted top board
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

    // Rotates the matrix 90 degrees clockwise
    public void rotateClockwise() {
        int n = blocks.length;
        boolean[][] newShape = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[j][n - 1 - i] = blocks[i][j];
            }
        }
        blocks = newShape;
    }

    // Rotates the matrix 90 degrees counter-clockwise (revert invalid rotation)
    public void rotateCounterClockwise() {
        int n = blocks.length;
        boolean[][] newShape = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[n - 1 - j][i] = blocks[i][j];
            }
        }
        blocks = newShape;
    }

    public boolean[][] getShape() { return blocks; }
    public int getX() { return x; }
    public int getY() { return y; }

    public Color getColor() {
        return color;
    }
    //public int getTypeId() { return typeId; }
}