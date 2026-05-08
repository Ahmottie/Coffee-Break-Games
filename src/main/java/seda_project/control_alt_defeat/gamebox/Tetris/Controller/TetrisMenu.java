package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;


public class TetrisMenu {
    private ViewStack vS;
    private Configuration c;
    private TetrisSettings tS;

    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction(){
        Object controller = c.changeScene("/Views/Memory/LocalGameConfiguration.fxml",header,vS);
    }
    @FXML
    protected void onHostAction(){
        HostLan controller = (HostLan) c.changeScene("/Views/Tetris/HostLan.fxml",header,vS);
        controller.handViewStack(vS,c);
        controller.handSettings(tS);
    }

    @FXML
    protected void onJoinAction(){
        JoinLan controller = (JoinLan) c.changeScene("/Views/Tetris/JoinLan.fxml",header,vS);
        controller.handViewStack(vS,c);
        controller.handSettings(tS);
    }

    @FXML
    protected void onSettingsAction(){
        SettingsMenu controller = (SettingsMenu) c.changeScene("/Views/Tetris/SettingsMenu.fxml",header,vS);
        controller.handViewStack(vS,c);
        controller.handSettings(tS);
    }

    @FXML
    protected void onPartDesignerAciton(){
        //TODO
    }

    @FXML
    protected void onExitAction(){
        GameBox controller = (GameBox) c.backScene(header, vS);
        controller.handViewStack(vS,c);

    }

    public void handViewStack(ViewStack vs, Configuration c){
        this.vS = vs;
        this.c = c;
    }

    public void handSettings(TetrisSettings tS){
        this.tS = tS;
    }
}
