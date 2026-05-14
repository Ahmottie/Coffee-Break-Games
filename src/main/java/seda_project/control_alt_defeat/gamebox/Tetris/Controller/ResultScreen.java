package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {
    TetrisEngine.GameState state;
    TetrisEngine engine;

    @FXML
    private VBox header;

    @FXML
    private Label PointsWinnerLabel, PointsLoserLabel, LinesWinnerLabel, LinesLoserLabel, positionLabel1, positionLabel2,NameWinnerLabel, NameLoserLabel;

    @FXML
    protected void onPlayAgainAction(){
        GameScreen controller = (GameScreen) c.changeScene("/Views/Tetris/GameScreen.fxml",header,vS);
        controller.create(state.p1Name(),state.p2Name(),false, engine);
    }

    @FXML
    protected void onExitGameAction(){
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    public void handGameState(TetrisEngine.GameState state, TetrisEngine engine, Label p1Lines, Label p2Lines) {
        this.engine = engine;
        this.state = state;
        int scorep1 = state.p1Score();
        int scorep2 = state.p2Score();
        int linesp1 = Integer.parseInt(p1Lines.getText());
        int linesp2 = Integer.parseInt(p2Lines.getText());
        
        boolean p1Wins = scorep1 >= scorep2;

        String winnerName  = p1Wins ? state.p1Name() : state.p2Name();
        String loserName   = p1Wins ? state.p2Name() : state.p1Name();
        int    winnerScore = p1Wins ? scorep1 : scorep2;
        int    loserScore  = p1Wins ? scorep2 : scorep1;
        int    winnerLines = p1Wins ? linesp1 : linesp2;
        int    loserLines  = p1Wins ? linesp2 : linesp1;

        NameWinnerLabel.setText(winnerName);
        NameLoserLabel.setText(loserName);
        PointsWinnerLabel.setText(winnerScore + "");
        PointsLoserLabel.setText(loserScore + "");
        LinesWinnerLabel.setText(winnerLines + "");
        LinesLoserLabel.setText(loserLines + "");

        if (scorep1 == scorep2) {
            positionLabel1.setText("DRAW");
            positionLabel2.setText("DRAW");
        }
    }
}
