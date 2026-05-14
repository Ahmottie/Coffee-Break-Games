package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.GameState;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {
    GameState state;

    @FXML
    private VBox header;

    @FXML
    private Label PointsWinnerLabel, PointsLoserLabel, LinesWinnerLabel, LinesLoserLabel, positionLabel1, positionLabel2,NameWinnerLabel, NameLoserLabel;

    @FXML
    protected void onPlayAgainAction(){
        GameScreen controller = (GameScreen) c.changeScene("/Views/Tetris/GameScreen.fxml",header,vS);
        controller.create(state.player1Name(),state.player2Name(),false);
    }

    @FXML
    protected void onExitGameAction(){
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    public void handGameState(GameState state) {
        this.state = state;
        int scorep1 = state.player1Score();
        int scorep2 = state.player2Score();

        if (scorep1 > scorep2) {
            NameWinnerLabel.setText(state.player1Name());
            NameLoserLabel.setText(state.player2Name());
            PointsWinnerLabel.setText(scorep1 +"");
            PointsLoserLabel.setText(scorep2 +"");
        }else if (scorep2 > scorep1){
            NameWinnerLabel.setText(state.player2Name());
            NameLoserLabel.setText(state.player1Name());
            LinesWinnerLabel.setText(scorep2 +"");
            LinesLoserLabel.setText(scorep1 +"");
        }
        else{
            NameWinnerLabel.setText(state.player1Name());
            NameLoserLabel.setText(state.player2Name());
            PointsWinnerLabel.setText(scorep1 +"");
            PointsLoserLabel.setText(scorep2 +"");
            LinesWinnerLabel.setText(state.player1Lines() +"");
            LinesLoserLabel.setText(state.player2Lines() +"");
            positionLabel1.setText("DRAW");
            positionLabel2.setText("DRAW");
        }
    }
}
