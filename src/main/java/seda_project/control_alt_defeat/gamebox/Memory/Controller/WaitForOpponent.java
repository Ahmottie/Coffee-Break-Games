package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.Lan;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.LanHost;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class WaitForOpponent extends Controller {
    private boolean host;
    private boolean ready;
    private String hostName;
    private String joinName;
    private int tupleSize;
    private int deckSize;
    private Timeline timeline;
    private String ipAddress;

    @FXML private VBox header;
    @FXML private Button startGameButton;
    @FXML private Label yourNameLabel, opponentNameLabel, deckSizeLabel, matchSizeLabel, statusLabel, hostIpAddressLabel;

    @FXML
    protected void onBackAction() {
        Session.clear();
        Object controller = c.backScene(header,vS);
        if (controller instanceof HostLan c) {
            c.backTransfer(hostName, tupleSize, deckSize);
        }
        if (controller instanceof JoinLan c) {
            c.backTransfer(joinName, ipAddress);
        }
    }

    @FXML
    protected void onStartGameAction() {
        NetworkLayer layer = Session.current().network;
        if (layer == null) return;

        Session.current().localReady = !Session.current().localReady;
        boolean myReady = Session.current().localReady;

        layer.send(new GameMessage.Ready(myReady));
        startGameButton.setText(myReady ? "Not Ready" : "Ready");

        updateLocalStatus(myReady);
        maybeStartCountdown();
    }

    private void updateLocalStatus(boolean myReady) {
        if (myReady && Session.current().peerReady) {
            statusLabel.getStyleClass().clear();
            statusLabel.getStyleClass().add("ready");
            statusLabel.getStyleClass().add("box");
            statusLabel.setText("All ready!");
        } else if (myReady) {
            statusLabel.setText("Waiting for opponent to be ready...");
        } else {
            statusLabel.setText("You are not ready.");
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
        startGameButton.setDisable(true);
        PauseTransition pt = new PauseTransition(Duration.millis(delayMs));
        pt.setOnFinished(e -> startGameNow());
        pt.play();
    }

    private void startGameNow() {
        GameScreen controller = (GameScreen) c.changeScene("/Views/Memory/GameScreen.fxml",header,vS);
        controller.passLanData();
        controller.startGame(
                Session.current().config.player1Name(),
                Session.current().config.player2Name()
        );
    }

    public void passHostData(boolean host, String hostName, int tupleSize, int deckSize) {
        this.host = host;
        this.hostName = hostName;
        this.tupleSize = tupleSize;
        this.deckSize = deckSize;

        yourNameLabel.setText(hostName);
        deckSizeLabel.setText(Integer.toString(deckSize));
        matchSizeLabel.setText(Integer.toString(tupleSize));
        startGameButton.setText("Ready");

        // Loading-dots animation while waiting for someone to join
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),   e -> opponentNameLabel.setText("")),
                new KeyFrame(Duration.seconds(0.5), e -> opponentNameLabel.setText("o")),
                new KeyFrame(Duration.seconds(1),   e -> opponentNameLabel.setText("oo")),
                new KeyFrame(Duration.seconds(1.5), e -> opponentNameLabel.setText("ooo")),
                new KeyFrame(Duration.seconds(2),   e -> opponentNameLabel.setText("ooo"))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        statusLabel.setText("Waiting for an opponent to Join!");

        // Open the server socket. When the client connects, hop back to JavaFX
        // and attach the network listener so we can react to messages.
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

    public void passJoinData(boolean host, String playerName, String ipAddress) {
        this.host = host;
        this.ready = false;
        this.joinName = playerName;
        this.ipAddress = ipAddress;

        yourNameLabel.setText(playerName);
        startGameButton.setText("Ready");
        statusLabel.setText("Waiting for game info from host...");

        NetworkLayer layer = Session.current().network;
        if (layer == null) {
            statusLabel.setText("No connection to host.");
            return;
        }

        // Listen for the host to send us the lobby config
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
        layer.send(new GameMessage.Hello(playerName));
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

    private void handleJoinMessage(Message msg) {
        if (msg instanceof GameMessage.LobbyConfig lc) {
            Session.current().config = lc.config();
            Session.current().setup  = lc.setup();

            opponentNameLabel.setText(lc.config().player1Name());
            deckSizeLabel.setText(Integer.toString(lc.config().deckSize()));
            matchSizeLabel.setText(Integer.toString(lc.config().k()));
            statusLabel.setText("Press Ready to start!");
        } else if (msg instanceof GameMessage.Ready r) {
            Session.current().peerReady = r.ready();
            updatePeerStatus();
        } else if (msg instanceof GameMessage.StartCountdown sc) {
            scheduleStartGame(sc.delayMs());
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
}