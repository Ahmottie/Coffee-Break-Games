package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

public class ResultScreen {
    ViewStack vS;

    String player1Name,player2Name, pointsPlayer1, pointsPlayer2;
    int tupleSize,deckSize, winner;

    @FXML
    private VBox header;

    @FXML
    private Label matchSizeLabel, deckSizeLabel,looserLabel,winnerLabel, positionWinnerLabel, positionLooserLabel, winnerPointsLabel,looserPointsLabel;

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

    @FXML
    private void onPlayAgainAction(){
        //TODO get Player 1 Name
        //TODO get Player 2 Name
        //TODO get TupleSize
        //TODO get DeckSize

        try {
            String address = "/Views/Memory/GameScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            GameScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);
            controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
            controller.startGame(player1Name,player2Name);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void passMatchData(String player1Name, String player2Name, String pointsPlayer1, String pointsPlayer2, int tupleSize, int deckSize, int winner){
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.tupleSize = tupleSize;
        this.deckSize = deckSize;
        this.winner = winner;
        this.pointsPlayer1 = pointsPlayer1;
        this.pointsPlayer2 = pointsPlayer2;

        deckSizeLabel.setText(String.valueOf(deckSize));
        matchSizeLabel.setText(String.valueOf(tupleSize));

        switch (winner){
            case 0:
                positionLooserLabel.setText("Draw");
                positionWinnerLabel.setText("Draw");
            case 1:
                winnerLabel.setText(player1Name);
                winnerPointsLabel.setText(pointsPlayer1);
                looserLabel.setText(player2Name);
                looserPointsLabel.setText(pointsPlayer2);
                break;
            case 2:
                winnerLabel.setText(player2Name);
                looserPointsLabel.setText(pointsPlayer2);
                looserLabel.setText(player1Name);
                winnerPointsLabel.setText(pointsPlayer1);
                break;
        }

    }

    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }
}
