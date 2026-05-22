package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.Tetris.network.TetrisMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {
    TetrisEngine.GameState state;
    TetrisEngine engine;
    private NetworkLayer network;
    private boolean disconnected = false;

    private int initP1Level,initP2Level;

    @FXML
    private VBox header;

    @FXML
    private Label PointsWinnerLabel, PointsLoserLabel, LinesWinnerLabel, LinesLoserLabel, positionLabel1, positionLabel2,NameWinnerLabel, NameLoserLabel;

    @FXML
    private Button playAgainButton;

    @FXML
    protected void onPlayAgainAction(){
        Session s = Session.current();

        // Local mode so original behaviour fresh engine and game
        if (s.network == null) {
            System.out.println(initP1Level);
            System.out.println(initP2Level);
            TetrisEngine fresh = new TetrisEngine(
                    state.p1Name(), state.p2Name(),initP1Level,initP2Level, BlockRegistry.getInstance(), TetrisAdvancedSettings.getInstance());
            GameScreen controller = (GameScreen) c.changeScene(
                    "/Views/Tetris/GameScreen.fxml", header, vS);
            controller.create(state.p1Name(), state.p2Name(), initP1Level,initP2Level,false, fresh);
            controller.setInitialLevels(initP1Level,initP2Level);
            return;
        }

        // LAN host: build new engine, broadcast Restart and navigate to lan game
        TetrisEngine fresh = new TetrisEngine(
                state.p1Name(), state.p2Name(), s.myLevel,s.peerLevel,BlockRegistry.getInstance(), TetrisAdvancedSettings.getInstance());
        s.tetrisEngine = fresh;
        s.localReady = false;
        s.peerReady  = false;

        s.network.send(new TetrisMessage.Restart());
        navigateToLanGame(fresh);
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

        // disable client play again button, only host can do that 
        Session s = Session.current();
        if (s.network != null) {
            this.network = s.network;
            if (!s.isHost) {
                playAgainButton.setText("Waiting for host...");
                playAgainButton.setDisable(true);
            }
            this.network.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof TetrisMessage.Restart) {
                        // Client side: host wants a new game
                        // the new GameScreen renders received state
                        Platform.runLater(() -> navigateToLanGame(null));
                    }
                }
                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> handleDisconnect(reason));
                }
            });
        }
    }


    private void navigateToLanGame(TetrisEngine engineForHost) {
        Session s = Session.current();
        GameScreen controller = (GameScreen) c.changeScene(
                "/Views/Tetris/GameScreen.fxml", header, vS);

        if (s.isHost) {
            controller.create(state.p1Name(), state.p2Name(), s.myLevel,s.peerLevel,true, engineForHost);
            controller.attachHostNetworkBridge(s.network);
        } else {
            System.out.println(s.network);
            controller.create(s.peerName, s.myName, s.peerLevel,s.myLevel,true, engineForHost);
            controller.attachClientNetworkBridge(s.network);
        }
    }

    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;
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
