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

    private Image swapImage, portalImage, swapBlockImage;
    private Image radialBombImage, columnBombImage;

    private List<PowerUp> currentPowerUps;


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
            drawGrid(state.p1Grid(), state.p1ActiveBlock(), player1Field, state.p1Lost());
        }
        else {
            drawGrid(state.p2Grid(), state.p2ActiveBlock(), player2Field, state.p2Lost());
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

    private void drawGrid(String[][] colors, Block activeBlock, GridPane grid, boolean isLost) {
        grid.getChildren().removeIf(node -> !(node instanceof Rectangle r
                && r.getStyleClass().contains("PowerUp")));

        int maxRows = grid.getRowConstraints().size();

        for (int i = 0; i < colors.length; i++) {
            if (i>= maxRows) break;
            for (int j = 0; j < colors[i].length; j++) {
                if (colors[i][j] != null){
                    Rectangle rect = new Rectangle(12,12);
                    if (isLost){
                        rect.setFill(Color.LIGHTGRAY);
                    }
                    else {
                        rect.setFill(Color.web(colors[i][j]));
                    }
                    grid.add(rect, j, i);
                }
            }
        }
        boolean[][] block = activeBlock.getShape();
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                if (block[i][j]) {
                    Rectangle rect = new Rectangle(12, 12);
                    if (activeBlock instanceof BombBlock bb) {
                        rect.setFill(new ImagePattern((bb.getType() == BombType.RADIUS) ? radialBombImage : columnBombImage));
                    }
                    else {
                        if (isLost) {
                            rect.setFill(Color.LIGHTGRAY);
                        } else {
                            rect.setFill(Color.web(activeBlock.getHexColor()));
                        }
                    }
                    grid.add(rect, j + activeBlock.getX(), i + activeBlock.getY());
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
        System.out.println("P1 Ticks " +  engine.getTickIntervalMs(1));
        p2EngineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs(2)),
                        e -> engine.tick(2)
                )
        );
        p2EngineTicker.setCycleCount(Animation.INDEFINITE);
        p2EngineTicker.play();
        System.out.println("P2 Ticks " + engine.getTickIntervalMs(2));

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
                System.out.println("REMOVED IN FIELD 1");
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
        System.out.println("Level UP in GameScreeen" + snapshot.p1Level() + " " + snapshot.p2Level());
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

    public void test(){
        System.out.println("MOVEMENT");
    }

    public void attachHostNetworkBridge(NetworkLayer network) {
        if (engine == null || network == null) return;

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
                test();
                network.send(new TetrisMessage.StateUpdate(s)); }

            @Override public void onStopped(TetrisEngine.GameState s){
                network.send(new TetrisMessage.StateUpdate(s));
            }
        });

        // Network to engine: Apply remote inputs 
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TetrisMessage.Input in) {
                    engine.processInput(in.playerNum(), in.action().name());
                }
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
        });
    }

    private int count = 0;
    public void attachClientNetworkBridge(NetworkLayer network) {
        if (engine != null || network == null) return; // only valid in client mode

        // network to UI
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TetrisMessage.StateUpdate update) {
                    Platform.runLater(() -> applyRemoteState(update.state()));
                } else if (msg instanceof TetrisMessage.LinesCleared lc) {
                    Platform.runLater(() -> incrementLines(lc.playerNum(), lc.lineCount()));
                }
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnect(reason));
            }
        });

        // keyboard to network
        // the client is the only player on this machineso from the hosts perspective the
        // client is always player 2, so thats what we send
        Scene scene = header.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                TetrisMessage.InputAction action = mapClientKey(event.getCode());
                if (action != null) {
                    network.send(new TetrisMessage.Input(2, action));
                    event.consume();
                }
            });
        }
    }

    private TetrisMessage.InputAction mapClientKey(KeyCode key) {
        // Mirror KeyHandlers order
        java.util.ArrayList<KeyCode> p1 = tS.getPlayer1Keys();
        if (key == p1.get(0)) return TetrisMessage.InputAction.LEFT;
        if (key == p1.get(1)) return TetrisMessage.InputAction.RIGHT;
        if (key == p1.get(2)) return TetrisMessage.InputAction.DROP;
        if (key == p1.get(3)) return TetrisMessage.InputAction.ROTATE;
        return null;
    }

    private void incrementLines(int playerNum, int lineCount) {
        Label target = playerNum == 1 ? player1LinesLabel : player2LinesLabel;
        int current = Integer.parseInt(target.getText());
        target.setText(String.valueOf(current + lineCount));
    }

    private void applyRemoteState(TetrisEngine.GameState s) {
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
