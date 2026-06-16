package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

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
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisAdvancedSettings;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;
import seda_project.control_alt_defeat.gamebox.network.Announcer;
import seda_project.control_alt_defeat.gamebox.network.Discovery;
import seda_project.control_alt_defeat.gamebox.Tetris.network.TetrisMessage;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.network.LanHost;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class WaitForOpponent extends Controller {

    private Timeline loadingDots;
    private Announcer announcer;

    @FXML
    private VBox header;

    @FXML
    private Label yourNameLabel, statusLabel, hostIpAddressLabel, opponentNameLabel;

    @FXML
    private Button startButton;

    @FXML
    public void onBackAction() {
        stopAnnouncer();
        if (loadingDots != null) loadingDots.stop();
        Session.clear(); 
        c.backScene(header, vS);
    }

    @FXML
    public void onStartGameAction() {
        NetworkLayer layer = Session.current().network;
        if (layer == null) return;

        Session s = Session.current();
        s.localReady = !s.localReady;
        layer.send(new TetrisMessage.Ready(s.localReady));

        startButton.setText(s.localReady ? "Not Ready" : "Ready");
        updatePeerStatus();
        maybeStartCountdown();
    }

    public void passHostData(String hostName, int hostLevel) {
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
        announcer = Discovery.announceTetris(hostName, Lan.DEFAULT_PORT, hostLevel);

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
        if (msg instanceof TetrisMessage.Hello h) {
            Session s = Session.current();
            s.peerName = h.playerName();
            s.peerLevel = h.playerLevel();
            boolean vertical = TetrisAdvancedSettings.getInstance().isVertical();
            s.lanVertical = vertical;
            layer.send(new TetrisMessage.LobbyInfo(s.myName,h.playerName(),s.myLevel,s.peerLevel,vertical));
            playerJoin(h.playerName());
        } else if (msg instanceof TetrisMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
            maybeStartCountdown();
        }
    }

    public void passJoinData(String playerName, String ipAddress) {
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
        layer.send(new TetrisMessage.Hello(playerName,Session.current().myLevel));
    }

    private void handleJoinMessage(Message msg) {
        if (msg instanceof TetrisMessage.LobbyInfo info) {
            Session.current().peerName = info.hostName();
            Session.current().lanVertical = info.vertical();
            opponentNameLabel.setText(info.hostName());
            statusLabel.setText("Press Ready to start!");
        } else if (msg instanceof TetrisMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
        } else if (msg instanceof TetrisMessage.StartCountdown sc) {
            scheduleStartGame(sc.delayMs());
        }
    }

    
    private void maybeStartCountdown() {
        Session s = Session.current();
        if (!s.isHost) return;
        if (!(s.localReady && s.peerReady)) return;

        long delayMs = 3000;
        s.network.send(new TetrisMessage.StartCountdown(delayMs));
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
        Session s = Session.current();
        // Player 1 = host , Player 2 = client
        String p1 = s.isHost ? s.myName  : s.peerName;
        String p2 = s.isHost ? s.peerName : s.myName;
        int p1L = s.isHost ? s.myLevel : s.peerLevel;
        int p2L = s.isHost ? s.peerLevel : s.myLevel;

        // Build the engine on the host only
        TetrisEngine engine = null;
        if (s.isHost) {
            engine = new TetrisEngine(p1, p2, p1L,p2L, BlockRegistry.getInstance(), TetrisAdvancedSettings.getInstance());
            s.tetrisEngine = engine;
        }

        String address = s.lanVertical
                ? "/Views/Tetris/GameScreen.fxml"
                : "/Views/Tetris/GameScreenHorizontal.fxml";

        GameScreen controller = (GameScreen) c.changeScene(address, header, vS);
        controller.create(p1, p2, p1L,p2L, true, engine);

        if (s.isHost) {
            controller.attachHostNetworkBridge(s.network);
        } else {
            controller.attachClientNetworkBridge(s.network);
        }
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
