package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import java.io.Serializable;

public class Block implements Serializable {
    private int[][] shape;
    private int x, y;
    private int typeId; // To identify colors/shapes later for Erik

    public Block(int[][] shape, int typeId) {
        this.shape = shape;
        this.typeId = typeId;
        this.x = 3; // Tetris spawn X middle of a 10-width board
        this.y = 0; // Spawn at the top
    }

    public void moveDown() { y++; }
    public void moveUp() { y--; } // Needed for the inverted top board
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

    // Rotates the matrix 90 degrees clockwise
    public void rotateClockwise() {
        int n = shape.length;
        int[][] newShape = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[j][n - 1 - i] = shape[i][j];
            }
        }
        shape = newShape;
    }

    // Rotates the matrix 90 degrees counter-clockwise (revert invalid rotation)
    public void rotateCounterClockwise() {
        int n = shape.length;
        int[][] newShape = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newShape[n - 1 - j][i] = shape[i][j];
            }
        }
        shape = newShape;
    }

    public int[][] getShape() { return shape; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getTypeId() { return typeId; }
}