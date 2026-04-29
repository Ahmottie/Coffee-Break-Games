package seda_project.control_alt_defeat.gamebox.Memory.engine;

import javafx.scene.control.Button;

public class MCard extends Button {
    int id;
    boolean faceUp;
    boolean removed;
    int x;
    int y;

    public MCard(int x, int y,int id) {
        this.id = id;
        this.x = x;
        this.y = y;

        this.faceUp = false;
        this.removed = false;

        this.setText("?");
    }

    public void faceDown(){
        this.faceUp = false;
        this.setText("?");
        this.setDisable(false);
    }

    public int getid(){
        return this.id;
    }

    public void setFaceUp(boolean b) {
        this.faceUp = b;
        System.out.println("Change to " +b);
    }

    public boolean getFaceUp(){
        return this.faceUp;
    }
}
