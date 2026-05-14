package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.*;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.util.List;

public class GameScreen extends Controller implements TetrisEventListener {
    private TetrisSettings tS = TetrisSettings.getInstance();
    private TetrisEngine engine;
    private int speed = 500;
    private KeyHandler handler;
    private Timeline engineTicker;
    @FXML
    private VBox header;

    @FXML
    private GridPane player1Field, player2Field;

    @FXML
    private Label player1NameLabel, player2NameLabel, player1PointsLabel, player2PointsLabel, player1LinesLabel, player2LinesLabel,LevelLabel;

    @FXML
    protected void onExitGameAction(ActionEvent event) {
        Session.clear();
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

    public void render(){
        TetrisEngine.GameState state = engine.getSnapshot();
        setPlayerPoints(1, String.valueOf(state.p1Score()));
        setPlayerPoints(2, String.valueOf(state.p2Score()));
        //setPlayerLines(1, String.valueOf(state.p1Lines()));
        //setPlayerLines(2, String.valueOf(state.p2Lines()));
        drawGrid(state.p1Grid(),state.p1ActiveBlock(), player1Field);
        drawGrid(state.p2Grid(),state.p2ActiveBlock(), player2Field);

    }

    private void drawGrid(String[][] colors, Block activeBlock, GridPane grid) {
        grid.getChildren().clear();
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                if (colors[i][j] != null){
                    Rectangle rect = new Rectangle(12,12);
                    rect.setFill(Color.web(colors[i][j]));
                    grid.add(rect, j, i);
                }
            }
        }

        boolean[][] block = activeBlock.getShape();
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                if (block[i][j]){
                    Rectangle rect = new Rectangle(12,12);
                    rect.setFill(Color.web(activeBlock.getHexColor()));
                    grid.add(rect, j + activeBlock.getX(), i+activeBlock.getY());
                }
            }

        }

    }


    public void create(String player1, String player2, boolean multiplayer, TetrisEngine engine) {
        this.engine = engine;
        engine.addListener(this);
        player1NameLabel.setText(player1);
        player2NameLabel.setText(player2);
        player1LinesLabel.setText("0");
        player2LinesLabel.setText("0");
        player1PointsLabel.setText("0");
        player2PointsLabel.setText("0");

        handler = new KeyHandler(engine,tS, this);
        handler.attach(header.getScene());

        engineTicker = new Timeline(
                new KeyFrame(
                        Duration.millis(engine.getTickIntervalMs()),
                        e -> engine.tick()
                )
        );
        engineTicker.setCycleCount(Animation.INDEFINITE);
        engineTicker.play();
    }


    @Override
    public void onTick(TetrisEngine.GameState snapshot) {
        render();
    }

    @Override
    public void onBlockLocked(int playerNum, TetrisEngine.GameState snapshot) {
        setPlayerPoints(playerNum, String.valueOf(
                playerNum == 1 ? snapshot.p1Score() : snapshot.p2Score()
        ));
    }

    @Override
    public void onLinesCleared(int playerNum, int lineCount, TetrisEngine.GameState snapshot) {
        if (playerNum == 1 ){
            int current = Integer.parseInt(player1LinesLabel.getText());
            setPlayerLines(playerNum, String.valueOf(current+lineCount));
        }
        else {
            int current = Integer.parseInt(player2LinesLabel.getText());
            setPlayerLines(playerNum, String.valueOf(current+lineCount));
        }

    }

    @Override
    public void onLevelChanged(int newLevel, long newTickIntervalMs, TetrisEngine.GameState snapshot) {
        engineTicker.stop();
        engineTicker.getKeyFrames().setAll(
                new KeyFrame(Duration.millis(newTickIntervalMs), e -> engine.tick())
        );
        engineTicker.play();
        LevelLabel.setText(String.valueOf(newLevel));
    }

    @Override
    public void onPlayerLost(int playerNum, TetrisEngine.GameState snapshot) {
        //TODO Reflect this in the UI
    }

    @Override
    public void onGameOver(TetrisEngine.GameState snapshot) {
        engineTicker.stop();
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Tetris/ResultScreen.fxml", header, vS);
        controller.handGameState(snapshot, engine, player1LinesLabel,player2LinesLabel);
    }

    @Override
    public void onPowerUpTriggered(int triggeringPlayer, TetrisEngine.GameState snapshot) {
        render();
    }

    @Override
    public void onPowerUpSpawned(List<PowerUp> activePowerUps){
        //TODO finish the visual representation of powerups
        for (PowerUp p : activePowerUps){
            if (p.playerNum() == 1){

            }
            else if (p.playerNum() == 2){

            }
        }
    }
}
