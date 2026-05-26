package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;
import java.util.Arrays;

public class Board implements Serializable {
    private int width = 10;
    private int height = 20;
    private String[][] grid; // null means empty, String holds hex color
    private final boolean inverted;

    public Board(boolean inverted) {
        this.grid = new String[height][width];
        this.inverted = inverted;
    }

    public boolean isValidPosition(Block block) {
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int boardX = bx + col;
                    int boardY = by + row;

                    if (boardX < 0 || boardX >= width || boardY < 0 || boardY >= height) {
                        return false;
                    }
                    if (grid[boardY][boardX] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void lockBlock(Block block) {
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    grid[by + row][bx + col] = block.getHexColor();
                }
            }
        }
    }

    public void overwriteGrid(String[][] newGrid) {
        int newHeight = newGrid.length;
        int newWidth  = newGrid[0].length;
        this.grid   = new String[newHeight][newWidth];
        this.height = newHeight;
        this.width  = newWidth;
        for (int i = 0; i < newHeight; i++) {
            this.grid[i] = newGrid[i].clone();
        }
    }

    public int clearLines() {
        int linesCleared = 0;
        int startRow = inverted ? 0 : height - 1;
        int endRow = inverted ? height : -1;
        int step = inverted ? 1 : -1;

        for (int row = startRow; row != endRow; row += step) {
            if (isLineFull(row)) {
                linesCleared++;
                shiftGrid(row);
                row -= step;
            }
        }
        return linesCleared;
    }

    private boolean isLineFull(int row) {
        for (int col = 0; col < width; col++) {
            if (grid[row][col] == null) return false;
        }
        return true;
    }

    private void shiftGrid(int targetRow) {
        if (inverted) {
            for (int row = targetRow; row < height - 1; row++) {
                System.arraycopy(grid[row + 1], 0, grid[row], 0, width);
            }
            Arrays.fill(grid[height - 1], null);
        } else {
            for (int row = targetRow; row > 0; row--) {
                System.arraycopy(grid[row - 1], 0, grid[row], 0, width);
            }
            Arrays.fill(grid[0], null);
        }
    }

    public void clear() {
        for (int row = 0; row < height; row++) {
            Arrays.fill(grid[row], null);
        }
    }

    public void expand(int rows) {
        int newHeight = height + rows;
        String[][] newGrid = new String[newHeight][width];

        if (inverted) {
            // blocks fall upward, active space is at the bottom — preserve existing rows at top
            for (int i = 0; i < height; i++) {
                newGrid[i] = grid[i].clone();
            }
            // fill new empty rows at the bottom
            for (int i = height; i < newHeight; i++) {
                newGrid[i] = new String[width];
            }
        } else {
            // blocks fall downward, active space is at the top — preserve existing rows at bottom
            int offset = newHeight - height;
            for (int i = 0; i < height; i++) {
                newGrid[i + offset] = grid[i].clone();
            }
            // fill new empty rows at the top
            for (int i = 0; i < offset; i++) {
                newGrid[i] = new String[width];
            }
        }
        this.grid = newGrid;
        this.height = newHeight;
    }

    public void shrink(int rows) {
        if (rows >= height) throw new IllegalArgumentException("Cannot shrink below 1 row");
        int newHeight = height - rows;
        String[][] newGrid = new String[newHeight][width];

        if (inverted) {
            // drop rows from the bottom
            for (int i = 0; i < newHeight; i++) {
                newGrid[i] = grid[i].clone();
            }
        } else {
            // drop rows from the top
            int offset = height - newHeight;
            for (int i = 0; i < newHeight; i++) {
                newGrid[i] = grid[i + offset].clone();
            }
        }


        this.grid = newGrid;
        this.height = newHeight;
    }

    public String[][] getGrid() { return grid; }
    public boolean isInverted() { return inverted; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}