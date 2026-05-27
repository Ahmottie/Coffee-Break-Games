package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.IntField;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;


public class AdvancedSettings extends Controller implements Initializable {
    private final TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    private CheckBox swapBoards, swapBlocks,portals, opponentSlowDown, opponentSpeedUp, opponentDelayRotation,selfSlowDown,selfDelayRotation,radialBomb,columnBomb;

    @FXML
    private IntField itemSpawnRate, itemDespawnTime;

    @FXML
    private VBox header;

    @FXML
    private ToggleGroup Layout;

    @FXML
    private ToggleSwitch toggleSwitch;

    @FXML
    protected void onBackAction(){
        c.backScene(header,vS);
    }
    @FXML
    protected void onSaveAction(){
        advancedSettings.saveIntSettings(itemSpawnRate.getValue(), itemDespawnTime.getValue());

        advancedSettings.setSwapBoards(swapBoards.isSelected());
        advancedSettings.setSwapBlocks(swapBlocks.isSelected());
        advancedSettings.setPortals(portals.isSelected());
        advancedSettings.setOpponentDelayRotation(opponentDelayRotation.isSelected());
        advancedSettings.setOpponentSpeedUp(opponentSpeedUp.isSelected());
        advancedSettings.setOpponentSlowDown(opponentSlowDown.isSelected());
        advancedSettings.setSelfDelayRotation(selfDelayRotation.isSelected());
        advancedSettings.setSelfSpeedDown(selfSlowDown.isSelected());
        advancedSettings.setRadialBomb(radialBomb.isSelected());
        advancedSettings.setColumnBomb(columnBomb.isSelected());
        advancedSettings.setBoardChange(toggleSwitch.switchOnProperty().getValue());
        advancedSettings.setVertical(((RadioButton)Layout.getSelectedToggle()).getText().equals("Vertical"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<Toggle> toggels = Layout.getToggles();
        if (!advancedSettings.isVertical()){
            toggels.get(1).setSelected(true);
        }
        toggleSwitch.setSwitchedOn(advancedSettings.isBoardChange());
        swapBoards.setSelected(advancedSettings.isSwapBoards());
        swapBlocks.setSelected(advancedSettings.isSwapBlocks());
        portals.setSelected(advancedSettings.isPortals());
        opponentSlowDown.setSelected(advancedSettings.isOpponentSlowDown());
        opponentSpeedUp.setSelected(advancedSettings.isOpponentSpeedUp());
        opponentDelayRotation.setSelected(advancedSettings.isOpponentDelayRotation());
        selfSlowDown.setSelected(advancedSettings.isSelfDelayRotation());
        selfDelayRotation.setSelected(advancedSettings.isSelfDelayRotation());
        radialBomb.setSelected(advancedSettings.isRadialBomb());
        columnBomb.setSelected(advancedSettings.isColumnBomb());

        itemSpawnRate.setText((advancedSettings.getItemSpawnRate()/1000)+"");
        itemDespawnTime.setText((advancedSettings.getItemDespawnRate()/1000)+"");
    }
}
