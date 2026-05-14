package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;

public record CustomBlock(boolean[][] shape, Color color) implements TBlock {
        @Override
        public boolean[][] getShape() {
            return shape;
        }
        @Override
        public Color getColor() {
            return color;
        }
        @Override
        public Block toPiece() {
            return new Block(shape, color);
        }
}
