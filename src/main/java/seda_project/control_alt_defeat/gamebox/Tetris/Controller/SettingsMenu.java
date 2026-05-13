package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsMenu extends Controller implements Initializable {
    TetrisSettings tS = TetrisSettings.getInstance();

    @FXML
    VBox header;

    @FXML
    Label player1Left,player1Right,player1Down,player1Up,player2Left,player2Right,player2Down,player2Up;

    @FXML
    protected void changePlayer1(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        int position = Integer.parseInt((String)clicked.getUserData());
        KeyCode input= tS.change(0,position);
        Label change = getPlayerLabel(0,position);
        change.setText(input.toString());

    }

    @FXML
    protected void changePlayer2(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        int position = Integer.parseInt((String)clicked.getUserData());
        KeyCode input= tS.change(1,position);
        Label change = getPlayerLabel(1,position);
        change.setText(input.toString());

    }

    @FXML
    protected void onBackAction(){
        c.backScene(header, vS);
    }

    private Label getPlayerLabel(int player, int position){
        switch (player){
            case 0:
                switch (position){
                    case 0:
                        return player1Left;
                    case 1:
                        return player1Right;
                    case 2:
                        return player1Down;
                    case 3:
                        return player1Up;
                }
                break;
            case 1:
                switch (position){
                    case 0:
                        return player2Left;
                    case 1:
                        return player2Right;
                    case 2:
                        return player2Down;
                    case 3:
                        return player2Up;
                }
                break;
        }
        return null;
    }

    private void updateKeys() {
        ArrayList<KeyCode> p1keys = tS.getPlayer1Keys();
        player1Left.setText(p1keys.get(0).toString());
        player1Right.setText(p1keys.get(1).toString());
        player1Down.setText(p1keys.get(2).toString());
        player1Up.setText(p1keys.get(3).toString());
        ArrayList<KeyCode> p2keys = tS.getPlayer2Keys();
        player2Left.setText(p2keys.get(0).toString());
        player2Right.setText(p2keys.get(1).toString());
        player2Down.setText(p2keys.get(2).toString());
        player2Up.setText(p2keys.get(3).toString());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateKeys();
    }
}
