package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;

public class TetrisSettings {
    private ArrayList<KeyCode> player1Keys;
    private ArrayList<KeyCode> player2Keys;
    private ArrayList<KeyCode> player1SecondaryKeys;
    private ArrayList<KeyCode> player2SecondaryKeys;

    private static TetrisSettings instance;

    public static TetrisSettings getInstance() {
        if (instance == null) {
            instance = new TetrisSettings();
        }
        return instance;
    }

    private TetrisSettings() {
        // Player 1 Primary (WASD + Toggle)
        player1Keys = new ArrayList<>();
        player1Keys.add(KeyCode.A);
        player1Keys.add(KeyCode.D);
        player1Keys.add(KeyCode.S);
        player1Keys.add(KeyCode.W);
        player1Keys.add(KeyCode.SPACE);

        // Player 1 Secondary (JLKI)
        player1SecondaryKeys = new ArrayList<>();
        player1SecondaryKeys.add(KeyCode.J);
        player1SecondaryKeys.add(KeyCode.L);
        player1SecondaryKeys.add(KeyCode.K);
        player1SecondaryKeys.add(KeyCode.I);

        // Player 2 Primary (Arrows + Toggle)
        player2Keys = new ArrayList<>();
        player2Keys.add(KeyCode.LEFT);
        player2Keys.add(KeyCode.RIGHT);
        player2Keys.add(KeyCode.DOWN);
        player2Keys.add(KeyCode.UP);
        player2Keys.add(KeyCode.SHIFT);

        // Player 2 Secondary (Numpad)
        player2SecondaryKeys = new ArrayList<>();
        player2SecondaryKeys.add(KeyCode.NUMPAD4);
        player2SecondaryKeys.add(KeyCode.NUMPAD6);
        player2SecondaryKeys.add(KeyCode.NUMPAD5);
        player2SecondaryKeys.add(KeyCode.NUMPAD8);
    }

    public KeyCode change(int player, int position) {
        Stage inputStage = new Stage();
        inputStage.setTitle("Press a key...");

        Label label = new Label("Press any unassigned key to assign it to Player " + (player + 1));
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 150);
        KeyCode[] result = new KeyCode[1];

        scene.setOnKeyPressed(e -> {
            KeyCode key = e.getCode();
            if (!player1Keys.contains(key) && !player2Keys.contains(key) &&
                    !player1SecondaryKeys.contains(key) && !player2SecondaryKeys.contains(key)) {

                result[0] = key;

                if (position < 4) {
                    switch (player) {
                        case 0:
                            player1Keys.set(position, key);
                            break;
                        case 1:
                            player2Keys.set(position, key);
                            break;
                    }
                }
                inputStage.close();
            }
        });

        inputStage.setScene(scene);
        inputStage.showAndWait();
        return result[0];
    }

    public ArrayList<KeyCode> getPlayer1Keys() { return player1Keys; }
    public ArrayList<KeyCode> getPlayer2Keys() { return player2Keys; }
    public ArrayList<KeyCode> getPlayer1SecondaryKeys() { return player1SecondaryKeys; }
    public ArrayList<KeyCode> getPlayer2SecondaryKeys() { return player2SecondaryKeys; }
}