package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {

    @FXML
    private VBox header;

    @FXML
    protected void onPlayAgainAction(){

    }

    @FXML
    protected void onExitGameAction(){
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }
}
