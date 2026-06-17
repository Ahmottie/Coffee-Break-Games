package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;


public class TetrisMenu extends Controller {

    @FXML
    protected void onLocalAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/LocalGameConfiguration.fxml",header,vS);
    }
    @FXML
    protected void onHostAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/HostLan.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/JoinLan.fxml",header,vS);
    }

    @FXML
    protected void onSettingsAction(){
        sC.play("button");
        c.changeScene("/Views/Tetris/SettingsMenu.fxml",header,vS);
    }

    @FXML
    protected void onPartDesignerAciton(){
        sC.play("button");
        c.changeScene("/Views/Tetris/BlockCreator.fxml",header,vS);
    }

    @FXML
    protected void onExitAction(){
        sC.play("button");
        c.backScene(header, vS);

    }
}
