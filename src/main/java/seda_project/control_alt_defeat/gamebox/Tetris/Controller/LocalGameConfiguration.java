package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class LocalGameConfiguration extends Controller {

    @FXML
    private VBox header;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onBackAction() {
        c.backScene(header,vS);
    }

    @FXML
    protected void onStartAction() {
        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        if (c.checkNameLength(player1Name,1,statusLabel) && c.checkNameLength(player2Name,2,statusLabel)){
            //TODO Move to GameScreen and start the Game
            GameScreen controller = (GameScreen) c.changeScene("/Views/Tetris/GameScreen.fxml",header,vS);
            controller.create(player1Name,player2Name,false);
        }
    }
}
