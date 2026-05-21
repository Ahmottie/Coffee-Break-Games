package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class HostLan extends Controller implements Initializable {

    @FXML
    VBox header;

    @FXML
    TextField hostNameTF;

    @FXML
    Label statusLabel;

    @FXML
    private ComboBox<Integer> yourLevel;

    @FXML
    protected void onBackAction(){
        Session.clear();
        c.backScene(header,vS);
    }

    @FXML
    private void onSearchAction(){
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        int hostLevel = yourLevel.getSelectionModel().getSelectedItem();
        if (c.checkNameLength(yourName,1,statusLabel)){
            Session s = Session.current();
            s.myName = yourName;
            s.myLevel = hostLevel;
            s.isHost = true;

            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
            controller.passHostData(yourName,hostLevel);
        }
    }

    public void handData(String hostName){
        this.hostNameTF.setText(hostName);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabel.setVisible(false);

        yourLevel.getItems().clear();

        for (int i = 0; i < 20; i++) {
            yourLevel.getItems().add(i+1);
        }
        yourLevel.getSelectionModel().select(0);
    }
}
