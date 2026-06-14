package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResultScreen {
    @FXML
    private Label p1NameLabel, p2NameLabel, p1Score, p2Score, p1Awarded, p2Awarded, resultLabel;

    public void handData(String p1Name, String p2Name, String p1Points, String p2Points){
        p1NameLabel.setText(p1Name);
        p2NameLabel.setText(p2Name);
        p1Score.setText(""+p1Points);
        p2Score.setText(""+p2Points);
    }

    public void winner(int winner){
        resultLabel.setText("Checkmate");
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.valueOf(p1Score.getText()) + 1.0));
            p1Awarded.setText("+1.0");
            p2Awarded.setText("+0.0");
        } else {
            p2Score.setText(String.valueOf(Double.valueOf(p2Score.getText()) + 1.0));
            p1Awarded.setText("+0.0");
            p2Awarded.setText("+1.0");
        }
    }

    public void remis(){
        resultLabel.setText("Remis");
        p1Score.setText(String.valueOf(Double.valueOf(p1Score.getText()) + .5));
        p2Score.setText(String.valueOf(Double.valueOf(p2Score.getText()) + .5));
        p1Awarded.setText("+0.5");
        p2Awarded.setText("+0.5");
    }

    public void stalemate(int winner){
        resultLabel.setText("Stalemate");
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.valueOf(p1Score.getText()) + .75));
            p2Score.setText(String.valueOf(Double.valueOf(p2Score.getText()) + .25));
            p1Awarded.setText("+0.75");
            p2Awarded.setText("+0.25");
        } else {
            p2Score.setText(String.valueOf(Double.valueOf(p2Score.getText()) + .75));
            p1Score.setText(String.valueOf(Double.valueOf(p1Score.getText()) + .25));
            p1Awarded.setText("+0.25");
            p2Awarded.setText("+0.75");
        }
    }

    public void onExitAction(ActionEvent actionEvent) {

    }

    public void onPlayAgainAction(ActionEvent actionEvent) {
    }
}
