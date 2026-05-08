package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.network.LanClient;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.Session;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JoinLan implements Initializable {
    ViewStack vS;
    Configuration c;
    TetrisSettings tS;
    ArrayList<Label> availableHosts = new ArrayList<>();

    @FXML
    VBox header,scrollElements;

    @FXML
    private TextField joinPlayerNameTF;

    @FXML
    private Label joinStatus,selectedHost;

    @FXML
    public void onBackAction(){
        TetrisMenu controller = (TetrisMenu) c.backScene(header,vS);
        controller.handViewStack(vS,c);
        controller.handSettings(tS);
    }

    @FXML
    public void onConnectAction(){
        //TODO The Data of the before selected Label should be used to Connect to the Host
        // If this works fine then the scene is changed to "WaitForOpponent"
        if (selectedHost == null){
            joinStatus.setVisible(true);
            joinStatus.setText("Select a Game to join!");
        }
        else {
            String yourName = c.checkNameInput(joinPlayerNameTF.getText(),2);
            if (c.checkNameLength(yourName,2,joinStatus)) {
                String ipAddresse = selectedHost.getText().split(": ")[1];
                try {
                    NetworkLayer layer = connectToHost(ipAddresse);
                    Session s = Session.current();
                    s.myName  = yourName;
                    s.isHost  = false;
                    s.network = layer;

                    WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Tetris/WaitForOpponent.fxml",header,vS);
                    controller.passJoinData(tS,yourName,ipAddresse);
                    controller.handViewStack(vS,c);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                joinStatus.setVisible(true);
                joinStatus.setText("Your name may not be longer than 16 character!");
            }
        }
    }

    private NetworkLayer connectToHost(String ipAddress) throws Exception {
        return LanClient.join(ipAddress, Lan.DEFAULT_PORT);
    }

    public void handViewStack(ViewStack vs, Configuration c){
        this.vS = vs;
        this.c = c;
    }

    public void handSettings(TetrisSettings tS){
        this.tS = tS;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        joinStatus.setVisible(false);
        for (int i = 0; i < 20; i++) {
            addLabel("NameSpace: " + i);
        }
        //TODO Network Stuff To get all open Network connections
        // Each Host should be represented as a Label with the Name of the Host and the IP-Address
        // If clicked on a Label this Label should be highlighted.
    }

    public void addLabel(String labelText){
        Label l = new Label(labelText);
        l.setMinWidth(570);
        l.setAlignment(Pos.CENTER);
        availableHosts.add(l);
        scrollElements.getChildren().add(l);
        l.setOnMouseClicked(mouseEvent -> {
            selectedHost = l;
            deselectAll();
            l.getStyleClass().add("box");
            l.getStyleClass().add("ready");
        });
    }

    private void deselectAll() {
        for (int i = 0; i < availableHosts.size(); i++) {
            Label l = availableHosts.get(i);
            l.getStyleClass().clear();
        }
    }

}
