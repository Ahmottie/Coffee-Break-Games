package seda_project.control_alt_defeat.gamebox.Memory;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

public class Configuration {

    public void deckSize(int tupleSize, RadioButton smallGame, RadioButton mediumGame, RadioButton largeGame){
        int max = (int) 45/tupleSize;
        int stepsize = max/3;
        System.out.println("tuple size: "+tupleSize);
        System.out.println("max: "+max);
        System.out.println("stepsize: "+stepsize);
        if (max == 2){
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max-stepsize)*tupleSize));
            largeGame.setDisable(true);
            largeGame.setText("---");
        }
        else if (max == 1){
            smallGame.setText(String.valueOf(tupleSize));
            mediumGame.setDisable(true);
            mediumGame.setText("---");
            largeGame.setDisable(true);
            largeGame.setText("---");
        }
        else {
            largeGame.setText(String.valueOf(max*tupleSize));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf((max-stepsize)*tupleSize));
            largeGame.setDisable(false);
            smallGame.setText(String.valueOf((max-2*stepsize)*tupleSize));
        }
    }

    public boolean checkNameLength(String name, int player, Label statusLabel) {
        int max = 16;
        int length = name.length();
        if (length > max){
            statusLabel.setVisible(true);
            statusLabel.setText("The name of player " + player +" may not be longer than "+max+" characters!");
            return false;
        }
        return true;
    }
    public String checkNameInput(String name, int player){
        if (name.equals("")){
            name = "Player " + player;
        }
        return name;
    }

    public int ActivePlayer(){
        //TODO create function that either returns 1 or 2
        return 1;
    }


}
