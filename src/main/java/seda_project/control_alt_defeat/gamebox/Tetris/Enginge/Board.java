package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

public class Board {
    private final int width = 10;
    private final int height = 20;
    private final boolean[][] grid; // 0 means empty, >0 means occupied by a block type
    private final boolean inverted;

    public Board(boolean inverted) {
        this.grid = new boolean[height][width];
        this.inverted = inverted;
    }

    // Checks if the block can exist at its current internal x,y coordinates
    public boolean isValidPosition(Block block) {
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != false) {
                    int boardX = bx + col;
                    int boardY = by + row;

                    // Check boundaries
                    if (boardX < 0 || boardX >= width || boardY < 0 || boardY >= height) {
                        return false;
                    }
                    // Check collision with locked blocks
                    if (grid[boardY][boardX] != false) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Locks the block permanently into the grid
    public void lockBlock(Block block) {
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] != false) {
                    grid[by + row][bx + col] = true;
                }
            }
        }
    }

    public int clearLines() {
        int linesCleared = 0;

        // Standard board checks bottom-to-top, Inverted checks top-to-bottom
        int startRow = inverted ? 0 : height - 1;
        int endRow = inverted ? height : -1;
        int step = inverted ? 1 : -1;

        for (int row = startRow; row != endRow; row += step) {
            if (isLineFull(row)) {
                linesCleared++;
                shiftGrid(row);
                row -= step; // Re-evaluate current row index after shift
            }
        }
        return linesCleared;
    }

    private boolean isLineFull(int row) {
        for (int col = 0; col < width; col++) {
            if (grid[row][col] == false) return false;
        }
        return true;
    }

    private void shiftGrid(int targetRow) {
        if (inverted) {
            // Shift blocks up
            for (int row = targetRow; row < height - 1; row++) {
                System.arraycopy(grid[row + 1], 0, grid[row], 0, width);
            }
            java.util.Arrays.fill(grid[height - 1], false);
        } else {
            // Shift blocks down
            for (int row = targetRow; row > 0; row--) {
                System.arraycopy(grid[row - 1], 0, grid[row], 0, width);
            }
            java.util.Arrays.fill(grid[0], false);
        }
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public boolean isInverted() {
        return inverted;
    }
}