package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class HostLan extends Controller {

    @FXML
    VBox header;

    @FXML
    TextField hostNameTF;

    @FXML
    Label statusLabel;

    @FXML
    protected void onBackAction(){
        Session.clear();
        c.backScene(header,vS);
    }

    @FXML
    private void onSearchAction(){
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        if (c.checkNameLength(yourName,1,statusLabel)){
            Session s = Session.current();
            s.myName = yourName;
            s.isHost = true;

            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
            controller.passHostData(yourName);
        }
    }

    public void handData(String hostName){
        this.hostNameTF.setText(hostName);
    }
}
