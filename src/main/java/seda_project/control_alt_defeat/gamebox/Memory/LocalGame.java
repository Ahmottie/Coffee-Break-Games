package seda_project.control_alt_defeat.gamebox.Memory;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import seda_project.control_alt_defeat.gamebox.Memory.Controller.GameScreen;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Player;

import java.util.ArrayList;

public class LocalGame {
    private Configuration c = new Configuration();
    private Player player1;
    private Player player2;
    private GameScreen gameScreen;
    private int matchSize;
    private int activePlayer;
    private ArrayList<Integer> flippedCards;
    private int deckSize;
    private int possibleMatches;
    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

    public LocalGame(Player player1, Player player2, GameScreen gameScreen, int matchSize, int deckSize) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameScreen = gameScreen;
        this.activePlayer = c.ActivePlayer();
        this.matchSize = matchSize;
        this.flippedCards = new ArrayList<>();
        this.deckSize = deckSize;
        possibleMatches = deckSize/matchSize;

        gameScreen.setActivePlayerLabel(getActivePlayerName());
    }

    public void flipCard(int id) {

        flippedCards.add(id);

        if (flippedCards.size() == matchSize) {
            if (checkMatch()){
                possibleMatches--;
                gameScreen.awardPoints(activePlayer);

                gameScreen.setStatusLabel(true);
                gameScreen.removeMatch();
                flippedCards.clear();
            }
            else {
                gameScreen.turnCardsBack();

                gameScreen.setStatusLabel(false);
                activePlayer += 1;
                gameScreen.setActivePlayerLabel(getActivePlayerName());

                flippedCards.clear();
            }

        }
        if(possibleMatches == 0){
            pause.setOnFinished(event -> {
                gameScreen.gameEnd();
            });
            pause.play();
        }
    }

    private boolean checkMatch() {
        int first = flippedCards.getFirst();
        for (int flippedID : flippedCards){
            if  (flippedID != first) {
                System.out.println("Kein Match");
                return false;
            }
        }
        return true;
    }

    public String getActivePlayerName() {
        if (activePlayer % 2 == 0) {
            return player2.getName();
        } else {
            return player1.getName();
        }
    }


}
