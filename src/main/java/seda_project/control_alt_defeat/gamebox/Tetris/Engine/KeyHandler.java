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

    public void handle (KeyCode key){
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();

        if (key == p1.get(0)) {engine.processInput(1,"LEFT");}
        else if (key == p1.get(1)) {engine.processInput(1,"RIGHT");}
        else if (key == p1.get(2)) {engine.processInput(1,"DROP");}
        else if (key == p1.get(3)) {engine.processInput(1,"ROTATE");}

        if (p1Only) return;

        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();
        if (key == p2.get(0)) {engine.processInput(2,"LEFT");}
        else if (key == p2.get(1)) {engine.processInput(2,"RIGHT");}
        else if (key  == p2.get(2)) {engine.processInput(2,"DROP");}
        else if (key  == p2.get(3)) {engine.processInput(2,"ROTATE");}
    }
    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyEvent key = (KeyEvent) event;
            handle(key.getCode());
            event.consume();
        });
    }
}
