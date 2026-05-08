package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.event.KeyEvent;
import java.security.Key;
import java.util.ArrayList;

public class TetrisSettings {
    private ArrayList<KeyCode> player1Keys;
    private ArrayList<KeyCode> player2Keys;

    public TetrisSettings() {
        player1Keys = new ArrayList<>();
        player1Keys.add(KeyCode.A);
        player1Keys.add(KeyCode.D);
        player1Keys.add(KeyCode.S);
        player1Keys.add(KeyCode.W);
        player2Keys = new ArrayList<>();
        player2Keys.add(KeyCode.LEFT);
        player2Keys.add(KeyCode.RIGHT);
        player2Keys.add(KeyCode.DOWN);
        player2Keys.add(KeyCode.UP);
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
            if (!player1Keys.contains(key)&& !player2Keys.contains(key)) {
                result[0] = key;
                switch (player) {
                    case 0:
                        player1Keys.set(position, key);
                        break;
                    case 1:
                        player2Keys.set(position, key);
                        break;
                }
                inputStage.close();
            }
        });

        inputStage.setScene(scene);
        inputStage.showAndWait();
        return result[0];
    }
    public ArrayList<KeyCode> getPlayer1Keys() {
        return player1Keys;
    }

    public ArrayList<KeyCode> getPlayer2Keys() {
        return player2Keys;
    }
}
