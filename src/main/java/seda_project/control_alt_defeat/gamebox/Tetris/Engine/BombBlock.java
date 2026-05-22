package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;

public class BombBlock extends Block {
    private ImagePattern background;
    private BombType type;

    public BombBlock(BombType type) {
        super(new boolean[][] {{true}}, Color.WHITE);
        this.type = type;
        Image i = (type == BombType.RADIUS)? loadImage("RadialBomb") :loadImage("ColumnBomb");
        background = new ImagePattern(i);

    }

    private Image loadImage(String type) {
        var stream = getClass().getResourceAsStream("/Images/Tetris/"+type+".png");
        if (stream != null) {
            return new Image(stream);
        }
        System.err.println("WARNING: swap.png not found in resources. Using color fallback.");
        return null;
    }
    public ImagePattern getImagePattern (){
        return this.background;
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
