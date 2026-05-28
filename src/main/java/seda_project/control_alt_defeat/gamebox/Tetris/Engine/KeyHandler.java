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

    // p1Only = true in LAN host mode: this keyboard owns player 1 only, player 2
    // input arrives from the client over the network.
    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen, boolean p1Only) {
        this.engine = engine;
        this.tS = tS;
        this.gameScreen = gameScreen;
        this.p1Only = p1Only;
    }

    public void handle(KeyCode key) {
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        ArrayList<KeyCode> p1Sec = tS.getPlayer1SecondaryKeys();

        if (key == p1.get(0)) engine.processInput(1, "LEFT", 0);
        else if (key == p1.get(1)) engine.processInput(1, "RIGHT", 0);
        else if (key == p1.get(2)) engine.processInput(1, "DROP", 0);
        else if (key == p1.get(3)) engine.processInput(1, "ROTATE", 0);

        else if (key == p1Sec.get(0)) engine.processInput(1, "LEFT", 1);
        else if (key == p1Sec.get(1)) engine.processInput(1, "RIGHT", 1);
        else if (key == p1Sec.get(2)) engine.processInput(1, "DROP", 1);
        else if (key == p1Sec.get(3)) engine.processInput(1, "ROTATE", 1);

        if (p1Only) return;

        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();
        ArrayList<KeyCode> p2Sec = tS.getPlayer2SecondaryKeys();

        if (key == p2.get(0)) engine.processInput(2, "LEFT", 0);
        else if (key == p2.get(1)) engine.processInput(2, "RIGHT", 0);
        else if (key == p2.get(2)) engine.processInput(2, "DROP", 0);
        else if (key == p2.get(3)) engine.processInput(2, "ROTATE", 0);

        else if (key == p2Sec.get(0)) engine.processInput(2, "LEFT", 1);
        else if (key == p2Sec.get(1)) engine.processInput(2, "RIGHT", 1);
        else if (key == p2Sec.get(2)) engine.processInput(2, "DROP", 1);
        else if (key == p2Sec.get(3)) engine.processInput(2, "ROTATE", 1);
    }

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyEvent key = (KeyEvent) event;
            handle(key.getCode());
            event.consume();
        });
    }
}
