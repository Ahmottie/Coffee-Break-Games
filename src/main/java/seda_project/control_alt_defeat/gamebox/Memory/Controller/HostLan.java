package seda_project.control_alt_defeat.gamebox.Memory.Controller;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.Session;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class HostLan extends Controller implements Initializable {
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
    protected void onBackAction(){
        sC.play("button");
        Session.clear();
        c.backScene(header,vS);
    }

    @FXML
    protected void onSearchAction(){
        sC.play("button");
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

                WaitForOpponent controller = (WaitForOpponent) c.changeScene("/Views/Memory/WaitForOpponent.fxml",header,vS);
                boolean host = true;
                controller.passHostData(host, yourName, tupleSize, deckSize);
            } else {
                statusLabel.setVisible(true);
                statusLabel.setText("You need to select a deck Size!");
            }
        }

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
