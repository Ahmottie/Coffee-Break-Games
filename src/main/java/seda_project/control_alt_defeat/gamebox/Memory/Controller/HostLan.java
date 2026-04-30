package seda_project.control_alt_defeat.gamebox.Memory.Controller;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.Session;

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
    Configuration c = new Configuration();

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

    @FXML
    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setVisible(false);

        matchSize.getItems().clear();
        for(int i = 1; i <= 45; i++) {
            matchSize.getItems().add(i);
        }
        matchSize.getSelectionModel().select(2);
    }
    @FXML
    private void calcDeckSize(){
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();
        c.deckSize(tupleSize,smallGame,mediumGame,largeGame);
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

        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String yourName = c.checkNameInput(hostNameTF.getText(),1);

        int tupleSize = matchSize.getSelectionModel().getSelectedItem();

        if (c.checkNameLength(yourName, 1,statusLabel)) {
            if (selected != null) {

                int deckSize = Integer.parseInt(selected.getText());

                GameConfig config = new GameConfig(tupleSize, deckSize, yourName, "Opponent");
                GameSetup setup = Decks.prepare(config);

                Session s = Session.current();
                s.myName = yourName;
                s.isHost = true;
                s.config = config;
                s.setup  = setup;

                try {
                    String address = "/Views/Memory/WaitForOpponent.fxml";
                    FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
                    Parent root = loader.load();
                    WaitForOpponent controller = loader.getController();

                    vS.addFxmlLoaders(address);
                    boolean host = true;
                    controller.passHostData(vS, host, yourName, tupleSize, deckSize);

                    Scene newScene = new Scene(root, 800, 600);
                    Stage stage = (Stage) header.getScene().getWindow();
                    stage.setScene(newScene);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
            }
        }

    }

    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }

    public void backTransfer(String name, int tupleSize, int deckSize){
        hostNameTF.setText(name);
        matchSize.getSelectionModel().select(tupleSize-1);
        c.deckSize(tupleSize,smallGame,mediumGame,largeGame);

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
