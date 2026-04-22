package seda_project.control_alt_defeat.gamebox.Memory;

import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

public class Configuration {

    public static void deckSize(int tupleSize, RadioButton smallGame, RadioButton mediumGame, RadioButton largeGame){
        int max = (int) 45/tupleSize;
        int stepsize = max/3;
        System.out.println("tuple size: "+tupleSize);
        System.out.println("max: "+max);
        System.out.println("stepsize: "+stepsize);
        if (max == 2){
            smallGame.setText(String.valueOf(tupleSize/2));
            mediumGame.setDisable(false);
            mediumGame.setText(String.valueOf(tupleSize));
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

    public static void changeScene(Parent root, VBox vBox){
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
