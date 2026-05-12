package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.Controller.GameScreen;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Tetris.Enginge.TetrisSettings;
import seda_project.control_alt_defeat.gamebox.network.*;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class WaitForOpponent extends Controller {
    private Timeline timeline;
    private String joinName;
    private boolean ready;

    @FXML
    private VBox header;

    @FXML
    private Label yourNameLabel,statusLabel,hostIpAddressLabel,opponentNameLabel;

    @FXML
    private Button startButton;

    @FXML
    public void onBackAction(){
        c.backScene(header,vS);
    }

    public void onStartGameAction(){
        //TODO IMPLEMENT
    }

    public void passHostData(String hostName){
        //TODO Complete

        yourNameLabel.setText(hostName);
        startButton.setText("Ready");

        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> opponentNameLabel.setText("")),
                new KeyFrame(Duration.seconds(0.5), e -> opponentNameLabel.setText("o")),
                new KeyFrame(Duration.seconds(1),   e -> opponentNameLabel.setText("oo")),
                new KeyFrame(Duration.seconds(1.5), e -> opponentNameLabel.setText("ooo")),
                new KeyFrame(Duration.seconds(2),   e -> opponentNameLabel.setText("ooo"))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        statusLabel.setText("Waiting for an opponent to Join!");

        LanHost.hostAsync(Lan.DEFAULT_PORT,
                layer -> Platform.runLater(() -> {
                    Session.current().network = layer;
                    attachHostListener(layer);
                }),
                err -> Platform.runLater(() -> {
                    statusLabel.setText("Hosting failed: " + err.getMessage());
                    err.printStackTrace();
                })
        );
        hostIpAddressLabel.setText(Lan.localIp());
    }

    public void passJoinData(String playerName,String ipAddress){
        this.ready = false;
        this.joinName = playerName;
        yourNameLabel.setText(playerName);
        startButton.setText("Ready");
        statusLabel.setText("Waiting for game info from host...");

        NetworkLayer layer = Session.current().network;
        if (layer == null) {
            statusLabel.setText("No connection to host.");
            return;
        }
        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(GameMessage msg) {
                Platform.runLater(() -> handleJoinMessage(msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> statusLabel.setText("Disconnected: " + reason));
            }
        });

        // Greet the host
        layer.send(new GameMessage.Hello(playerName));
    }

    private void handleJoinMessage(GameMessage msg) {
        if (msg instanceof GameMessage.LobbyConfig lc) {

            //TODO Adapt to the needed Settings and Setup
            //Session.current().tetrisConfig = lc.config();
            //Session.current().tetrisSetup  = lc.setup();

            opponentNameLabel.setText(lc.config().player1Name());
            statusLabel.setText("Press Ready to start!");
        } else if (msg instanceof GameMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
        } else if (msg instanceof GameMessage.StartCountdown sc) {
            scheduleStartGame(sc.delayMs());
        }
    }


    private void attachHostListener(NetworkLayer layer) {
        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(GameMessage msg) {
                Platform.runLater(() -> handleHostMessage(layer, msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> statusLabel.setText("Opponent disconnected: " + reason));
            }
        });
    }

    private void handleHostMessage(NetworkLayer layer, GameMessage msg) {
        if (msg instanceof GameMessage.Hello h) {
            // Now we know the joiner's name. Patch config + setup so they
            // reflect reality (instead of the "Opponent" placeholder).
            GameConfig oldCfg = Session.current().config;
            GameConfig newCfg = new GameConfig(
                    oldCfg.k(), oldCfg.deckSize(),
                    oldCfg.player1Name(), h.playerName());

            GameSetup oldSetup = Session.current().setup;
            String first = oldSetup.firstPlayer().equals("Opponent")
                    ? h.playerName()
                    : oldSetup.firstPlayer();
            GameSetup newSetup = new GameSetup(oldSetup.initialDeck(), first);

            Session.current().config = newCfg;
            Session.current().setup  = newSetup;

            // Tell the joiner everything they need to start the same engine
            layer.send(new GameMessage.LobbyConfig(newCfg, newSetup));

            // Update host UI
            playerJoin(h.playerName());
        } else if (msg instanceof GameMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
            maybeStartCountdown();
        }
    }

    private void maybeStartCountdown() {
        Session s = Session.current();
        if (!s.isHost) return;                          // only the host triggers
        if (!(s.localReady && s.peerReady)) return;     // need both sides ready

        long delayMs = 3000;
        s.network.send(new GameMessage.StartCountdown(delayMs));
        scheduleStartGame(delayMs);
    }

    private void scheduleStartGame(long delayMs) {
        statusLabel.setText("Starting in " + (delayMs / 1000) + "s...");
        startButton.setDisable(true);
        PauseTransition pt = new PauseTransition(Duration.millis(delayMs));
        pt.setOnFinished(e -> startGameNow());
        pt.play();
    }

    private void startGameNow() {
        //TODO Change Scene and get Controller
        //TODO startGame
        try {
            String address = "/Views/Memory/GameScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            GameScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.passLanData();
            controller.startGame(
                    Session.current().config.player1Name(),
                    Session.current().config.player2Name()
            );

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playerJoin(String joinN) {
        timeline.stop();
        joinName = joinN;
        opponentNameLabel.setText(joinName);
        statusLabel.setText("Waiting for " + joinName + " to be ready!");
    }

    private void updatePeerStatus() {
        Session s = Session.current();
        if (s.localReady && s.peerReady) {
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("ready");
            statusLabel.getStyleClass().add("box");
            statusLabel.setText("All ready!");
        } else if (s.peerReady) {
            statusLabel.setText("Opponent is ready. Press Ready when you are.");
        } else if (s.localReady) {
            statusLabel.setText("Waiting for opponent to be ready...");
        } else {
            statusLabel.setText("Press Ready to start!");
        }
    }

    public void handData(String hostName, boolean isHost){



    }
}
