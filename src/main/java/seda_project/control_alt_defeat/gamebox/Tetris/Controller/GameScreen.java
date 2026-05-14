package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.*;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class GameScreen extends Controller {
    private TetrisSettings tS;
    private TetrisEngine engine;
    private int speed = 500;
    private KeyHandler handler;
    private Timeline gameloop;
    @FXML
    private VBox header;

    @FXML
    private GridPane player1Field, player2Field;

    @FXML
    private Label player1NameLabel, player2NameLabel, player1PointsLabel, player2PointsLabel, player1LinesLabel, player2LinesLabel;

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
        GameState state = engine.getSnapshot();
        setPlayerPoints(1, String.valueOf(state.player1Score()));
        setPlayerPoints(2, String.valueOf(state.player2Score()));
        drawGrid(state.player1Grid(),state.player1ActiveBlock(), player1Field);
        drawGrid(state.player2Grid(),state.player2ActiveBlock(), player2Field);

    }

    private void drawGrid(boolean[][] booleans, Block activeBlock, GridPane grid) {
        grid.getChildren().clear();
        for (int i = 0; i < booleans.length; i++) {
            for (int j = 0; j < booleans[i].length; j++) {
                if (booleans[i][j]){
                    Rectangle rect = new Rectangle(12,12);
                    rect.setFill(Color.RED);
                    grid.add(rect, j, i);
                }
            }
        }
        if (activeBlock != null ){
            boolean[][] shape = activeBlock.getShape();
            for (int i = 0; i < shape.length ; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j]){
                        Rectangle rect = new Rectangle(12,12);
                        rect.setFill(activeBlock.getColor());
                        grid.add(rect, activeBlock.getX() +j , activeBlock.getY() +i );
                    }
                }
                
            }
        }
    }

    //Method to set the initial State
    public void create(String player1, String player2, boolean multiplayer) {
        player1NameLabel.setText(player1);
        player2NameLabel.setText(player2);
        player1LinesLabel.setText("0");
        player2LinesLabel.setText("0");
        player1PointsLabel.setText("0");
        player2PointsLabel.setText("0");

        tS = TetrisSettings.getInstance();

        engine = new TetrisEngine(player1, player2);

        handler = new KeyHandler(engine,tS);
        handler.attach(header.getScene());

        gameloop = new Timeline(
                new KeyFrame(
                        javafx.util.Duration.millis(speed),
                        e -> {
                            engine.tick();
                            render();
                            checkEnd();
                        }
                )
        );
        gameloop.setCycleCount(javafx.animation.Animation.INDEFINITE);
        gameloop.play();
    }

    private void checkEnd() {
        GameState state = engine.getSnapshot();
        if (state.isGameOver()) {
            gameloop.stop();
            c.changeScene("/Views/Tetris/ResultScreen.fxml",header,vS);
        }
    }
}
