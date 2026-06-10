package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    public void onBackAction() {
        c.backScene(header, vS);
    }

    @FXML
    public void onStartAction() {
        //TODO Initialize Engine and change to GameScreen
        c.changeScene("/Views/HexChess/GameScreen.fxml",header,vS);
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
