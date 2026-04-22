package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

import java.net.URL;
import java.util.ResourceBundle;

public class HostLan implements Initializable {
    public ViewStack vS = GameBox.getvS();

    @FXML
    private VBox header;

    @FXML
    private RadioButton smallGame,mediumGame,largeGame;

    @FXML
    private ComboBox<Integer> matchSize;

    @FXML
    private ToggleGroup DeckSizeGroup;

    @FXML
    private TextField hostNameTF;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matchSize.getItems().clear();

        for(int i = 1; i <= 45; i++) {
            matchSize.getItems().add(i);
        }
        matchSize.getSelectionModel().select(2);
    }
    @FXML
    private void calcDeckSize(){
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();
        Configuration.deckSize(tupleSize,smallGame,mediumGame,largeGame);
    }

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
    private void onSearchAction(){
        //TODO Create Server that can be found

        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String yourName = hostNameTF.getText();
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();
        int deckSize = Integer.parseInt(selected.getText());

        try {
            String address = "/Views/Memory/WaitForOpponent.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            WaitForOpponent controller = loader.getController();

            vS.addFxmlLoaders(address);
            boolean host = true;
            controller.passHostData(vS, host, yourName, tupleSize,deckSize);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }

    public void backTransfer(String name, int tupleSize, int deckSize){
        hostNameTF.setText(name);
        matchSize.getSelectionModel().select(tupleSize-1);
        Configuration.deckSize(tupleSize,smallGame,mediumGame,largeGame);

        if (smallGame.getText().equals(String.valueOf(deckSize))) {
            smallGame.setSelected(true);
        }
        if (mediumGame.getText().equals(String.valueOf(deckSize))) {
            mediumGame.setSelected(true);
        }
        if (largeGame.getText().equals(String.valueOf(deckSize))) {
            largeGame.setSelected(true);
        }
    }
}
