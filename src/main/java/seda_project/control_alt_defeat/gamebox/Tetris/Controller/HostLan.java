package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;
import seda_project.control_alt_defeat.gamebox.network.Session;

public class HostLan {
    ViewStack vS;
    Configuration c;
    TetrisSettings tS;

    @FXML
    VBox header;

    @FXML
    TextField hostNameTF;

    @FXML
    Label statusLabel;

    @FXML
    public void onBackAction(){
        Session.clear();
        TetrisMenu controller = (TetrisMenu) c.backScene(header,vS);
        controller.handSettings(tS);
        controller.handViewStack(vS,c);
    }

    @FXML
    public void onSearchAction(){
        //TODO Open the Network Connection
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        if (c.checkNameLength(yourName,1,statusLabel)){
            Session s = Session.current();
            s.myName = yourName;
            s.isHost = true;

            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
            controller.handViewStack(vS,c);
            controller.passHostData(tS,hostNameTF.getText());
        }
    }

    public void handViewStack(ViewStack vs, Configuration c){
        this.vS = vs;
        this.c = c;
    }
    public void handSettings(TetrisSettings tS){
        this.tS = tS;
    }
    public void handData(String hostName){
        this.hostNameTF.setText(hostName);
    }
}
