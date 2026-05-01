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
import seda_project.control_alt_defeat.gamebox.Memory.engine.SymbolLoader;

import java.net.URL;
import java.util.ResourceBundle;

public class LocalGameConfiguration implements Initializable {
    ViewStack vS = GameBox.getvS();
    Configuration c = new Configuration();

    @FXML
    private VBox header;

    @FXML
    private ComboBox<Integer> matchSize;

    @FXML
    private ToggleGroup DeckSizeGroup;

    @FXML
    private RadioButton smallGame,mediumGame,largeGame;

    @FXML
    private TextField player1TF, player2TF;

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
    private void onStartGameAction(){
        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();


        if (c.checkNameLength(player1Name,1, statusLabel) && c.checkNameLength(player2Name,2,statusLabel)) {
            if (selected != null) {
                try {
                    int deckSize = Integer.parseInt(selected.getText());
                    String address = "/Views/Memory/GameScreen.fxml";
                    FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
                    Parent root = loader.load();
                    GameScreen controller = loader.getController();

                    vS.addFxmlLoaders(address);

                    Scene newScene = new Scene(root, 800, 600);
                    Stage stage = (Stage) header.getScene().getWindow();
                    stage.setScene(newScene);
                    stage.show();

                    controller.handViewStack(vS);
                    controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
                    controller.startGame(player1Name,player2Name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
            }
        }

    }


    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }
}
