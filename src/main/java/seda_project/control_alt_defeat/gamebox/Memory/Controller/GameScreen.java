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
import seda_project.control_alt_defeat.gamebox.Memory.LocalGame;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Memory.engine.MCard;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Player;

import java.util.ArrayList;
import java.util.Collections;

public class GameScreen {
    ViewStack vS;
    LocalGame localGame;
    int matchSize;
    int deckSize;
    ArrayList<MCard> flippedCards = new ArrayList<>();
    boolean canClick = true;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
    ScaleTransition blink;

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

    public void passMemoryData(String player1, String player2, int tupleSize, int deckSize){
        sboardP1.setText(player1);
        sboardP2.setText(player2);

        this.matchSize = tupleSize;
        this.deckSize = deckSize;
        createBoard(tupleSize,deckSize);
    }

    private void createBoard(int tupleSize, int deckSize) {
        GridPane playingGrid  = new GridPane();

        playingGrid.setHgap(5);
        playingGrid.setVgap(5);
        playingGrid.setPadding(new Insets(5));

        int col = (int)Math.ceil(Math.sqrt(deckSize));
        int row = (int) Math.ceil((double) deckSize /col);

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
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int helper = 0;
                if ( i == row-1 && overhang!=0){
                    helper = (row-overhang)/2;
                }
                if (placed < deckSize) {
                    placed++;
                    int id = positions.get((col * i) + j);
                    MCard cell = new MCard(i, j+helper, id);
                    cell.setPrefSize(size, size);
                    cell.setMinSize(size, size);
                    cell.setMaxSize(size, size);

                    cell.setOnAction(mouseEvent -> {
                        if (canClick) {
                            flipmotion(cell);
                        }
                    });
                    playingGrid.add(cell, j+helper, i);
                }
            }
        }
        gamePane.getChildren().add(playingGrid);
        StackPane.setAlignment(playingGrid, Pos.CENTER);
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

    }

    public void turnCardsBack(){
        canClick = false;
        pause.setOnFinished(e -> {
            for (MCard c : flippedCards) {
                flipmotion(c);
            }
            flippedCards.clear();
            canClick = true;
        });

        pause.play();
    }

    private void flipCard(MCard card, int id) {
        flippedCards.add(card);
        localGame.flipCard(id);
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
    public void awardPoints(int active){
        if (active %2 == 0) {
            int current = Integer.parseInt(sboardScoreP2.getText());
            sboardScoreP2.setText((current+10)+"");
        }
        else {
            int current = Integer.parseInt(sboardScoreP1.getText());
            sboardScoreP1.setText((current+10)+"");
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
}
