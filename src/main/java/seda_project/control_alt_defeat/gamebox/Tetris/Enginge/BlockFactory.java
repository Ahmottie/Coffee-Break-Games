package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import java.util.Random;

// generation of standard blocks + custome ones
public class BlockFactory {
    private static final Random RANDOM = new Random();

    // Standard shapes mapped to unique integer IDs (1-7)
    private static final int[][][] STANDARD_SHAPES = {
            {{1, 1, 1, 1}},                         // I (ID: 1)
            {{2, 0, 0}, {2, 2, 2}},                 // J (ID: 2)
            {{0, 0, 3}, {3, 3, 3}},                 // L (ID: 3)
            {{4, 4}, {4, 4}},                       // O (ID: 4)
            {{0, 5, 5}, {5, 5, 0}},                 // S (ID: 5)
            {{0, 6, 0}, {6, 6, 6}},                 // T (ID: 6)
            {{7, 7, 0}, {0, 7, 7}}                  // Z (ID: 7)
    };

    public static Block generateRandomStandardBlock() {
        int index = RANDOM.nextInt(STANDARD_SHAPES.length);
        int[][] shape = copyMatrix(STANDARD_SHAPES[index]);
        return new Block(shape, index + 1);
    }

    public static Block generateCustomBlock(int[][] customShape, int typeId) {
        return new Block(copyMatrix(customShape), typeId);
    }

    // Prevents unintended modification of base templates by reference
    private static int[][] copyMatrix(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}