package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
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

public class GameScreen extends Controller implements TetrisEventListener {
    private TetrisSettings tS = TetrisSettings.getInstance();
    private TetrisEngine engine;
    private KeyHandler handler;
    private Timeline engineTicker;
    private int initP1Level, initP2Level;

    private Timeline p1EngineTicker;
    private Timeline p2EngineTicker;

    private boolean disconnected = false;
    private boolean gameOverHandled = false;

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
        var stream = getClass().getResource("/Images/Tetris/Swap.png").toExternalForm();
        if (stream != null) {
            swapImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/Portal.png").toExternalForm();
        if (stream != null) {
            portalImage =  new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/SwapBlocks.png").toExternalForm();
        if ( stream != null) {
            swapBlockImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/DecreaseRotationOpponent.png").toExternalForm();
        if ( stream != null) {
            decreaseRotationOpponentImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/DecreaseRotationSelf.png").toExternalForm();
        if ( stream != null) {
            decreaseRotationSelfImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/DecreaseTickOpponent.png").toExternalForm();
        if ( stream != null) {
            decreaseTickOpponentImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/DecreaseTickSelf.png").toExternalForm();
        if ( stream != null) {
            decreaseTickSelfImage = new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/IncreaseTickOpponent.png").toExternalForm();
        if ( stream != null) {
            increaseTickOpponentImage = new Image(stream);
        }
    }

    @FXML
    private VBox header;

    @FXML
    private GridPane player1Field, player2Field;

    @FXML
    private Label player1NameLabel, player2NameLabel, player1PointsLabel, player2PointsLabel, player1LinesLabel, player2LinesLabel,p1LevelLabel, p2LevelLabel;

    @FXML
    protected void onExitGameAction(ActionEvent event) {
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
            drawGrid(state.p1Grid(), state.p1ActiveBlocks(), player1Field, state.p1Lost());
        } else {
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


    public void create(String player1, String player2, int p1Level, int p2Level, boolean multiplayer, TetrisEngine engine) {
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

        // LAN-client mode: no engine, no KeyHandler etc
        // client renders snapshots received over the network and forwards key events as input msg
        if (engine == null) {
            return;
        }

        engine.addListener(this);
        // In LAN host mode this keyboard only drives player 1; player 2 input
        // arrives from the client over the network.
        boolean lanHost = Session.current().network != null;
        handler = new KeyHandler(engine, tS, this, lanHost);
        handler.attach(header.getScene());

        p1EngineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs(1)),
                        e -> engine.tick(1)
                )
        );
        p1EngineTicker.setCycleCount(Animation.INDEFINITE);
        p1EngineTicker.play();
        p2EngineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs(2)),
                        e -> engine.tick(2)
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
        var stream = getClass().getResource("/Images/Tetris/RadialBomb.png").toExternalForm();
        if (stream != null) {
            radialBombImage =  new Image(stream);
        }
        stream = getClass().getResource("/Images/Tetris/ColumnBomb.png").toExternalForm();
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
    }

    @Override
    public void onBoardSizeChange (int playerNum, int linesCleared, TetrisEngine.GameState snapshot) {
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
        Timeline timeline = player == 1 ? p1EngineTicker : p2EngineTicker;
        if (timeline != null) {
            timeline.stop();
            timeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.millis(newTickIntervalMs), e -> engine.tick(player))
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
                    new KeyFrame(Duration.millis(newTickSpeed), e -> engine.tick(playerNum))
            );
            timeline.play();
        }
    }

    @Override
    public void onPlayerLost(int playerNum, TetrisEngine.GameState snapshot) {
        render(snapshot, playerNum);
    }

    @Override
    public void onGameOver(TetrisEngine.GameState snapshot) {
        if (p1EngineTicker != null){
            p1EngineTicker.stop();
            p2EngineTicker.stop();
        }
        if (handler != null) handler.stop();
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Tetris/ResultScreen.fxml", header, vS);
        controller.setInitialLevels(initP1Level,initP2Level);
        controller.handGameState(snapshot, engine, player1LinesLabel,player2LinesLabel);
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
        if (p1EngineTicker != null){
            p1EngineTicker.stop();
            p2EngineTicker.stop();
        }
        if (handler != null) handler.stop();
    }

    @Override
    public void onBlockMovement (TetrisEngine.GameState snapshot, int player) {
        render(snapshot, player);
    }

    @Override
    public void onBlockSwap(TetrisEngine.GameState snapshot){
        render(snapshot, 1);
        render(snapshot,2);
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
                if (msg instanceof TetrisMessage.StateUpdate update) {
                    // AI-generated optimization: store latest snapshot; only schedule a
                    // render if one isn't already pending (last-write-wins).
                    if (pendingState.getAndSet(update.state()) == null) {
                        Platform.runLater(() -> {
                            TetrisEngine.GameState latest = pendingState.getAndSet(null);
                            if (latest != null) applyRemoteState(latest);
                        });
                    }
                } else if (msg instanceof TetrisMessage.LinesCleared lc) {
                    Platform.runLater(() -> incrementLines(lc.playerNum(), lc.lineCount()));
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

            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                clientActiveKeys.remove(event.getCode());
            });

            clientRepeatTimer = new Timeline(new KeyFrame(Duration.millis(20), e -> {
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
        }
    }

    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;

        if (p1EngineTicker != null){
            p1EngineTicker.stop();
            p2EngineTicker.stop();
        }
        if (engine != null) engine.stop();
        if (handler != null) handler.stop();
        if (clientRepeatTimer != null) clientRepeatTimer.stop();

        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason + "\n\nReturning to the main menu.",
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();

        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml", header, vS);
    }
    public void setInitialLevels(int initP1Level,int initP2Level){
        this.initP1Level = initP1Level;
        this.initP2Level = initP2Level;
    }
}
