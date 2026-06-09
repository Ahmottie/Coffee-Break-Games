package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class BombBlock extends Block implements Serializable {
    private BombType type;

    public BombBlock(BombType type) {
        super(new boolean[][] {{true}}, Color.WHITE);
        this.type = type;
    }

    @Override
    public Block cloneForSnapshot() {
        BombBlock clone = new BombBlock(this.type);
        clone.setX(this.getX());
        clone.setY(this.getY());
        clone.setShape(this.getShape().clone());
        return clone;
    }

    public BombType getType(){
        return this.type;
    }

}
