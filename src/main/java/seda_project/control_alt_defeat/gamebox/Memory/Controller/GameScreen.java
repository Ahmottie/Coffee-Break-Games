package seda_project.control_alt_defeat.gamebox.Memory.Controller;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Card;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngineImpl;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEventListener;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSnapshot;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.MCard;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen extends Controller {
    GameEngine engine;
    int matchSize;
    int deckSize;
    ArrayList<MCard> flippedCards = new ArrayList<>();
    boolean canClick = true;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
    private final java.util.Set<Integer> remoteFlipIds = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private boolean disconnected = false;
    private String myName;
    private final Map<Integer, MCard> cardsById = new HashMap<>();
    private final Map<MCard, Integer> cardIdOf = new HashMap<>();
    private GameConfig config;
    private GameSetup setup;

    @FXML
    private Label sboardP1, sboardP2, sboardScoreP1, sboardScoreP2, activePlayerLabel;

    @FXML
    private StackPane gamePane;

    @FXML
    private void onExitGameAction() {
        sC.play("button");
        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    public void passMemoryData(String player1, String player2, int tupleSize, int deckSize) {
        sboardP1.setText(player1);
        sboardP2.setText(player2);

        this.matchSize = tupleSize;
        this.deckSize = deckSize;
        this.myName = player1;

        // Build a fresh GameConfig + GameSetup for a single-machine game.
        this.config = new GameConfig(tupleSize, deckSize, player1, player2);
        this.setup  = Decks.prepare(config);

        createBoard();
    }

    public void passLanData() {
        Session s = Session.current();
        sboardP1.setText(s.config.player1Name());
        sboardP2.setText(s.config.player2Name());

        this.matchSize = s.config.k();
        this.deckSize  = s.config.deckSize();
        this.myName    = s.myName;
        this.config    = s.config;
        this.setup     = s.setup;

        createBoard();
    }

    private void createBoard() {
        GridPane playingGrid = new GridPane();
        playingGrid.setHgap(5);
        playingGrid.setVgap(5);
        playingGrid.setPadding(new Insets(5));

        List<Card> deck = setup.initialDeck();
        int n = deck.size();

        int row = 5;
        int col = (int) Math.ceil((double) n / row);

        if (row > col){
            int tmp = col;
            col = row;
            row = tmp;
        }

        int height = 340;

        double size = (double) height /row;

        playingGrid.setPrefSize(size*col, size*row);
        playingGrid.setMaxSize(size*col, size*row);
        playingGrid.setMinSize(size*col, size*row);

        for (int i = 0; i < col; i++) {
            playingGrid.getColumnConstraints().add(new ColumnConstraints(size));
        }
        for (int i = 0; i < row; i++) {
            playingGrid.getRowConstraints().add(new RowConstraints(size));
        }

        int placed = 0;
        int overhang = n % row;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int helper = 0;
                if (i == row - 1 && overhang != 0) {
                    helper = (row - overhang) / 2;
                }
                if (placed < n) {
                    Card c = deck.get(placed);
                    int cardId = c.id();
                    int symbolId = c.symbolId();
                    MCard cell = new MCard(i, j + helper, symbolId);
                    cell.setHeightProporties(size,size);
                    cell.setPrefSize(size, size);
                    cell.setMinSize(size, size);
                    cell.setMaxSize(size, size);

                    // Track the MCard in both directions so listeners can find it.
                    cardsById.put(cardId, cell);
                    cardIdOf.put(cell, cardId);

                    cell.setOnAction(_ -> {
                        if (!canClick) return;
                        // In LAN mode, only the active player can flip.
                        if (Session.current().network != null
                                && !engine.getActivePlayer().equals(myName)) {
                            return;
                        }
                        flipmotion(cell, cardId);
                    });
                    playingGrid.add(cell, j + helper, i);
                    placed++;
                }
            }
        }

        gamePane.getChildren().add(playingGrid);
    }

    public void setStatusLabel(boolean match) {
        if (match){
            sC.play("memory_match");
            Toast.makeText(gamePane,"Match");
        }
        else {
            sC.play("memory_missmatch");
            Toast.makeText(gamePane,"Missmatch");
        }
    }

    public void setActivePlayerLabel(String name) {
        activePlayerLabel.setText(name);
    }

    public void startGame() {
        // Build the engine and start it with our prepared setup.
        engine = new GameEngineImpl();
        engine.start(config, setup);

        // Drive the UI from engine events.
        engine.addListener(new GameEventListener() {
            @Override
            public void onMatch(List<Integer> matchedIds, String scoringPlayer,
                                int scoreAwarded, GameSnapshot snapshot) {
                Platform.runLater(() -> {
                    int activeIdx = scoringPlayer.equals(config.player1Name()) ? 1 : 0;
                    awardPoints(activeIdx);
                    setStatusLabel(true);
                    removeMatch();
                    if (snapshot.gameOver()) gameEnd();
                });
            }

            @Override
            public void onMismatch(List<Integer> flippedIds, GameSnapshot snapshot) {
                canClick = false;
                Platform.runLater(() -> {
                    turnCardsBack();
                    setStatusLabel(false);
                });
            }

            @Override
            public void onTurnChanged(String newActivePlayer, GameSnapshot snapshot) {
                Platform.runLater(() -> setActivePlayerLabel(newActivePlayer));
            }
        });

        NetworkLayer net = Session.current().network;
        if (net != null) {
            net.addListener(new NetworkListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof GameMessage.Flip f) {
                        Platform.runLater(() -> applyRemoteFlip(f.cardId()));
                    }
                }
                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> handleDisconnect(reason));
                }
            });
        }

        setActivePlayerLabel(engine.getActivePlayer());
    }

    public void turnCardsBack() {
        pause.setOnFinished(_ -> {
            for (MCard c : flippedCards) {
                flipmotion(c, cardIdOf.get(c));
            }
            flippedCards.clear();
            canClick = true;
        });
        pause.play();
    }

    public void removeMatch() {
        canClick = false;
        pause.setOnFinished(_ -> {
            for (MCard c : flippedCards) {
                c.setVisible(false);
            }
            flippedCards.clear();
            canClick = true;
        });
        pause.play();
    }

    public void awardPoints(int active) {
        if (active % 2 == 0) {
            int current = Integer.parseInt(sboardScoreP2.getText());
            sboardScoreP2.setText((current + 10) + "");
        } else {
            int current = Integer.parseInt(sboardScoreP1.getText());
            sboardScoreP1.setText((current + 10) + "");
        }
    }

    public void gameEnd() {
        int points1 = Integer.parseInt(sboardScoreP1.getText());
        int points2 = Integer.parseInt(sboardScoreP2.getText());
        int winner;
        if (points1 == points2) {
            winner = 0;
        } else if (points1 > points2) {
            winner = 1;
        } else {
            winner = 2;
        }
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/Memory/ResultScreen.fxml",header,vS);
        controller.passMatchData(sboardP1.getText(), sboardP2.getText(),
                    sboardScoreP1.getText(), sboardScoreP2.getText(),
                    matchSize, deckSize, winner);
        if(flipped){
            controller.flip();
        }
        if(rainbowed){
            controller.rainbow();
        }
    }

    private void flipmotion(MCard card, int cardId) {
        ScaleTransition firstHalf = new ScaleTransition(Duration.millis(300), card);
        firstHalf.setFromX(1);
        firstHalf.setToX(0);

        ScaleTransition secondHalf = new ScaleTransition(Duration.millis(300), card);
        secondHalf.setFromX(0);
        secondHalf.setToX(1);

        if (!card.getFaceUp()) {
            flippedCards.add(card);
            if (flippedCards.size() >= matchSize) {
                canClick = false;
            }
            firstHalf.setOnFinished(_ -> {
                card.setFaceUp(true);
                boolean wasRemote = remoteFlipIds.remove(cardId);
                engine.flip(cardId);
                NetworkLayer net = Session.current().network;
                if (net != null && !wasRemote) {
                    net.send(new GameMessage.Flip(cardId));
                }
            });
        } else {
            firstHalf.setOnFinished(_ -> card.faceDown());
        }

        SequentialTransition flip = new SequentialTransition(firstHalf, secondHalf);
        flip.play();
    }

    private void applyRemoteFlip(int cardId) {
        MCard card = cardsById.get(cardId);
        if (card == null || card.getFaceUp() || !canClick) return;
        remoteFlipIds.add(cardId);
        flipmotion(card, cardId);
    }

    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;

        canClick = false;
        pause.stop();

        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason + "\n\nReturning to the main menu.",
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();

        Session.clear();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }
}
