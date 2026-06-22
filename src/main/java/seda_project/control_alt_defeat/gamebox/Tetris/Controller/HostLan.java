package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;

import java.net.URL;
import java.util.ResourceBundle;

public class HostLan extends Controller implements Initializable {
    private TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    TextField hostNameTF;

    @FXML
    Label statusLabel;

    @FXML
    private ComboBox<Integer> yourLevel;

    @FXML
    private ToggleSwitch toggleSwitch;

    @FXML
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
    }

    @FXML
    private void onSearchAction(){
        sC.play("button");
        String yourName = c.checkNameInput(hostNameTF.getText(),1);
        int hostLevel = yourLevel.getSelectionModel().getSelectedItem();
        if (c.checkNameLength(yourName,1,statusLabel)){
            Session s = Session.current();
            s.myName = yourName;
            s.myLevel = hostLevel;
            s.isHost = true;
            advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
            WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
            controller.passHostData(yourName,hostLevel);
        }
        else{
            sC.play("error");
        }
    }

    public void handData(String hostName){
        this.hostNameTF.setText(hostName);
    }

    @FXML
    private void onAdvancedSettingsAction(){
        AdvancedSettings controller = (AdvancedSettings) c.changeScene("/Views/Tetris/AdvancedSettings.fxml",header,vS);

        String yourName = hostNameTF.getText();
        int hostLevel = yourLevel.getSelectionModel().getSelectedItem();
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());

        controller.handHostData(yourName,hostLevel);
        advancedSettings.setTwoBlocks(toggleSwitch.switchOnProperty().getValue());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusLabel.setVisible(false);

        yourLevel.getItems().clear();

        for (int i = 0; i < 20; i++) {
            yourLevel.getItems().add(i+1);
        }
        yourLevel.getSelectionModel().select(0);

        toggleSwitch.setSwitchedOn(advancedSettings.isTwoBlocks());
    }

    public void handPlayerData(String p1Name, int p1Level) {
        hostNameTF.setText(p1Name);
        yourLevel.getSelectionModel().select(p1Level-1);
    }
}
