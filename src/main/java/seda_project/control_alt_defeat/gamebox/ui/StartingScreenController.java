package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.GameBox;

public class StartingScreenController extends Controller {

    @FXML
    private VBox header;

    @FXML
    public void onExitAction() {
        sC.play("button");
        GameBox.cleanExit();
    }
    @FXML
    public void onMemoryAction() {
        sC.play("button");
        c.changeScene("/Views/Memory/MemoryMenu.fxml", header, vS);
    }
    @FXML
    public void onTetrisAction() {
        sC.play("button");
        c.changeScene("/Views/Tetris/TetrisMenu.fxml", header, vS);
    }

    @FXML
    protected void onHexChessAction(){
        sC.play("button");
        c.changeScene("/Views/HexChess/ChessMenu.fxml", header, vS);
    }
}