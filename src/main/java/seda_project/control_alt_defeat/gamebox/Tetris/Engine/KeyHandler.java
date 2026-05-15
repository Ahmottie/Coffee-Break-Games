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

    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen) {
        this.engine = engine;
        this.tS = tS;
        this.gameScreen = gameScreen;
    }

    public void handle (KeyCode key){
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();

        if (key == p1.get(0)) {engine.processInput(1,"LEFT");}
        else if (key == p1.get(1)) {engine.processInput(1,"RIGHT");}
        else if (key == p1.get(2)) {engine.processInput(1,"DROP");}
        else if (key == p1.get(3)) {engine.processInput(1,"ROTATE");}

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
