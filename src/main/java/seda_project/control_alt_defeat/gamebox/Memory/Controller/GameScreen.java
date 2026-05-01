package seda_project.control_alt_defeat.gamebox.Memory.Controller;
import seda_project.control_alt_defeat.gamebox.network.Session;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Card;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngineImpl;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEventListener;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSnapshot;
import seda_project.control_alt_defeat.gamebox.Memory.engine.MCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen {
    ViewStack vS;
    GameEngine engine;
    int matchSize;
    int deckSize;
    ArrayList<MCard> flippedCards = new ArrayList<>();
    boolean canClick = true;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
    ScaleTransition blink;

    private String myName;
    private final Map<Integer, MCard> cardsById = new HashMap<>();
    private final Map<MCard, Integer> cardIdOf = new HashMap<>();
    private GameConfig config;
    private GameSetup setup;

    @FXML private VBox header;
    @FXML private Label sboardP1, sboardP2, sboardScoreP1, sboardScoreP2, activePlayerLabel, turnStatusLabel;
    @FXML private StackPane gamePane;
    @FXML private Text notificationText;

    @FXML
    private void onExitGameAction() {
        try {
            vS.emtyStack();
            String address = "/Views/Memory/MemoryMenu.fxml";

            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            MemoryMenu controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handViewStack(ViewStack vS) {
        this.vS = vS;
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
        int col = (int) Math.ceil(Math.sqrt(n));
        int row = (int) Math.ceil((double) n / col);

        int gridSize = 340;
        playingGrid.setPrefSize(gridSize, gridSize);
        playingGrid.setMaxSize(gridSize, gridSize);
        playingGrid.setMinSize(gridSize, gridSize);

        double size = (double) gridSize / row;

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
                    cell.setPrefSize(size, size);
                    cell.setMinSize(size, size);
                    cell.setMaxSize(size, size);

                    // Track the MCard in both directions so listeners can find it.
                    cardsById.put(cardId, cell);
                    cardIdOf.put(cell, cardId);

                    cell.setOnAction(e -> {
                        if (canClick) flipmotion(cell, cardId);
                    });
                    playingGrid.add(cell, j + helper, i);
                    placed++;
                }
            }
        }

        gamePane.getChildren().add(playingGrid);
        StackPane.setAlignment(playingGrid, Pos.CENTER);
    }

    public void setStatusLabel(boolean match) {
        notificationText.setVisible(true);
        notificationText.setViewOrder(-1.0);
        notificationText.setText(match ? "Match" : "Mismatch");
        blink.play();
        blink.setOnFinished(e -> {
            notificationText.setVisible(false);
            notificationText.setViewOrder(5.0);
        });
    }

    public void setActivePlayerLabel(String name) {
        activePlayerLabel.setText(name);
    }

    public void startGame(String player1Name, String player2Name) {
        blink = new ScaleTransition(Duration.seconds(0.5), notificationText);
        blink.setFromX(1.0);
        blink.setToX(0.75);
        blink.setFromY(1.0);
        blink.setToY(0.75);
        blink.setAutoReverse(true);
        blink.setCycleCount(4);

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

        setActivePlayerLabel(engine.getActivePlayer());
    }

    public void turnCardsBack() {
        canClick = false;
        pause.setOnFinished(e -> {
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
        pause.setOnFinished(e -> {
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
        try {
            String address = "/Views/Memory/ResultScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            ResultScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);
            controller.passMatchData(sboardP1.getText(), sboardP2.getText(),
                    sboardScoreP1.getText(), sboardScoreP2.getText(),
                    matchSize, deckSize, winner);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
            firstHalf.setOnFinished(e -> {
                card.setFaceUp(true);
                flippedCards.add(card);
                engine.flip(cardId);
            });
        } else {
            firstHalf.setOnFinished(e -> {
                card.faceDown();
            });
        }

        SequentialTransition flip = new SequentialTransition(firstHalf, secondHalf);
        flip.play();
    }
}
