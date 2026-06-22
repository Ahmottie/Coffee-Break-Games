package seda_project.control_alt_defeat.gamebox.ui;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

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

        this.getStyleClass().remove("button");
        this.getStyleClass().add("card");

        Rectangle clip = new Rectangle();
        clip.setArcWidth(50);  // Creates a 10px corner radius to match your CSS border
        clip.setArcHeight(50);
        clip.widthProperty().bind(this.widthProperty());
        clip.heightProperty().bind(this.heightProperty());
        this.setClip(clip);

        BackgroundSize backgroundSize = new BackgroundSize(
                BackgroundSize.AUTO,
                BackgroundSize.AUTO,
                false,
                false,
                false,
                true
        );

        String back = getClass().getResource("/Images/Memory/backface.png").toExternalForm();
        String front = getClass().getResource("/Images/Memory/"+id+".png").toExternalForm();
        cardBack = new Background(new BackgroundImage(new Image(back,true), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        cardFront = new Background(new BackgroundImage(new Image(front,true),  BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize));
        this.setBackground(cardBack);
    }

    public void setHeightProporties(double height, double width){
        this.heightProperty().add(height);
        this.widthProperty().add(width);
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
