package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ChessMenu extends Controller {
    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction() {
        c.changeScene("/Views/HexChess/LocalGameConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction() {
        c.changeScene("/Views/HexChess/JoinLan.fxml",header,vS);
    }

    @FXML
    protected void onHostAction() {
        c.changeScene("/Views/HexChess/HostLanConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onExitAction() {
        c.backScene(header,vS);
    }

    @FXML
    protected void onSettingsAction() {
        c.changeScene("/Views/HexChess/ChessSettings.fxml",header,vS);
    }

    @FXML
    public void onCustomBoardAction() {
        c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
    }
}
