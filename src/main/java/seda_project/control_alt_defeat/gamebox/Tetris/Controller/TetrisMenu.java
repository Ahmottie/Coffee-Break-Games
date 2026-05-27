package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import javafx.css.PseudoClass;


public class TetrisMenu extends Controller {

    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction(){
        c.changeScene("/Views/Tetris/LocalGameConfiguration.fxml",header,vS);
    }
    @FXML
    protected void onHostAction(){
        c.changeScene("/Views/Tetris/HostLan.fxml",header,vS);
    }

    @FXML
    protected void onJoinAction(){
        c.changeScene("/Views/Tetris/JoinLan.fxml",header,vS);
    }

    @FXML
    protected void onSettingsAction(){
        c.changeScene("/Views/Tetris/SettingsMenu.fxml",header,vS);
    }

    @FXML
    protected void onPartDesignerAciton(){
        c.changeScene("/Views/Tetris/BlockCreator.fxml",header,vS);
    }

    @FXML
    protected void onExitAction(){
        c.backScene(header, vS);

    }
}
