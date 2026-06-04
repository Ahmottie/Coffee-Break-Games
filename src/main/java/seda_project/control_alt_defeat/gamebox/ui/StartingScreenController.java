package seda_project.control_alt_defeat.gamebox.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.GameBox;

public class StartingScreenController extends Controller {

    @FXML
    private VBox header;

    @FXML
    public void onExitAction() {
        GameBox.cleanExit();
    }
    @FXML
    public void onMemoryAction() {
        c.changeScene("/Views/Memory/MemoryMenu.fxml", header, vS);
    }
    @FXML
    public void onTetrisAction() {
        c.changeScene("/Views/Tetris/TetrisMenu.fxml", header, vS);
    }

    @FXML
    protected void onHexChessAction(){
        c.changeScene("/Views/HexChess/ChessMenu.fxml", header, vS);
    }
}