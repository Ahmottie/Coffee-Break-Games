package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class ResultScreen extends Controller {

    @FXML
    private VBox header;

    @FXML
    private Label p1NameLabel, p2NameLabel, p1Score, p2Score, p1Awarded, p2Awarded, resultLabel;

    public void initNetwork() {
        Session s = Session.current();
        if (s.network != null) {
            // Reset ready states for the new lobby phase
            s.localReady = false;
            s.peerReady = false;

            s.network.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof ChessMessage.Ready r) {
                        Platform.runLater(() -> {
                            s.peerReady = r.ready();
                            checkStart();
                        });
                    } else if (msg instanceof ChessMessage.StartCountdown sc) {
                        Platform.runLater(() -> scheduleStartGame(sc.delayMs()));
                    }
                }

                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> {
                        resultLabel.setText("Opponent disconnected.");
                    });
                }
            });
        }
    }

    private void checkStart() {
        Session s = Session.current();
        if (s.isHost && s.localReady && s.peerReady) {
            long delayMs = 3000;
            s.network.send(new ChessMessage.StartCountdown(delayMs));
            scheduleStartGame(delayMs);
        }
    }

    private void scheduleStartGame(long delayMs) {
        resultLabel.setText("Restarting in " + (delayMs / 1000) + "s...");
        PauseTransition pt = new PauseTransition(Duration.millis(delayMs));
        pt.setOnFinished(e -> startGameNow());
        pt.play();
    }

    public void handData(String p1Name, String p2Name, String p1Points, String p2Points){
        p1NameLabel.setText(p1Name);
        p2NameLabel.setText(p2Name);
        p1Score.setText(""+p1Points);
        p2Score.setText(""+p2Points);
    }

    public void winner(int winner){
        resultLabel.setText("Checkmate");
        onePoint(winner);
    }

    public void remis(){
        resultLabel.setText("Remis");
        halfPoint();
    }

    public void stalemate(int winner){
        resultLabel.setText("Stalemate");
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .75));
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .25));
            p1Awarded.setText("+0.75");
            p2Awarded.setText("+0.25");
        } else {
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .75));
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .25));
            p1Awarded.setText("+0.25");
            p2Awarded.setText("+0.75");
        }
    }

    public void onExitAction(ActionEvent actionEvent) {
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    public void onPlayAgainAction(ActionEvent actionEvent) {
        Session s = Session.current();

        if (s.network != null) {
            s.localReady = true;
            s.network.send(new ChessMessage.Ready(true));

            // Give the user visual feedback that they are waiting
            resultLabel.setText("Waiting for opponent...");

            // In case the peer was already ready
            checkStart();
        } else {
            // Local offline play: skip the network wait and start immediately
            startGameNow();
        }
    }

    private void startGameNow() {
        Session s = Session.current();

        if (s.network != null) {
            s.network.clearListeners();
        }

        GameEngine engine = new GameEngine();
        s.chessEngine = engine;

        GameScreen controller = (GameScreen) c.changeScene("/Views/HexChess/GameScreen.fxml", header, vS);
        controller.setGameEngine(engine);
        controller.init();

        if (s.network != null) {
            if (s.isHost) {
                controller.attachHostBridge(s.network, engine);
            } else {
                controller.attachClientBridge(s.network, engine);
            }
        }

        controller.setNames(p1NameLabel.getText(), p2NameLabel.getText());
        controller.setPoints(Double.parseDouble(p1Score.getText()), Double.parseDouble(p2Score.getText()));
    }

    public void draw() {
        resultLabel.setText("Draw Proposal");
        halfPoint();
    }

    public void resign(int winner) {
        resultLabel.setText("Resing");
        onePoint(winner);
    }

    private void onePoint(int winner) {
        if ((winner == 1)) {
            p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + 1.0));
            p1Awarded.setText("+1.0");
            p2Awarded.setText("+0.0");
        } else {
            p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + 1.0));
            p1Awarded.setText("+0.0");
            p2Awarded.setText("+1.0");
        }
    }

    private void halfPoint() {
        p1Score.setText(String.valueOf(Double.parseDouble(p1Score.getText()) + .5));
        p2Score.setText(String.valueOf(Double.parseDouble(p2Score.getText()) + .5));
        p1Awarded.setText("+0.5");
        p2Awarded.setText("+0.5");
    }
}
