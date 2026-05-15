package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;
import java.io.Serializable;

public class Block implements Serializable {
    private int x, y;
    private boolean[][] blocks;
    private String hexColor;

    public Block(boolean[][] shape, Color fxColor) {
        // Deep copy the shape to prevent mutating the Enum's base array during rotation
        this.blocks = new boolean[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            this.blocks[i] = shape[i].clone();
        }

        // Convert Color to String
        this.hexColor = fxColor.toString();

        this.x = 3; // Tetris spawn X middle of a 10-width board
        this.y = 0; // Spawn at the top
    }

    private Block(boolean[][] clonedShape, String hexColor, int x, int y) {
        this.blocks = clonedShape;
        this.hexColor = hexColor;
        this.x = x;
        this.y = y;
    }

    public Block cloneForSnapshot() {
        boolean[][] clonedBlocks = new boolean[blocks.length][];
        for (int i = 0; i < blocks.length; i++) {
            clonedBlocks[i] = blocks[i].clone();
        }
        return new Block(clonedBlocks, this.hexColor, this.x, this.y);
    }

    public void moveDown() { y++; }
    public void moveUp() { y--; }
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

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
    public String getHexColor() { return hexColor; }
}