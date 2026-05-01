package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Memory.engine.MCard;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Player;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Card;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEngineImpl;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameEventListener;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameOutcome;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSnapshot;
import seda_project.control_alt_defeat.gamebox.network.Session;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collections;
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

    @FXML
    private VBox header;

    @FXML
    private Label sboardP1,sboardP2,sboardScoreP1,sboardScoreP2,activePlayerLabel,turnStatusLabel;

    @FXML
    private StackPane gamePane;

    @FXML
    private Text notificationText;


    @FXML
    private void onExitGameAction(){
        try{
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
        }
        catch(Exception e){
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
<<<<<<< HEAD
        this.myName = player1;   // local mode — both players share the screen, name doesn't gate clicks

        // Build a fresh GameConfig + GameSetup for a single-machine game.
        this.config = new GameConfig(tupleSize, deckSize, player1, player2);
        this.setup  = Decks.prepare(config);

        createBoard();
    }

    private void createBoard() {
        GridPane playingGrid = new GridPane();
        playingGrid.setHgap(10);
        playingGrid.setVgap(10);
        playingGrid.setPadding(new Insets(10));
=======
        createBoard(tupleSize,deckSize);
    }

    private void createBoard(int tupleSize, int deckSize) {
        GridPane playingGrid  = new GridPane();

        playingGrid.setHgap(5);
        playingGrid.setVgap(5);
        playingGrid.setPadding(new Insets(5));
>>>>>>> origin/main

        List<Card> deck = setup.initialDeck();
        int n = deck.size();

        int col = (int) Math.ceil(Math.sqrt(n));
        int row = (int) Math.ceil((double) n / col);

<<<<<<< HEAD
        int placed = 0;
        int overhang = n % row;
=======
        int gridSize = 340;

        playingGrid.setPrefSize(gridSize,gridSize);
        playingGrid.setMaxSize(gridSize,gridSize);
        playingGrid.setMinSize(gridSize,gridSize);

        double size = gridSize/row;


        for (int i = 0; i < col; i++) {
            ColumnConstraints cc = new ColumnConstraints(size);
            playingGrid.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < row; i++) {
            RowConstraints rc = new RowConstraints(size);
            playingGrid.getRowConstraints().add(rc);
        }


        ArrayList<Integer> positions  = new ArrayList<>();
        int repeats = deckSize/tupleSize;
        for (int i = 0; i < repeats; i++) {
            for (int j = 0; j < tupleSize; j++){
                positions.add(i);
            }
        }

        Collections.shuffle(positions);

        int placed = 0;
        int overhang = deckSize%row;
>>>>>>> origin/main
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int helper = 0;
                if (i == row - 1 && overhang != 0) {
                    helper = (row - overhang) / 2;
                }
<<<<<<< HEAD
                if (placed < n) {
                    Card c = deck.get(placed);
                    int cardId = c.id();
                    int symbolId = c.symbolId();
                    MCard cell = new MCard(i, j + helper, symbolId);
=======
                if (placed < deckSize) {
                    placed++;
                    int id = positions.get((col * i) + j);
                    MCard cell = new MCard(i, j+helper, id);
                    cell.setPrefSize(size, size);
                    cell.setMinSize(size, size);
                    cell.setMaxSize(size, size);
>>>>>>> origin/main

                    // Track this MCard by the engine's Card.id so listeners can find it
                    cardsById.put(cardId, cell);
                    cardIdOf.put(cell, cardId);

                    cell.setOnAction(e -> {
                        if (canClick) {
                            flipmotion(cell, cardId);
                        }
                    });
<<<<<<< HEAD
                    cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    playingGrid.add(cell, j + helper, i);

                    GridPane.setHgrow(cell, Priority.ALWAYS);
                    GridPane.setVgrow(cell, Priority.ALWAYS);
                    placed++;
=======
                    playingGrid.add(cell, j+helper, i);
>>>>>>> origin/main
                }
            }
        }

        gamePane.getChildren().add(playingGrid);
<<<<<<< HEAD
        AnchorPane.setBottomAnchor(playingGrid, 20.0);
        AnchorPane.setTopAnchor(playingGrid, 20.0);
        AnchorPane.setLeftAnchor(playingGrid, 20.0);
        AnchorPane.setRightAnchor(playingGrid, 20.0);

=======
        StackPane.setAlignment(playingGrid, Pos.CENTER);
>>>>>>> origin/main
    }

    public void setStatusLabel(boolean match){
        notificationText.setVisible(true);
        notificationText.setViewOrder(-1.0);

        notificationText.setText(match ? "Match" : "Mismatch");
        blink.play();
        blink.setOnFinished( e-> {
            notificationText.setVisible(false);
            notificationText.setViewOrder(5.0);
        });
    }

    public void setActivePlayerLabel(String name){
        activePlayerLabel.setText(name);
    }

    public void startGame(String player1Name, String player2Name) {
<<<<<<< HEAD
        blink = new Timeline(
                new KeyFrame(Duration.seconds(.25), e -> {
                    notificationText.setStyle("-fx-font-size: 75px;");
                }),
                new KeyFrame(Duration.seconds(.5), e -> {
                    notificationText.setStyle("-fx-font-size: 100px;");
                })
        );
        blink.setCycleCount(3);

        // Build the engine and start it with our prepared setup
        engine = new GameEngineImpl();
        engine.start(config, setup);

        // Drive the UI from engine events
        engine.addListener(new GameEventListener() {
            @Override
            public void onMatch(List<Integer> matchedIds, String scoringPlayer,
                                int scoreAwarded, GameSnapshot snapshot) {
                Platform.runLater(() -> {
                    int activeIdx = scoringPlayer.equals(config.player1Name()) ? 1 : 2;
                    awardPoints(activeIdx == 1 ? 1 : 0);   // mirror old "even = P2, odd = P1" mapping
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

        // Initial active player label
        setActivePlayerLabel(engine.getActivePlayer());
=======
        Player player1 = new Player(player1Name);
        Player player2 = new Player(player2Name);

        blink = new ScaleTransition(Duration.seconds(0.5), notificationText);
        blink.setFromX(1.0);
        blink.setToX(0.75);
        blink.setFromY(1.0);
        blink.setToY(0.75);
        blink.setAutoReverse(true);
        blink.setCycleCount(4);

        localGame = new LocalGame(player1,player2,this,matchSize, deckSize);

>>>>>>> origin/main
    }


    public void turnCardsBack(){
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

    private void flipmotion(MCard card, int cardId) {
        ScaleTransition firstHalf = new ScaleTransition(Duration.millis(300), card);
        firstHalf.setFromX(1);
        firstHalf.setToX(0);

        ScaleTransition secondHalf = new ScaleTransition(Duration.millis(300), card);
        secondHalf.setFromX(0);
        secondHalf.setToX(1);

        if (!card.getFaceUp()) {
            firstHalf.setOnFinished(e -> {
                card.setText(Integer.toString(card.getid()));
                card.setDisable(true);
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

    public void removeMatch() {
        canClick = false;
        pause.setOnFinished(e -> {
            for (MCard c : flippedCards){
                c.setVisible(false);
            }
            flippedCards.clear();
            canClick = true;
        });

        pause.play();
    }

    public void awardPoints(int active) {
        if (active == 1) {
            int current = Integer.parseInt(sboardScoreP1.getText());
            sboardScoreP1.setText((current + 10) + "");
        } else {
            int current = Integer.parseInt(sboardScoreP2.getText());
            sboardScoreP2.setText((current + 10) + "");
        }
    }

    public void gameEnd() {
        int points1 = Integer.parseInt(sboardScoreP1.getText());
        int points2 = Integer.parseInt(sboardScoreP2.getText());
        int winner;
        if (points1 == points2){
            winner = 0;
        }
        else if (points1 > points2){
            winner = 1;
        }
        else{
            winner = 2;
        }
        try {
            String address = "/Views/Memory/ResultScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            ResultScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);
            controller.passMatchData(sboardP1.getText(), sboardP2.getText(), sboardScoreP1.getText(),sboardScoreP2.getText(),matchSize, deckSize,winner);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
=======
    private void flipmotion(MCard card){
        ScaleTransition firstHalf = new ScaleTransition(Duration.millis(300), card);
        firstHalf.setFromX(1);
        firstHalf.setToX(0);

        ScaleTransition secondHalf = new ScaleTransition(Duration.millis(300), card);
        secondHalf.setFromX(0);
        secondHalf.setToX(1);

        if (!card.getFaceUp()) {
            firstHalf.setOnFinished(e -> {
                card.setFaceUp(true);
                flipCard(card, card.getid());
            });
        }
        else{
            System.out.println("card.getFaceUp()");
            firstHalf.setOnFinished(e -> {
                card.faceDown();
            });
        }

        SequentialTransition flip = new SequentialTransition(firstHalf, secondHalf);
        flip.play();
    }
>>>>>>> origin/main
}
