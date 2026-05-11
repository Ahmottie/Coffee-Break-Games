package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;

public class LocalGameConfiguration {
    private ViewStack vS;
    private Configuration c;
    private TetrisSettings tS;

    @FXML
    private VBox header;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onBackAction(ActionEvent actionEvent) {
        TetrisMenu controller = (TetrisMenu) c.backScene(header,vS);
        controller.handSettings(tS);
        controller.handViewStack(vS,c);
    }

    @FXML
    protected void onStartAction(ActionEvent actionEvent) {
        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        if (c.checkNameLength(player1Name,1,statusLabel) && c.checkNameLength(player2Name,2,statusLabel)){
            //TODO Move to GameScreen and start the Game
            //c.changeScene()
        }
    }

    public void handViewStack(ViewStack vs, Configuration c){
        this.vS = vs;
        this.c = c;
    }

    public void handSettings(TetrisSettings tS){
        this.tS = tS;
    }


}
