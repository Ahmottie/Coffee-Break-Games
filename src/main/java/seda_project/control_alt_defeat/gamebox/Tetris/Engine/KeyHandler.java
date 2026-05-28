package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Tetris.Controller.GameScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KeyHandler {
    private final TetrisEngine engine;
    private final TetrisSettings tS;
    private final GameScreen gameScreen;
    private final boolean p1Only;

    private int p1LocalFocus = 0;
    private int p2LocalFocus = 0;

    // bypass OS limitations
    private final Map<KeyCode, Integer> activeKeys = new HashMap<>();
    private Timeline repeatTimer;

    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen) {
        this(engine, tS, gameScreen, false);
    }

    public KeyHandler(TetrisEngine engine, TetrisSettings tS, GameScreen gameScreen, boolean p1Only) {
        this.engine = engine;
        this.tS = tS;
        this.gameScreen = gameScreen;
        this.p1Only = p1Only;
    }

    public void stop() {
        if (repeatTimer != null) repeatTimer.stop();
    }

    public void handle(KeyCode key) {
        ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        ArrayList<KeyCode> p2 = tS.getPlayer2Keys();
        boolean twoBlocks = TetrisAdvancedSettings.getInstance().isTwoBlocks();

        if (p1Only) {
            if (key == p1.get(0)) engine.processInput(1, "LEFT", 0);
            else if (key == p1.get(1)) engine.processInput(1, "RIGHT", 0);
            else if (key == p1.get(2)) engine.processInput(1, "DROP", 0);
            else if (key == p1.get(3)) engine.processInput(1, "ROTATE", 0);

            if (twoBlocks) {
                if (key == p2.get(0)) engine.processInput(1, "LEFT", 1);
                else if (key == p2.get(1)) engine.processInput(1, "RIGHT", 1);
                else if (key == p2.get(2)) engine.processInput(1, "DROP", 1);
                else if (key == p2.get(3)) engine.processInput(1, "ROTATE", 1);
            } else {
                if (key == p2.get(0)) engine.processInput(1, "LEFT", 0);
                else if (key == p2.get(1)) engine.processInput(1, "RIGHT", 0);
                else if (key == p2.get(2)) engine.processInput(1, "DROP", 0);
                else if (key == p2.get(3)) engine.processInput(1, "ROTATE", 0);
            }
        }

        if (key == p1.get(0)) engine.processInput(1, "LEFT", p1LocalFocus);
        else if (key == p1.get(1)) engine.processInput(1, "RIGHT", p1LocalFocus);
        else if (key == p1.get(2)) engine.processInput(1, "DROP", p1LocalFocus);
        else if (key == p1.get(3)) engine.processInput(1, "ROTATE", p1LocalFocus);

        if (key == p2.get(0)) engine.processInput(2, "LEFT", p2LocalFocus);
        else if (key == p2.get(1)) engine.processInput(2, "RIGHT", p2LocalFocus);
        else if (key == p2.get(2)) engine.processInput(2, "DROP", p2LocalFocus);
        else if (key == p2.get(3)) engine.processInput(2, "ROTATE", p2LocalFocus);
    }

    public void attach(Scene scene) {
        // Track Physical Press
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = ((KeyEvent) event).getCode();
            if (!activeKeys.containsKey(code)) {
                activeKeys.put(code, 0); // 0 ticks held
                handle(code); // Trigger immediately
            }
            event.consume(); // Prevent OS repeat
        });

        // Track Physical Release
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            activeKeys.remove(((KeyEvent) event).getCode());
        });

        // Internal 60fps Key Engine
        repeatTimer = new Timeline(new KeyFrame(Duration.millis(20), e -> {
            ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
            ArrayList<KeyCode> p2 = tS.getPlayer2Keys();

            for (Map.Entry<KeyCode, Integer> entry : new ArrayList<>(activeKeys.entrySet())) {
                KeyCode key = entry.getKey();
                int ticks = entry.getValue();
                ticks++;
                activeKeys.put(key, ticks);

                boolean isLeftRight = (key == p1.get(0) || key == p1.get(1) || key == p2.get(0) || key == p2.get(1));
                boolean isDrop = (key == p1.get(2) || key == p2.get(2));

                if (isDrop) {
                    // Soft drop repeats very fast (40ms) immediately
                    if (ticks % 2 == 0) handle(key);
                } else if (isLeftRight) {
                    // Standard Tetris DAS: Wait 160ms before auto-repeating, then move every 60ms
                    if (ticks > 8 && (ticks - 8) % 3 == 0) handle(key);
                }
            }
        }));
        repeatTimer.setCycleCount(Timeline.INDEFINITE);
        repeatTimer.play();
    }
}
