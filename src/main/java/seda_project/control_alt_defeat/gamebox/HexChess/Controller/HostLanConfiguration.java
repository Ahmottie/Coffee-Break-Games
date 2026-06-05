package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class HostLanConfiguration  extends Controller {
    @FXML
    private VBox header;

    @FXML
    private TextField hostNameTF;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onSearchAction() {
        //TODO Change Scene and Lan Stuff
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        if (c.checkNameLength(yourName,1,statusLabel)) {
            Session s = Session.current();
            s.myName = yourName;
            s.isHost = true;
            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/HexChess/WaitForOpponent.fxml", header, vS);
            controller.passHostData(yourName);
        }
    }


    @FXML
    protected void onBackAction() {
        Session.clear();
        c.backScene(header,vS);
    }

    public void handData(String name){
        hostNameTF.setText(name);
    }
}
