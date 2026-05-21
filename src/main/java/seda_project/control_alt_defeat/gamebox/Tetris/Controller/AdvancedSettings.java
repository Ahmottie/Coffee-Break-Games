package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;

import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.IntField;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class AdvancedSettings extends Controller {
    private TetrisAdvancedSettings advancedSettings = TetrisAdvancedSettings.getInstance();

    @FXML
    private CheckBox swapBoards, swapBlocks,portals, opponentSlowDown, opponentSpeedUp, opponentDelayRotation,selfSlowDown,selfDelayRotation,radialBomb,columnBomb;

    @FXML
    private IntField itemSpawnRate, itemDespawnTime;

    @FXML
    private VBox header;

    @FXML
    private ToggleGroup Layout;

    @FXML
    protected void onBackAction(){
        c.backScene(header,vS);
    }
    @FXML
    protected void onSaveAction(){
        advancedSettings.saveIntSettings(itemSpawnRate.getValue(), itemDespawnTime.getValue());
        boolean vertical = ((RadioButton)Layout.getSelectedToggle()).getText().toString().equals("Vertical");
        List bools = Stream.of(vertical,
                swapBoards.isSelected(),
                swapBlocks.isSelected(),
                portals.isSelected(),
                opponentSlowDown.isSelected(),
                opponentSpeedUp.isSelected(),
                opponentDelayRotation.isSelected(),
                selfSlowDown.isSelected(),
                selfDelayRotation.isSelected(),
                radialBomb.isSelected(),
                columnBomb.isSelected()
        ).toList();
        bools.stream().forEach(bool -> {
            System.out.println(bool);
        });
        advancedSettings.saveBoolSettings(bools);
    }

}
