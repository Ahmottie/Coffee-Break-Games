package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import seda_project.control_alt_defeat.gamebox.network.LanClient;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class JoinLan extends Controller implements Initializable {
    @FXML
    private Label joinStatus;

    @FXML
    private TextField joinPlayerNameTF, ipAdresseTF;

    @FXML
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        joinStatus.setVisible(false);
    }

    @FXML
    protected void onConnectAction(){
        sC.play("button");
        String yourName = c.checkNameInput(joinPlayerNameTF.getText(),2);
        if (c.checkNameLength(yourName,2,joinStatus)) {
            if (checkIP()) {
                try {
                    NetworkLayer layer = connectToHost(ipAdresseTF.getText());

                    Session s = Session.current();
                    s.myName  = yourName;
                    s.isHost  = false;
                    s.network = layer;


                    WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Memory/WaitForOpponent.fxml",header,vS);
                    controller.passJoinData(yourName, ipAdresseTF.getText());
                } catch (Exception e) {
                    joinStatus.setVisible(true);
                    joinStatus.setText("Could not connect: " + e.getMessage());
                    sC.play("error");
                }
            }
        }
        else {
            sC.play("error");
        }
    }

    private NetworkLayer connectToHost(String ipAddress) throws Exception {
        return LanClient.join(ipAddress, Lan.DEFAULT_PORT);
    }

    private boolean checkIP() {
        joinStatus.setVisible(false);
        String ipAdresse = ipAdresseTF.getText();
        if (ipAdresse.isEmpty()){
            sC.play("error");
            joinStatus.setVisible(true);
            joinStatus.setText("You need to fill in an IP-Address");
            return false;
        }
        else {
            String[] ipParts = ipAdresse.split("\\.");
            if (ipParts.length != 4){
                System.out.println(ipParts.length);
                joinStatus.setVisible(true);
                joinStatus.setText("You need to fill in a correct IP-Address");
                sC.play("error");
                return false;
            }

            for (String s : ipParts){
                int number = Integer.parseInt(s);
                if (number<0||number >255){
                    joinStatus.setVisible(true);
                    joinStatus.setText("The numbers of your IP-Address can only be in the range of 0 to 255!");
                    sC.play("error");
                    return false;
                }
            }
            if (ipAdresse.endsWith(".")){
                joinStatus.setVisible(true);
                joinStatus.setText("Your IP-Address may not end with a dot!");
                sC.play("error");
                return false;
            }
        }
        return true;
    }

    public void backTransfer(String joinName, String ipAddress){
        joinPlayerNameTF.setText(joinName);
        ipAdresseTF.setText(ipAddress);
    }
}
