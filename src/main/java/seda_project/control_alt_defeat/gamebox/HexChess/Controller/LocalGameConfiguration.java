package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class LocalGameConfiguration extends Controller {
    private String boardState;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private VBox header;

    @FXML
    private Label statusLabel;

    @FXML
    public void onBackAction() {
        c.backScene(header, vS);
    }

    @FXML
    public void onStartAction() {
        //TODO Initialize Engine and change to GameScreen

        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        if (c.checkNameLength(player1Name,1, statusLabel) && c.checkNameLength(player2Name,2,statusLabel)) {
            GameScreen controller = (GameScreen) c.changeScene("/Views/HexChess/GameScreen.fxml", header, vS);
            if (boardState == null) {
                controller.init();
            }
            else{
                controller.init(boardState);
            }
            controller.setNames(player1Name, player2Name);
            controller.setPoints(0, 0);
        }
    }

    @FXML
    public void onCustomBoardAction() {
        BoardDesigner controller = (BoardDesigner) c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
        controller.handNames(player1TF.getText(),player2TF.getText());
    }



    public void boardSelection(String notation, String p1Name, String p2Name) {
        boardState = notation;
        if (p1Name != null){
            player1TF.setText(p1Name);
        }
        if (p2Name != null){
            player2TF.setText(p2Name);
        }
    }
}
