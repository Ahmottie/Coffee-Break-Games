package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.scene.paint.Color;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.CustomBlock;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.BlockRegistry;

import java.util.Arrays;

public class BlockEditorController {
    private static final int MAX_GRID_SIZE = 4;

    private final boolean[][] grid = new boolean[MAX_GRID_SIZE][MAX_GRID_SIZE];
    private Color selectedColor = Color.WHITE;
    private final BlockRegistry registry;
    private CustomBlock pieceToUpdate = null    ;

    public BlockEditorController(BlockRegistry registry) {
        this.registry = registry;
    }

    public void toggleCell(int row, int col) {
        grid[row][col] = !grid[row][col];
    }

    public void setColor(Color color) {
        this.selectedColor = color;
    }

    public void savePiece() {
        boolean[][] trimmed = trimGrid(grid);
        if (isEmpty(trimmed)) throw new IllegalStateException("Piece cannot be empty");
        registry.addCustomPiece(trimmed, selectedColor);
    }

    public void loadForUpdate(CustomBlock customBlock) {
        this.pieceToUpdate = customBlock;
    }

    public void updatePiece(){
        boolean[][] trimmed = trimGrid(grid);
        if (isEmpty(trimmed)) throw new IllegalStateException("Piece cannot be empty");
        registry.updateCustomPiece(trimmed, selectedColor, pieceToUpdate);
        pieceToUpdate = null;
    }

    public void reset() {
        for (boolean[] row : grid) Arrays.fill(row, false);
    }

    private boolean[][] trimGrid(boolean[][] g) {
        int minRow = MAX_GRID_SIZE, maxRow = 0, minCol = MAX_GRID_SIZE, maxCol = 0;
        for (int r = 0; r < MAX_GRID_SIZE; r++)
            for (int c = 0; c < MAX_GRID_SIZE; c++)
                if (g[r][c]) {
                    minRow = Math.min(minRow, r); maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c); maxCol = Math.max(maxCol, c);
                }
        int rows = maxRow - minRow + 1, cols = maxCol - minCol + 1;
        boolean[][] trimmed = new boolean[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                trimmed[r][c] = g[r + minRow][c + minCol];
        return trimmed;
    }

    private boolean isEmpty(boolean[][] g) {
        for (boolean[] row : g)
            for (boolean cell : row)
                if (cell) return false;
        return true;
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public boolean checkEmpty() {
        return isEmpty(this.grid);
    }
}
