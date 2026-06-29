package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.*;
import seda_project.control_alt_defeat.gamebox.Tetris.network.TetrisMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameScreen extends Controller implements TetrisEventListener {
    private final TetrisSettings tS = TetrisSettings.getInstance();
    private TetrisEngine engine;
    private KeyHandler handler;
    private int initP1Level, initP2Level;

    private Timeline p1EngineTicker;
    private Timeline p2EngineTicker;

    private boolean disconnected = false;
    private boolean gameOverHandled = false;

    // Dual-engine LAN state. controlledPlayer is the player THIS machine simulates
    // (host = 1, client = 2); the opponent board is display-only from snapshots.
    private int controlledPlayer = 0;
    private boolean localLost = false;
    private boolean oppLost = false;
    private int oppFinalScore = 0;
    private int oppFinalLines = 0;
    // Power-ups currently shown on the opponent panel, tracked separately from my own
    // (currentPowerUps) so the two displays don't clobber each other in dual mode.
    private List<PowerUp> oppPowerUps = new ArrayList<>();

    private Image swapImage, portalImage, swapBlockImage, decreaseRotationOpponentImage,decreaseRotationSelfImage,decreaseTickOpponentImage,decreaseTickSelfImage,increaseTickOpponentImage;
    private Image radialBombImage, columnBombImage;

    private List<PowerUp> currentPowerUps;
    private boolean currentTwoBlockMode = false;

    private final java.util.Map<KeyCode, Integer> clientActiveKeys = new java.util.HashMap<>();
    private Timeline clientRepeatTimer;

    // AI-generated optimization: keep only the latest pending snapshot so the
    // JavaFX thread renders once per batch instead of one runLater per message.
    private final java.util.concurrent.atomic.AtomicReference<TetrisEngine.GameState> pendingState =
            new java.util.concurrent.atomic.AtomicReference<>();


    private void loadPowerUpImages() {
        var stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/Swap.png")).toExternalForm();
        if (stream != null) {
            swapImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/Portal.png")).toExternalForm();
        if (stream != null) {
            portalImage =  new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/SwapBlocks.png")).toExternalForm();
        if ( stream != null) {
            swapBlockImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/DecreaseRotationOpponent.png")).toExternalForm();
        if ( stream != null) {
            decreaseRotationOpponentImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/DecreaseRotationSelf.png")).toExternalForm();
        if ( stream != null) {
            decreaseRotationSelfImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/DecreaseTickOpponent.png")).toExternalForm();
        if ( stream != null) {
            decreaseTickOpponentImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/DecreaseTickSelf.png")).toExternalForm();
        if ( stream != null) {
            decreaseTickSelfImage = new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/IncreaseTickOpponent.png")).toExternalForm();
        if ( stream != null) {
            increaseTickOpponentImage = new Image(stream);
        }
    }

    @FXML
    private GridPane player1Field, player2Field;

    @FXML
    private Label player1NameLabel, player2NameLabel, player1PointsLabel, player2PointsLabel, player1LinesLabel, player2LinesLabel,p1LevelLabel, p2LevelLabel;

    @FXML
    protected void onExitGameAction() {
        sC.play("button");
        sC.stopLooping();
        sC.playLooping("lobby_background",.2);
        // We're leaving on purpose: closing the connection below fires our own
        // onDisconnected, so suppress the disconnect handler to avoid a double navigate.
        disconnected = true;
        Session.clear();
        // engine is null in LAN-client mode
        if (engine != null) {
            engine.reset();
            engine.stop();
        }
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    public void setPlayerPoints(int player, String points){
        if (player == 1){
            player1PointsLabel.setText(points);
        }
        else if (player == 2){
            player2PointsLabel.setText(points);
        }
    }

    public void setPlayerLines(int player, String lines){
        if (player == 1){
            player1LinesLabel.setText(lines);
        }
        else if (player == 2){
            player2LinesLabel.setText(lines);
        }
    }

    public void render(TetrisEngine.GameState state, int player ){
        if (player == 1) {
            // Dual mode: keep my own field's row count in sync with my board (board-change / swaps).
            if (controlledPlayer == 1) syncBoardRows(player1Field, state.p1Grid().length, true);
            drawGrid(state.p1Grid(), state.p1ActiveBlocks(), player1Field, state.p1Lost());
        } else {
            if (controlledPlayer == 2) syncBoardRows(player2Field, state.p2Grid().length, false);
            drawGrid(state.p2Grid(), state.p2ActiveBlocks(), player2Field, state.p2Lost());
        }
    }

    private void showPowerUP(List<PowerUp> powerUps) {
        if (powerUps.isEmpty()){
            player1Field.getChildren().removeIf(node -> (node instanceof Rectangle r
                    && r.getStyleClass().contains("PowerUp")));
            player2Field.getChildren().removeIf(node -> (node instanceof Rectangle r
                    && r.getStyleClass().contains("PowerUp")));
        }

        Rectangle rect = null;

        for (PowerUp powerUp : powerUps) {
            if (!currentPowerUps.contains(powerUp)) {
                rect = new Rectangle(13, 13);
                Image i = getImage(powerUp);
                if (i != null) {
                    rect.setFill(new ImagePattern(i));
                    rect.getStyleClass().add("PowerUp");
                } else {
                    rect.setFill(Color.YELLOW);
                }

                if (powerUp.getPlayerNum() == 1) {
                    player1Field.add(rect, powerUp.getCol(), powerUp.getRow());
                } else {
                    player2Field.add(rect, powerUp.getCol(), powerUp.getRow());
                }
            }
        }

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(0.5), rect);
        pulse.setFromX(1.0);
        pulse.setToX(2.0);
        pulse.setFromY(1.0);
        pulse.setToY(2.0);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private Image getImage(PowerUp powerUp) {
        Image i = null;
        switch (powerUp.getType()) {
            case PORTAL -> i = portalImage;
            case SWAPBOARDS -> i = swapImage;
            case SWAPACTIVEBLOCKS -> i = swapBlockImage;
            case OPPONENTROTATIONDELAY -> i = decreaseRotationOpponentImage;
            case SELFROTATIONDELAY -> i = decreaseRotationSelfImage;
            case OPPONENTSPEEDDOWN -> i = decreaseTickOpponentImage;
            case OPPONENTSPEEDUP -> i = increaseTickOpponentImage;
            case SELFSPEEDDOWN -> i = decreaseTickSelfImage;
        }
        return i;
    }

    private void removePowerUP(PowerUp powerUp) {
        GridPane targetField = powerUp.getPlayerNum() == 1 ? player1Field : player2Field;

        targetField.getChildren().removeIf(node ->
                node instanceof Rectangle r
                        && r.getStyleClass().contains("PowerUp")
                        && GridPane.getColumnIndex(r) == powerUp.getCol()
                        && GridPane.getRowIndex(r) == powerUp.getRow()
        );
    }

    private void drawGrid(String[][] colors, Block[] activeBlocks, GridPane grid, boolean isLost) {
        grid.getChildren().removeIf(node -> !(node instanceof Rectangle r && r.getStyleClass().contains("PowerUp")));
        int maxRows = grid.getRowConstraints().size();

        // 1. Draw locked board
        for (int i = 0; i < colors.length; i++) {
            if (i >= maxRows) break;
            for (int j = 0; j < colors[i].length; j++) {
                if (colors[i][j] != null){
                    Rectangle rect = new Rectangle(12, 12);
                    if (isLost) rect.setFill(Color.LIGHTGRAY);
                    else rect.setFill(Color.web(colors[i][j]));
                    grid.add(rect, j, i);
                }
            }
        }

        // 2. Draw all active blocks
        if (activeBlocks != null) {
            for (Block activeBlock : activeBlocks) {
                if (activeBlock == null) continue;
                boolean[][] block = activeBlock.getShape();
                for (int i = 0; i < block.length; i++) {
                    for (int j = 0; j < block[0].length; j++) {
                        if (block[i][j]) {
                            Rectangle rect = new Rectangle(12, 12);
                            if (activeBlock instanceof BombBlock bb) {
                                rect.setFill(new ImagePattern((bb.getType() == BombType.RADIUS) ? radialBombImage : columnBombImage));
                            } else {
                                if (isLost) rect.setFill(Color.LIGHTGRAY);
                                else rect.setFill(Color.web(activeBlock.getHexColor()));
                            }
                            grid.add(rect, j + activeBlock.getX(), i + activeBlock.getY());
                        }
                    }
                }
            }
        }
    }


    public void create(String player1, String player2, int p1Level, int p2Level, TetrisEngine engine, Scene scene) {
        this.engine = engine;
        player1NameLabel.setText(player1);
        player2NameLabel.setText(player2);
        player1LinesLabel.setText("0");
        player2LinesLabel.setText("0");
        player1PointsLabel.setText("0");
        player2PointsLabel.setText("0");
        p1LevelLabel.setText(p1Level +"");
        p2LevelLabel.setText(p2Level +"");

        currentPowerUps = new ArrayList<>();

        loadImages();

        // LAN-client mode: no engine, no KeyHandler etc.
        // client renders snapshots received over the network and forwards key events as input msg
        if (engine == null) {
            return;
        }

        engine.addListener(this);
        // In LAN host mode this keyboard only drives player 1; player 2 input
        // arrives from the client over the network.
        boolean lanHost = Session.current().network != null;
        handler = new KeyHandler(engine, tS, this, lanHost);
        handler.attach(scene);

        p1EngineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs(1)),
                        _ -> engine.tick(1)
                )
        );
        p1EngineTicker.setCycleCount(Animation.INDEFINITE);
        p1EngineTicker.play();
        p2EngineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs(2)),
                        _ -> engine.tick(2)
                )
        );
        p2EngineTicker.setCycleCount(Animation.INDEFINITE);
        p2EngineTicker.play();
    }

    private void loadImages() {
        loadPowerUpImages();
        loadBombImages();
    }

    private void loadBombImages() {
        var stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/RadialBomb.png")).toExternalForm();
        if (stream != null) {
            radialBombImage =  new Image(stream);
        }
        stream = Objects.requireNonNull(getClass().getResource("/Images/Tetris/ColumnBomb.png")).toExternalForm();
        if (stream != null) {
            columnBombImage = new Image(stream);
        }
    }

    @Override
    public void onTick(TetrisEngine.GameState snapshot, int player) {
        render(snapshot, player);
    }

    @Override
    public void onBlockLocked(int playerNum, TetrisEngine.GameState snapshot) {
        setPlayerPoints(playerNum, String.valueOf(
                playerNum == 1 ? snapshot.p1Score() : snapshot.p2Score()
        ));
        render(snapshot,playerNum);
    }

    @Override
    public void onBoardSizeChange (int playerNum, int linesCleared, TetrisEngine.GameState snapshot) {
        // Dual mode: only my own board changes here; render() resizes my field from the
        // grid, and the opponent's field is resized from their snapshots in renderOpponent.
        if (controlledPlayer != 0) {
            render(snapshot, controlledPlayer);
            return;
        }
        RowConstraints row = new RowConstraints();
        row.setPrefHeight(12);
        row.setMinHeight(12);
        row.setMaxHeight(12);
        row.setVgrow(Priority.NEVER);
        for (int i = 0; i < linesCleared; i++) {
            if (playerNum == 1) {
                player1Field.getRowConstraints().addFirst(row);
                player2Field.getRowConstraints().removeLast();
            }
            else {
                player1Field.getRowConstraints().removeFirst();
                player2Field.getRowConstraints().add(row);
            }
        }
        render(snapshot, 1);
        render(snapshot, 2);
    }

    @Override
    public void onLinesCleared(int playerNum, int lineCount, TetrisEngine.GameState snapshot) {
        sC.play("tetris_lineclear");
        if (playerNum == 1 ){
            int current = Integer.parseInt(player1LinesLabel.getText());
            setPlayerLines(playerNum, String.valueOf(current+lineCount));
            setPlayerPoints(1, String.valueOf(snapshot.p1Score()));
        }
        else {
            int current = Integer.parseInt(player2LinesLabel.getText());
            setPlayerLines(playerNum, String.valueOf(current+lineCount));
            setPlayerPoints(2, String.valueOf(snapshot.p2Score()));
        }
        render(snapshot,playerNum);

    }

    @Override
    public void onLevelChanged(long newTickIntervalMs, TetrisEngine.GameState snapshot, int player) {
        // engineTicker only exists in modes that own an engine like lan host or local
        sC.play("tetris_levelup");
        Timeline timeline = player == 1 ? p1EngineTicker : p2EngineTicker;
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.millis(newTickIntervalMs), _ -> engine.tick(player))
            );
            timeline.play();
        }
        if (player == 1) {
            p1LevelLabel.setText(String.valueOf(snapshot.p1Level()));
        } else {
            p2LevelLabel.setText(String.valueOf(snapshot.p2Level()));
        }
    }

    public void changeTickSpeed(int playerNum, long newTickSpeed) {
        Timeline timeline = playerNum == 1 ? p1EngineTicker : p2EngineTicker;
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.millis(newTickSpeed), _ -> engine.tick(playerNum))
            );
            timeline.play();
        }
    }

    @Override
    public void radialBomb() {
        sC.play("tetris_radialbomb");
    }

    @Override
    public void onPlayerLost(int playerNum, TetrisEngine.GameState snapshot) {
        render(snapshot, playerNum);
    }

    @Override
    public void onGameOver(TetrisEngine.GameState snapshot) {
        // Null-safe per ticker: in dual-engine mode only one of the two tickers
        // exists (each machine ticks only its own player), so stopping them must
        // not assume both are present.
        if (p1EngineTicker != null) p1EngineTicker.stop();
        if (p2EngineTicker != null) p2EngineTicker.stop();
        if (handler != null) handler.stop();
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Tetris/ResultScreen.fxml", header, vS);
        controller.setInitialLevels(initP1Level,initP2Level);
        controller.handGameState(snapshot, engine, player1LinesLabel,player2LinesLabel);
        if (flipped){
            controller.flip();
        }
        if (rainbowed){
            controller.rainbow();
        }
    }

    @Override
    public void onPowerUpTriggered(TetrisEngine.GameState snapshot, PowerUp p) {
        removePowerUP(p);
        currentPowerUps = snapshot.powerUps();
    }

    @Override
    public void clearPowerUps(){
        player1Field.getChildren().removeIf(node -> (node instanceof Rectangle r
                && r.getStyleClass().contains("PowerUp")));
        player2Field.getChildren().removeIf(node -> (node instanceof Rectangle r
                && r.getStyleClass().contains("PowerUp")));
        currentPowerUps.clear();
    }

    @Override
    public void onPowerUpSpawned(TetrisEngine.GameState snapshot){
        showPowerUP(snapshot.powerUps());
        currentPowerUps = snapshot.powerUps();
    }

    @Override
    public void onStopped (TetrisEngine.GameState snapshot){
        // Null-safe per ticker: in dual-engine mode only one of the two tickers
        // exists (each machine ticks only its own player), so stopping them must
        // not assume both are present.
        if (p1EngineTicker != null) p1EngineTicker.stop();
        if (p2EngineTicker != null) p2EngineTicker.stop();
        if (handler != null) handler.stop();
    }

    @Override
    public void onBlockMovement (TetrisEngine.GameState snapshot, int player) {
        render(snapshot, player);
    }

    @Override
    public void onBlockSwap(TetrisEngine.GameState snapshot){
        // Dual mode: my snapshot is only authoritative for my own board.
        if (controlledPlayer != 0) {
            render(snapshot, controlledPlayer);
            return;
        }
        render(snapshot, 1);
        render(snapshot,2);
    }

    @Override
    public void lockSound() {
        sC.play("tetris_snap");
    }

    @Override
    public void columnBomb(){
        sC.play("tetris_columnbomb");
    }

    public void attachHostNetworkBridge(NetworkLayer network) {
        if (engine == null || network == null) return;

        network.clearListeners();

        engine.addListener(new TetrisEventListener() {
            @Override public void onTick(TetrisEngine.GameState s, int player) {
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onBlockLocked(int p, TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onLinesCleared(int p, int n, TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
                network.send(new TetrisMessage.LinesCleared(p, n));
            }
            @Override public void onLevelChanged(long ms, TetrisEngine.GameState s, int player){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onPlayerLost(int p, TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onGameOver(TetrisEngine.GameState s) {
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onPowerUpTriggered(TetrisEngine.GameState s, PowerUp p ){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onPowerUpSpawned(TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onReset(TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
            @Override public void onBlockMovement(TetrisEngine.GameState s, int player){
                network.send(new TetrisMessage.StateUpdate(s));
            }

            @Override public void onStopped(TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
        });

        // Network to engine: Apply remote inputs
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TetrisMessage.Input in) {
                    engine.processInput(in.playerNum(), in.action().name(), in.blockIndex());
                }
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
        });
    }

    public void attachClientNetworkBridge(NetworkLayer network) {
        if (engine != null || network == null) return;

        network.clearListeners();

        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TetrisMessage.StateUpdate(TetrisEngine.GameState state)) {
                    // AI-generated optimization: store latest snapshot; only schedule a
                    // render if one isn't already pending (last-write-wins).
                    if (pendingState.getAndSet(state) == null) {
                        Platform.runLater(() -> {
                            TetrisEngine.GameState latest = pendingState.getAndSet(null);
                            if (latest != null) applyRemoteState(latest);
                        });
                    }
                } else if (msg instanceof TetrisMessage.LinesCleared(int playerNum, int lineCount)) {
                    Platform.runLater(() -> incrementLines(playerNum, lineCount));
                }
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
        });

        Scene scene = header.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                KeyCode code = event.getCode();
                if (!clientActiveKeys.containsKey(code)) {
                    clientActiveKeys.put(code, 0);
                    sendClientKey(code, network);
                }
                event.consume();
            });

            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> clientActiveKeys.remove(event.getCode()));

            clientRepeatTimer = new Timeline(new KeyFrame(Duration.millis(20), _ -> {
                java.util.ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
                java.util.ArrayList<KeyCode> p2 = tS.getPlayer2Keys();

                for (java.util.Map.Entry<KeyCode, Integer> entry : new java.util.ArrayList<>(clientActiveKeys.entrySet())) {
                    KeyCode key = entry.getKey();
                    int ticks = entry.getValue();
                    ticks++;
                    clientActiveKeys.put(key, ticks);

                    boolean isLeftRight = (key == p1.get(0) || key == p1.get(1) || key == p2.get(0) || key == p2.get(1));
                    boolean isDrop = (key == p1.get(2) || key == p2.get(2));

                    if (isDrop) {
                        if (ticks % 2 == 0) sendClientKey(key, network);
                    } else if (isLeftRight) {
                        if (ticks > 8 && (ticks - 8) % 3 == 0) sendClientKey(key, network);
                    }
                }
            }));
            clientRepeatTimer.setCycleCount(Animation.INDEFINITE);
            clientRepeatTimer.play();
        }
    }

    private void sendClientKey(KeyCode key, NetworkLayer network) {
        Object[] actionData = mapClientKey(key);
        if (actionData != null) {
            TetrisMessage.InputAction action = (TetrisMessage.InputAction) actionData[0];
            int blockIndex = (int) actionData[1];
            network.send(new TetrisMessage.Input(2, blockIndex, action));
        }
    }

    private Object[] mapClientKey(KeyCode key) {
        java.util.ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        java.util.ArrayList<KeyCode> p2 = tS.getPlayer2Keys();

        // Use the synchronized network state
        boolean twoBlocks = this.currentTwoBlockMode;

        // LAN Client Primary Keys (WASD) -> Block 0
        if (key == p1.get(0)) return new Object[]{TetrisMessage.InputAction.LEFT, 0};
        if (key == p1.get(1)) return new Object[]{TetrisMessage.InputAction.RIGHT, 0};
        if (key == p1.get(2)) return new Object[]{TetrisMessage.InputAction.DROP, 0};
        if (key == p1.get(3)) return new Object[]{TetrisMessage.InputAction.ROTATE, 0};

        // LAN Client Secondary Keys (Arrows)
        if (twoBlocks) {
            // -> Block 1 (active if Two Blocks is ON)
            if (key == p2.get(0)) return new Object[]{TetrisMessage.InputAction.LEFT, 1};
            if (key == p2.get(1)) return new Object[]{TetrisMessage.InputAction.RIGHT, 1};
            if (key == p2.get(2)) return new Object[]{TetrisMessage.InputAction.DROP, 1};
            if (key == p2.get(3)) return new Object[]{TetrisMessage.InputAction.ROTATE, 1};
        } else {
            // -> Block 0 (For single-PC LAN testing)
            if (key == p2.get(0)) return new Object[]{TetrisMessage.InputAction.LEFT, 0};
            if (key == p2.get(1)) return new Object[]{TetrisMessage.InputAction.RIGHT, 0};
            if (key == p2.get(2)) return new Object[]{TetrisMessage.InputAction.DROP, 0};
            if (key == p2.get(3)) return new Object[]{TetrisMessage.InputAction.ROTATE, 0};
        }

        return null;
    }

    private void incrementLines(int playerNum, int lineCount) {
        Label target = playerNum == 1 ? player1LinesLabel : player2LinesLabel;
        int current = Integer.parseInt(target.getText());
        target.setText(String.valueOf(current + lineCount));
    }

    private void syncBoardRows(GridPane field, int target, boolean front) {
        var rows = field.getRowConstraints();
        while (rows.size() < target) {
            RowConstraints rc = new RowConstraints();
            rc.setPrefHeight(12);
            rc.setMinHeight(12);
            rc.setMaxHeight(12);
            rc.setVgrow(Priority.NEVER);
            if (front) rows.addFirst(rc); else rows.add(rc);
        }
        while (rows.size() > target && rows.size() > 1) {
            if (front) rows.removeFirst(); else rows.removeLast();
        }
    }

    private void applyRemoteState(TetrisEngine.GameState s) {
        this.currentTwoBlockMode = s.isTwoBlockMode(); // Sync with Host
        syncBoardRows(player1Field, s.p1Grid().length, true);
        syncBoardRows(player2Field, s.p2Grid().length, false);
        render(s,2);
        render(s,1);
        player1PointsLabel.setText(String.valueOf(s.p1Score()));
        player2PointsLabel.setText(String.valueOf(s.p2Score()));
        p1LevelLabel.setText(String.valueOf(s.p1Level()));
        p2LevelLabel.setText(String.valueOf(s.p2Level()));
        if (s.powerUps() != null) {
            for (PowerUp old : currentPowerUps) {
                if (!s.powerUps().contains(old)) {
                    removePowerUP(old);
                }
            }
            showPowerUP(s.powerUps());
            currentPowerUps = s.powerUps();
        }

        if (s.gameOver() && !gameOverHandled) {
            gameOverHandled = true;
            ResultScreen controller = (ResultScreen) c.changeScene(
                    "/Views/Tetris/ResultScreen.fxml", header, vS);
            controller.handGameState(s, null, player1LinesLabel, player2LinesLabel);
            controller.setInitialLevels(initP1Level, initP2Level);
            if (flipped){
                controller.flip();
            }
            if (rainbowed){
                controller.rainbow();
            }
        }
    }

    // ----- Dual-engine LAN (each machine simulates only its own board) -----

    /**
     * Like {@link #create} but for the dual-engine LAN model: this machine builds a
     * full engine yet drives and ticks ONLY its own player ({@code controlledPlayer}),
     * so local input is applied instantly with no host round-trip. The opponent board
     * is rendered from snapshots, never simulated here.
     */
    public void createDual(String player1, String player2, int p1Level, int p2Level,
                           TetrisEngine engine, int controlledPlayer) {
        this.engine = engine;
        this.controlledPlayer = controlledPlayer;
        // Simulate only our own board; power-ups spawn here, opponent effects go over the wire.
        engine.setSoloPlayer(controlledPlayer);

        player1NameLabel.setText(player1);
        player2NameLabel.setText(player2);
        player1LinesLabel.setText("0");
        player2LinesLabel.setText("0");
        player1PointsLabel.setText("0");
        player2PointsLabel.setText("0");
        p1LevelLabel.setText(p1Level + "");
        p2LevelLabel.setText(p2Level + "");

        currentPowerUps = new ArrayList<>();
        loadImages();

        // Render my own board from my engine's events.
        engine.addListener(this);

        // Keyboard drives only my player -> immediate local response.
        handler = new KeyHandler(engine, tS, this, controlledPlayer);
        handler.attach(root.getScene());

        // Tick only my own board; the opponent ticks on their machine.
        if (controlledPlayer == 1) {
            p1EngineTicker = new Timeline(new KeyFrame(
                    Duration.millis(engine.getTickIntervalMs(1)), _ -> engine.tick(1)));
            p1EngineTicker.setCycleCount(Animation.INDEFINITE);
            p1EngineTicker.play();
        } else {
            p2EngineTicker = new Timeline(new KeyFrame(
                    Duration.millis(engine.getTickIntervalMs(2)), _ -> engine.tick(2)));
            p2EngineTicker.setCycleCount(Animation.INDEFINITE);
            p2EngineTicker.play();
        }

        // Show my starting board right away instead of waiting for the first tick.
        render(engine.getSnapshot(), controlledPlayer);
    }

    /**
     * Bidirectional bridge for the dual-engine model: ships my board snapshots to the
     * opponent, renders the opponent's snapshots, and coordinates game-over so both
     * sides agree once both players have topped out.
     */
    public void attachDualBridge(NetworkLayer network, int controlledPlayer) {
        if (engine == null || network == null) return;
        this.controlledPlayer = controlledPlayer;

        network.clearListeners();

        // Engine -> network: ship MY board to the opponent for display.
        engine.addListener(new TetrisEventListener() {
            @Override public void onTick(TetrisEngine.GameState s, int player) { sendMyBoard(network, s); }
            @Override public void onBlockLocked(int p, TetrisEngine.GameState s) { sendMyBoard(network, s); }
            @Override public void onLinesCleared(int p, int n, TetrisEngine.GameState s) {
                sendMyBoard(network, s);
                network.send(new TetrisMessage.LinesCleared(controlledPlayer, n));
            }
            @Override public void onLevelChanged(long ms, TetrisEngine.GameState s, int player) { sendMyBoard(network, s); }
            @Override public void onBlockMovement(TetrisEngine.GameState s, int player) { sendMyBoard(network, s); }
            @Override public void onReset(TetrisEngine.GameState s) { sendMyBoard(network, s); }
            @Override public void onStopped(TetrisEngine.GameState s) { sendMyBoard(network, s); }
            // Power-up spawn/pickup changes my board's power-up set; push it so the opponent's display updates.
            @Override public void onPowerUpSpawned(TetrisEngine.GameState s) { sendMyBoard(network, s); }
            @Override public void onPowerUpTriggered(TetrisEngine.GameState s, PowerUp p) { sendMyBoard(network, s); }
            // Swaps and board-change reshape my board; push it immediately rather than waiting for the next tick.
            @Override public void onBlockSwap(TetrisEngine.GameState s) { sendMyBoard(network, s); }
            @Override public void onBoardSizeChange(int p, int n, TetrisEngine.GameState s) { sendMyBoard(network, s); }
            // An opponent-targeted effect fired on my board -> deliver it for them to apply to theirs.
            @Override public void onOpponentAttack(AttackType type) { network.send(new TetrisMessage.Attack(type)); }
            // A PORTAL sent one of my blocks to the opponent's board.
            @Override public void onPortalOut(Block block) { network.send(new TetrisMessage.PortalBlock(block)); }
            // Board-change: I cleared lines, so the opponent shrinks their board.
            @Override public void onBoardShrinkOut(int rows) { network.send(new TetrisMessage.BoardShrink(rows)); }
            // Swap-active-blocks handshake.
            @Override public void onSwapActiveRequest(Block[] blocks) { network.send(new TetrisMessage.SwapActiveRequest(blocks)); }
            @Override public void onSwapActiveResponse(Block[] blocks) { network.send(new TetrisMessage.SwapActiveResponse(blocks)); }
            // Swap-boards handshake.
            @Override public void onSwapBoardsRequest(String[][] grid, Block[] blocks) { network.send(new TetrisMessage.SwapBoardsRequest(grid, blocks)); }
            @Override public void onSwapBoardsResponse(String[][] grid, Block[] blocks) { network.send(new TetrisMessage.SwapBoardsResponse(grid, blocks)); }
            @Override public void onPlayerLost(int p, TetrisEngine.GameState s) {
                sendMyBoard(network, s);
                int score = controlledPlayer == 1 ? s.p1Score() : s.p2Score();
                int lines = Integer.parseInt(
                        (controlledPlayer == 1 ? player1LinesLabel : player2LinesLabel).getText());
                network.send(new TetrisMessage.PlayerLost(controlledPlayer, score, lines));
                Platform.runLater(() -> { localLost = true; maybeFinishDual(); });
            }
        });

        // Network -> me: render the opponent board and reach game-over consensus.
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TetrisMessage.BoardState(TetrisEngine.GameState state, int senderPlayer)) {
                    // last-write-wins coalescing so the FX thread renders once per batch
                    if (pendingState.getAndSet(state) == null) {
                        Platform.runLater(() -> {
                            TetrisEngine.GameState latest = pendingState.getAndSet(null);
                            if (latest != null) renderOpponent(latest);
                        });
                    }
                } else if (msg instanceof TetrisMessage.LinesCleared(int playerNum, int lineCount)) {
                    Platform.runLater(() -> incrementLines(playerNum, lineCount));
                } else if (msg instanceof TetrisMessage.PlayerLost pl) {
                    Platform.runLater(() -> markOpponentLost(pl.finalScore(), pl.finalLines()));
                } else if (msg instanceof TetrisMessage.Attack a) {
                    Platform.runLater(() -> engine.applyIncomingAttack(a.type()));
                } else if (msg instanceof TetrisMessage.PortalBlock(Block block)) {
                    Platform.runLater(() -> engine.receivePortalBlock(block));
                } else if (msg instanceof TetrisMessage.BoardShrink(int rows)) {
                    Platform.runLater(() -> engine.applyBoardShrink(rows));
                } else if (msg instanceof TetrisMessage.SwapActiveRequest(Block[] blocks)) {
                    Platform.runLater(() -> engine.receiveSwapActiveRequest(blocks));
                } else if (msg instanceof TetrisMessage.SwapActiveResponse(Block[] blocks)) {
                    Platform.runLater(() -> engine.receiveSwapActiveResponse(blocks));
                } else if (msg instanceof TetrisMessage.SwapBoardsRequest(String[][] grid, Block[] blocks)) {
                    Platform.runLater(() -> engine.receiveSwapBoardsRequest(grid, blocks));
                } else if (msg instanceof TetrisMessage.SwapBoardsResponse(String[][] grid, Block[] blocks)) {
                    Platform.runLater(() -> engine.receiveSwapBoardsResponse(grid, blocks));
                }
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
        });

        // Push my starting board so the opponent sees it immediately, not after my first tick.
        sendMyBoard(network, engine.getSnapshot());
    }

    private void sendMyBoard(NetworkLayer network, TetrisEngine.GameState s) {
        network.send(new TetrisMessage.BoardState(s, controlledPlayer));
    }

    /** Render the opponent's half of a received snapshot into the opponent panel. */
    private void renderOpponent(TetrisEngine.GameState s) {
        int opp = 3 - controlledPlayer; // 1 -> 2, 2 -> 1
        boolean oppHalfLost;
        int oppScore;
        if (opp == 1) {
            syncBoardRows(player1Field, s.p1Grid().length, true);
            render(s, 1);
            player1PointsLabel.setText(String.valueOf(s.p1Score()));
            p1LevelLabel.setText(String.valueOf(s.p1Level()));
            oppHalfLost = s.p1Lost();
            oppScore = s.p1Score();
        } else {
            syncBoardRows(player2Field, s.p2Grid().length, false);
            render(s, 2);
            player2PointsLabel.setText(String.valueOf(s.p2Score()));
            p2LevelLabel.setText(String.valueOf(s.p2Level()));
            oppHalfLost = s.p2Lost();
            oppScore = s.p2Score();
        }

        renderOppPowerUps(s.powerUps());

        // Backstop for game-over: the opponent's loss is also carried by their board
        // snapshots, which stream continuously. So even if the discrete PlayerLost
        // message is dropped or arrives out of order, this still finishes the game.
        if (oppHalfLost) {
            int oppLines = Integer.parseInt(
                    (opp == 1 ? player1LinesLabel : player2LinesLabel).getText());
            markOpponentLost(oppScore, oppLines);
        }
    }

    /**
     * Render the opponent's power-ups onto the opponent panel, diffing against what we
     * last showed. Kept separate from showPowerUP/currentPowerUps (which manage MY own
     * board) so the two power-up displays never clobber one another.
     */
    private void renderOppPowerUps(List<PowerUp> incoming) {
        if (incoming == null) incoming = new ArrayList<>();
        GridPane field = (controlledPlayer == 1) ? player2Field : player1Field;
        for (PowerUp old : oppPowerUps) {
            if (!incoming.contains(old)) removePowerUP(old);
        }
        for (PowerUp p : incoming) {
            if (!oppPowerUps.contains(p)) {
                Rectangle rect = new Rectangle(13, 13);
                Image img = getImage(p);
                if (img != null) rect.setFill(new ImagePattern(img));
                else rect.setFill(Color.YELLOW);
                rect.getStyleClass().add("PowerUp");
                field.add(rect, p.getCol(), p.getRow());
            }
        }
        oppPowerUps = new ArrayList<>(incoming);
    }

    /**
     * Record that the opponent has topped out (learned from either the explicit
     * PlayerLost message or their board snapshot) and try to finish. Capture-once,
     * so whichever signal arrives first sets the opponent's final totals.
     */
    private void markOpponentLost(int finalScore, int finalLines) {
        if (!oppLost) {
            oppLost = true;
            oppFinalScore = finalScore;
            oppFinalLines = finalLines;
        }
        maybeFinishDual();
    }

    /** Once both players have topped out, both machines move to the result screen. */
    private void maybeFinishDual() {
        if (!(localLost && oppLost) || gameOverHandled) return;
        gameOverHandled = true;

        if (p1EngineTicker != null) p1EngineTicker.stop();
        if (p2EngineTicker != null) p2EngineTicker.stop();
        if (handler != null) handler.stop();
        if (engine != null) engine.stop();

        TetrisEngine.GameState mine = engine.getSnapshot();
        int myScore = controlledPlayer == 1 ? mine.p1Score() : mine.p2Score();
        int p1Score = controlledPlayer == 1 ? myScore : oppFinalScore;
        int p2Score = controlledPlayer == 2 ? myScore : oppFinalScore;

        // Pin the opponent's final lines so the result screen totals are exact.
        if (controlledPlayer == 1) player2LinesLabel.setText(String.valueOf(oppFinalLines));
        else player1LinesLabel.setText(String.valueOf(oppFinalLines));

        TetrisEngine.GameState merged = new TetrisEngine.GameState(
                mine.p1Grid(), mine.p2Grid(), mine.p1ActiveBlocks(), mine.p2ActiveBlocks(),
                p1Score, p2Score, mine.p1Level(), mine.p2Level(),
                mine.p1Name(), mine.p2Name(), true, true, true,
                new ArrayList<>(), mine.isTwoBlockMode());

        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Tetris/ResultScreen.fxml", header, vS);
        controller.handGameState(merged, null, player1LinesLabel, player2LinesLabel);
        controller.setInitialLevels(initP1Level, initP2Level);
        if (flipped) controller.flip();
        if (rainbowed) controller.rainbow();
    }

    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;

        // Null-safe per ticker: in dual-engine mode only one of the two tickers
        // exists (each machine ticks only its own player), so stopping them must
        // not assume both are present.
        if (p1EngineTicker != null) p1EngineTicker.stop();
        if (p2EngineTicker != null) p2EngineTicker.stop();
        if (engine != null) engine.stop();
        if (handler != null) handler.stop();
        if (clientRepeatTimer != null) clientRepeatTimer.stop();

        // Only navigate if we're still attached to a window; if the scene was already
        // swapped out, skip it instead of crashing changeScene with a null window.
        javafx.stage.Window owner = (header.getScene() != null) ? header.getScene().getWindow() : null;
        Session.clear();
        vS.emtyStack();
        if (owner != null) {
            c.changeScene("/Views/StartingScreen.fxml", header, vS);
        }

        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason + "\n\nReturning to the main menu.",
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }
    public void setInitialLevels(int initP1Level,int initP2Level){
        this.initP1Level = initP1Level;
        this.initP2Level = initP2Level;
    }
}
