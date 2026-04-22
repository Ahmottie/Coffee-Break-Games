package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

public class JoinLan {
    ViewStack vS = GameBox.getvS();
    int c = 0;

    @FXML
    private VBox header;

    @FXML
    private Label joinStatus;

    @FXML
    private TextField joinPlayerName;

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

    @FXML
    private void onConnectAction(){
        //TODO Check for valid IP-Adress
        if (c == 0){
            joinStatus.setText("Clicked " +c +" times");
            c++;
        }

        else if (c == 1){
            joinStatus.setText("Wrong IP");
            c++;
        }
        else {
            try {
                String address = "/Views/Memory/WaitForOpponent.fxml";
                FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
                Parent root = loader.load();
                WaitForOpponent controller = loader.getController();

                vS.addFxmlLoaders(address);
                boolean host = false;
                String name = joinPlayerName.getText();
                controller.passJoinData(vS, host, name);

                Scene newScene = new Scene(root, 800, 600);
                Stage stage = (Stage) header.getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }

    public void backTransfer(String joinName){
        joinPlayerName.setText(joinName);
    }
}
