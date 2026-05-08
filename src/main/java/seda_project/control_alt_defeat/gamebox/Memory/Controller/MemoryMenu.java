package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

public class MemoryMenu {
    ViewStack vS;
    Configuration c;

    @FXML
    private VBox header;

    @FXML
    protected void onLocalAction(){
        try{
            String address = "/Views/Memory/LocalGameConfiguration.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            LocalGameConfiguration controller = loader.getController();

            vS.addFxmlLoaders(address);
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
    protected void onHostAction(){
        try{
            String address = "/Views/Memory/HostLanConfiguration.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            HostLan controller = loader.getController();

            vS.addFxmlLoaders(address);
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
    protected void onJoinAction(){
        try{
            String address = "/Views/Memory/JoinLanGame.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            JoinLan controller = loader.getController();

            vS.addFxmlLoaders(address);
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
    protected void onExitAction(){
        GameBox controller = (GameBox) c.backScene(header, vS);
        controller.handViewStack(vS,c);

    }

    public void handViewStack(ViewStack vs, Configuration c){
        this.vS = vs;
        this.c = c;
    }
}
