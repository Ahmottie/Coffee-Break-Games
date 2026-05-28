package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seda_project.control_alt_defeat.gamebox.Tetris.Controller.GameScreen;

import java.util.ArrayList;

public class KeyHandler {
    private final TetrisEngine engine;
    private final TetrisSettings tS;
    private final GameScreen gameScreen;
    private final boolean p1Only;

    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen) {
        this(engine, tS, gameScreen, false);
    }

    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen, boolean p1Only) {
        this.engine = engine;
        this.tS = tS;
        this.gameScreen = gameScreen;
        this.p1Only = p1Only;
    }

    public void handle(KeyCode key) {
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();
        boolean twoBlocks = TetrisAdvancedSettings.getInstance().isTwoBlocks();

        // Standard: P1 UI keys control Block 0
        if (key == p1.get(0)) engine.processInput(1, "LEFT", 0);
        else if (key == p1.get(1)) engine.processInput(1, "RIGHT", 0);
        else if (key == p1.get(2)) engine.processInput(1, "DROP", 0);
        else if (key == p1.get(3)) engine.processInput(1, "ROTATE", 0);

        if (p1Only) {
            // LAN HOST MODE
            int blockIndex = twoBlocks ? 1 : 0;
            if (key == p2.get(0)) engine.processInput(1, "LEFT", blockIndex);
            else if (key == p2.get(1)) engine.processInput(1, "RIGHT", blockIndex);
            else if (key == p2.get(2)) engine.processInput(1, "DROP", blockIndex);
            else if (key == p2.get(3)) engine.processInput(1, "ROTATE", blockIndex);
            return;
        }

        // LOCAL MODE
        if (key == p2.get(0)) engine.processInput(2, "LEFT", 0);
        else if (key == p2.get(1)) engine.processInput(2, "RIGHT", 0);
        else if (key == p2.get(2)) engine.processInput(2, "DROP", 0);
        else if (key == p2.get(3)) engine.processInput(2, "ROTATE", 0);
    }

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyEvent key = (KeyEvent) event;
            handle(key.getCode());
            event.consume();
        });
    }
}
