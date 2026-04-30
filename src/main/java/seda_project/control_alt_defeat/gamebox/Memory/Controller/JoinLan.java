package seda_project.control_alt_defeat.gamebox.Memory.Controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.network.LanClient;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.network.Lan;

import java.net.URL;
import java.util.ResourceBundle;

public class JoinLan implements Initializable {
    ViewStack vS;
    Configuration c = new Configuration();

    @FXML
    private VBox header;

    @FXML
    private Label joinStatus;

    @FXML
    private TextField joinPlayerNameTF, ipAdresseTF;

    @FXML
    private void onBackAction(){
        try{
            vS.popFxmlLoader();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(vS.getFxmlLoader()));
            Parent root = loader.load();
            MemoryMenu controller = loader.getController();
            controller.handViewStack(vS);
            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        joinStatus.setVisible(false);
    }

    @FXML
    private void onConnectAction(){
        String yourName = c.checkNameInput(joinPlayerNameTF.getText(),2);
        if (c.checkNameLength(yourName,2,joinStatus)) {
            if (checkIP()) {
                try {
                    NetworkLayer layer = connectToHost(ipAdresseTF.getText());

                    Session s = Session.current();
                    s.myName  = yourName;
                    s.isHost  = false;
                    s.network = layer;

                    String address = "/Views/Memory/WaitForOpponent.fxml";
                    FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
                    Parent root = loader.load();
                    WaitForOpponent controller = loader.getController();

                    vS.addFxmlLoaders(address);
                    boolean host = false;
                    controller.passJoinData(vS, host, yourName, ipAdresseTF.getText());

                    Scene newScene = new Scene(root, 800, 600);
                    Stage stage = (Stage) header.getScene().getWindow();
                    stage.setScene(newScene);
                    stage.show();
                } catch (Exception e) {
                    joinStatus.setVisible(true);
                    joinStatus.setText("Could not connect: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private NetworkLayer connectToHost(String ipAddress) throws Exception {
        return LanClient.join(ipAddress, Lan.DEFAULT_PORT);
    }

    private boolean checkIP() {
        joinStatus.setVisible(false);
        String ipAdresse = ipAdresseTF.getText();
        if (ipAdresse.equals("")){
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
                return false;
            }

            for (String s : ipParts){
                int number = Integer.parseInt(s);
                if (number<0||number >255){
                    joinStatus.setVisible(true);
                    joinStatus.setText("The numbers of your IP-Address can only be in the range of 0 to 255!");
                    return false;
                }
            }
            if (ipAdresse.endsWith(".")){
                joinStatus.setVisible(true);
                joinStatus.setText("Your IP-Address may not end with a dot!");
                return false;
            }
        }
        return true;
    }

    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }

    public void backTransfer(String joinName, String ipAddress){
        joinPlayerNameTF.setText(joinName);
        ipAdresseTF.setText(ipAddress);
    }
}
