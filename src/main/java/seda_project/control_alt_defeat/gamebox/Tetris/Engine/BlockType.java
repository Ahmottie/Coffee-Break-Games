package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
public enum BlockType implements TBlock {

    I(Color.AQUA, new boolean[][] {
            { false, false, true, false },
            { false, false, true, false },
            { false, false, true, false },
            { false, false, true, false }
    }),
    O(Color.RED, new boolean[][] {
            { true, true },
            { true, true }
    }),
    T(Color.GREEN, new boolean[][] {
            { true, true,  true },
            { false, true,  false },
            {false,false,false,false}
    }),
    L(Color.ORANGE, new boolean[][] {
            { true,  false, false },
            { true,  false, false },
            { true,  true,  false }
    }),
    L_INV(Color.PINK, new boolean[][] {
            { false, false, true },
            { false, false, true },
            { false, true,  true }
    }),
    Z(Color.PURPLE, new boolean[][] {
            { false, false, false },
            { true,  true,  false },
            { false, true,  true  }
    }),
    Z_INV(Color.YELLOW, new boolean[][] {
            { false, false, false },
            { false, true,  true  },
            { true,  true,  false }
    });

    public final Color color;
    public final boolean[][] shape;

    BlockType(Color color, boolean[][] shape) {
        this.color = color;
        this.shape = shape;
    }

    public static ArrayList<Block> createStandardPieces() {
        return Arrays.stream(values())
                .map(BlockType::toPiece)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    @Override
    public boolean[][] getShape() {
        return shape;
    }

    @Override public Color getColor() {
        return color;
    }

    @Override
    public Block toPiece() {
        return new Block(shape, color);
    }
}
