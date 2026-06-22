package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class LocalGameConfiguration extends Controller implements Initializable {

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
    protected void onBackAction(){
        sC.play("button");
        c.backScene(header,vS);
    }

    @FXML
    private void onStartGameAction(){
        sC.play("button");
        RadioButton selected = (RadioButton) DeckSizeGroup.getSelectedToggle();

        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        int tupleSize = matchSize.getSelectionModel().getSelectedItem();


        if (c.checkNameLength(player1Name,1, statusLabel) && c.checkNameLength(player2Name,2,statusLabel)) {
            if (selected != null) {
                sC.stopLooping();
                sC.playLooping("memory_background",.3);
                int deckSize = Integer.parseInt(selected.getText());
                GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
                if (c.checkFlip(player1Name,player2Name)){
                    controller.flip();
                }
                if (c.checkRainbow(player1Name, player2Name)){
                    controller.rainbow();
                }

                controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
                controller.startGame();
            }
            else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
            }
        }

    }
}
