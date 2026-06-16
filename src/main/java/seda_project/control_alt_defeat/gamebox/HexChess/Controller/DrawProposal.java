package seda_project.control_alt_defeat.gamebox.HexChess.Controller;


import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class DrawProposal {
    private GameScreen screen;
    private int proposing;
    @FXML
    private AnchorPane root;
    public void onAcceptAction() {
        screen.drawProposal();
        ((Stage) root.getScene().getWindow()).close();
    }

    public void onDeclineAction() {
        screen.declined(proposing);
        ((Stage) root.getScene().getWindow()).close();
    }

    public void sendGameScreen(GameScreen screen){
        this.screen = screen;

    }

    public void sendPlayer(int proposing) {
        this.proposing = proposing;
    }
}
