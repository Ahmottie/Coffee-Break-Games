package seda_project.control_alt_defeat.gamebox.Memory.engine;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class MCard extends Button {
    int id;
    boolean faceUp;
    boolean removed;
    int x;
    int y;
    Background cardBack;
    Background cardFront;

    public MCard(int x, int y,int id) {
        this.id = id;
        this.x = x;
        this.y = y;

        this.faceUp = false;
        this.removed = false;

        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO,
                BackgroundSize.AUTO,
                false,
                false,
                true,
                false
        );

        String back = getClass().getResource("/Images/Memory/backface.png").toExternalForm();
        String front = getClass().getResource("/Images/Memory/"+id+".png").toExternalForm();
        cardBack = new Background(new BackgroundImage(new Image(back,true), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        cardFront = new Background(new BackgroundImage(new Image(front,true),  BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        this.setBackground(cardBack);
    }

    public void faceDown(){
        this.faceUp = false;
        this.setBackground(cardBack);
        this.setDisable(false);
    }

    public int getid(){
        return this.id;
    }

    public void setFaceUp(boolean b) {
        this.faceUp = b;
        this.setBackground(cardFront);
        this.setDisable(true);
    }

    public boolean getFaceUp(){
        return this.faceUp;
    }
}
