package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class LocalGameConfiguration extends Controller implements Initializable {

    protected TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    private VBox header;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<Integer> player1Level,player2Level;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setVisible(false);

        player1Level.getItems().clear();
        player2Level.getItems().clear();

        for (int i = 0; i < 20; i++) {
            player1Level.getItems().add(i+1);
            player2Level.getItems().add(i+1);
        }
        player1Level.getSelectionModel().select(0);
        player2Level.getSelectionModel().select(0);
    }

    @FXML
    protected void onBackAction() {
        c.backScene(header,vS);
    }

    @FXML
    protected void onAdvancedSettingsAction(){
        String p1Name = c.checkNameInput(player1TF.getText(),1);
        String p2Name = c.checkNameInput(player2TF.getText(),2);
        int p1Level =  player1Level.getSelectionModel().getSelectedItem();
        int p2Level =  player2Level.getSelectionModel().getSelectedItem();
        AdvancedSettings controller = (AdvancedSettings) c.changeScene("/Views/Tetris/AdvancedSettings.fxml",header,vS);
        controller.handLocalData(p1Name,p2Name,p1Level,p2Level);
    }

    @FXML
    protected void onStartAction() {
        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        int p1Level =  player1Level.getSelectionModel().getSelectedItem();
        int p2Level =  player2Level.getSelectionModel().getSelectedItem();
        String address =  "";
        advancedSettings.setTwoBlocks(false);
        if (advancedSettings.isVertical()){
            address = "/Views/Tetris/GameScreen.fxml";
        }
        else { address = "/Views/Tetris/GameScreenHorizontal.fxml";
        }

        if (c.checkNameLength(player1Name,1,statusLabel) && c.checkNameLength(player2Name,2,statusLabel)){
            GameScreen controller = (GameScreen) c.changeScene(address,header,vS);
            TetrisEngine engine = new TetrisEngine(player1Name,player2Name, p1Level,p2Level, BlockRegistry.getInstance(),advancedSettings);
            controller.create(player1Name,player2Name,p1Level, p2Level,false, engine);
            controller.setInitialLevels(p1Level,p2Level);
        }
    }

    public void handLocalData(String p1Name, String p2Name, int p1Level, int p2Level) {
        player1TF.setText(p1Name);
        player2TF.setText(p2Name);
        player1Level.getSelectionModel().select(p1Level-1);
        player2Level.getSelectionModel().select(p2Level-1);
    }
}
