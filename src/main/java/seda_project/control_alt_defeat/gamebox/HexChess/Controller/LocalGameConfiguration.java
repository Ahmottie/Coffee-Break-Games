package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class LocalGameConfiguration extends Controller {
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
        c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
    }
}
