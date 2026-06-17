package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ChessMenu extends Controller {
    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/LocalGameConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/JoinLan.fxml",header,vS);
    }

    @FXML
    protected void onHostAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/HostLanConfiguration.fxml",header,vS);
    }

    @FXML
    protected void onExitAction() {
        sC.play("button");
        c.backScene(header,vS);
    }

    @FXML
    protected void onSettingsAction() {
        sC.play("button");
        c.changeScene("/Views/HexChess/ChessSettings.fxml",header,vS);
    }

    @FXML
    public void onCustomBoardAction() {
        sC.play("button");
        BoardDesigner controller = (BoardDesigner)c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
        controller.disableUse();
    }
}
