package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessMessage;
import seda_project.control_alt_defeat.gamebox.network.*;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class WaitForOpponent extends Controller {
    private String myname;
    private Timeline loadingDots;
    private Announcer announcer;
    private String boardState;

    @FXML
    private Label yourNameLabel, statusLabel, hostIpAddressLabel, opponentNameLabel;

    @FXML
    private Button startButton;
    @FXML
    protected void onBackAction() {
        sC.play("button");
        Object controller = c.backScene(header,vS);
        if (loadingDots != null) loadingDots.stop();
        Session.clear();
        if (controller instanceof HostLanConfiguration host) {
            host.handData(myname);
        }
        if (controller instanceof JoinLan join){
            join.handData(myname);
        }
    }

    @FXML
    protected void onStartGameAction() {
        sC.play("button");
        NetworkLayer layer = Session.current().network;
        if (layer == null) return;

        Session s = Session.current();
        s.localReady = !s.localReady;
        layer.send(new ChessMessage.Ready(s.localReady));

        startButton.setText(s.localReady ? "Not Ready" : "Ready");
        updatePeerStatus();
        maybeStartCountdown();
    }

    public void passJoinData(String playerName, String ipAddress) {
        myname = playerName;
        Session s = Session.current();
        s.localReady = false;
        s.peerReady  = false;

        yourNameLabel.setText(playerName);
        hostIpAddressLabel.setText(ipAddress);
        startButton.setText("Ready");
        statusLabel.setText("Waiting for game info from host...");

        NetworkLayer layer = s.network;
        if (layer == null) {
            statusLabel.setText("No connection to host.");
            return;
        }

        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                Platform.runLater(() -> handleJoinMessage(msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> statusLabel.setText("Disconnected: " + reason));
            }
        });

        // Greet the host
        layer.send(new ChessMessage.Hello(playerName));
    }

    public void passHostData(String hostName, String boardState){
        myname = hostName;
        this.boardState = boardState;
        yourNameLabel.setText(hostName);
        hostIpAddressLabel.setText(Lan.localIp());
        startButton.setText("Ready");
        statusLabel.setText("Waiting for an opponent to join!");

        loadingDots = new Timeline(
                new KeyFrame(Duration.seconds(0),   e -> opponentNameLabel.setText("")),
                new KeyFrame(Duration.seconds(0.5), e -> opponentNameLabel.setText("o")),
                new KeyFrame(Duration.seconds(1),   e -> opponentNameLabel.setText("oo")),
                new KeyFrame(Duration.seconds(1.5), e -> opponentNameLabel.setText("ooo")),
                new KeyFrame(Duration.seconds(2),   e -> opponentNameLabel.setText("ooo"))
        );
        loadingDots.setCycleCount(Animation.INDEFINITE);
        loadingDots.play();

        // Start UDP broadcast so joiners can discover us
        announcer = Discovery.announceChess(hostName, Lan.DEFAULT_PORT, boardState);

        // Open the server socket on the background
        LanHost.hostAsync(Lan.DEFAULT_PORT,
                layer -> Platform.runLater(() -> {
                    stopAnnouncer();
                    Session.current().network = layer;
                    attachHostListener(layer);
                }),
                err -> Platform.runLater(() -> {
                    statusLabel.setText("Hosting failed: " + err.getMessage());
                    err.printStackTrace();
                })
        );
    }
    private void attachHostListener(NetworkLayer layer) {
        layer.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                Platform.runLater(() -> handleHostMessage(layer, msg));
            }
            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> statusLabel.setText("Opponent disconnected: " + reason));
            }
        });
    }

    private void handleHostMessage(NetworkLayer layer, Message msg) {
        if (msg instanceof ChessMessage.Hello h) {
            Session s = Session.current();
            s.peerName = h.playerName();
            layer.send(new ChessMessage.LobbyInfo(s.myName,h.playerName()));
            playerJoin(h.playerName());
        } else if (msg instanceof ChessMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
            maybeStartCountdown();
        }
    }
    private void handleJoinMessage(Message msg) {
        if (msg instanceof ChessMessage.LobbyInfo info) {
            Session.current().peerName = info.hostName();
            opponentNameLabel.setText(info.hostName());
            statusLabel.setText("Press Ready to start!");
        } else if (msg instanceof ChessMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
        } else if (msg instanceof ChessMessage.StartCountdown sc) {
            scheduleStartGame(sc.delayMs());
        }
    }


    private void maybeStartCountdown() {
        Session s = Session.current();
        if (!s.isHost) return;
        if (!(s.localReady && s.peerReady)) return;

        long delayMs = 3000;
        s.network.send(new ChessMessage.StartCountdown(delayMs));
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
        sC.stopLooping();
        sC.playLooping("chess_background",.1);
        Session s = Session.current();

        if (s.network != null) {
            s.network.clearListeners();
        }

        // Player 1 = host , Player 2 = client
        String p1 = s.isHost ? s.myName  : s.peerName;
        String p2 = s.isHost ? s.peerName : s.myName;
        int p1L = s.isHost ? s.myLevel : s.peerLevel;
        int p2L = s.isHost ? s.peerLevel : s.myLevel;

        GameEngine engine = new GameEngine();
        s.chessEngine = engine;

        String address = "/Views/HexChess/GameScreen.fxml";
        GameScreen controller = (GameScreen) c.changeScene(address, header, vS);
        controller.setGameEngine(engine);
        if (c.checkFlip(p1,p2)){
            controller.flip();
        }
        if (c.checkRainbow(p1,p2)){
            controller.rainbow();
        }
        if (p1.equals("Duck")) {
            controller.p1Duck();
        }
        if (p2.equals("Duck")){
            controller.p2Duck();
        }
        if (s.isHost) {
            if (boardState == null) {
                controller.init(header.getScene());
            }
            else{
                controller.init(boardState, header.getScene());
            }
            controller.attachHostBridge(s.network, engine);
        } else {
            if(s.boardState == null) {
                controller.init(header.getScene());
            }
            else {
                controller.init(s.boardState, header.getScene());
            }
            controller.attachClientBridge(s.network, engine);
        }
        controller.setNames(p1,p2);
        controller.setPoints(0.0, 0.0);
    }


    private void playerJoin(String joinN) {
        if (loadingDots != null) loadingDots.stop();
        opponentNameLabel.setText(joinN);
        statusLabel.setText("Waiting for " + joinN + " to be ready!");
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

    private void stopAnnouncer() {
        if (announcer != null) {
            try { announcer.close(); } catch (Exception ignored) {}
            announcer = null;
        }
    }
}
