package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

public class DrawProposal extends Controller {
    private GameScreen screen;
    private int proposing;

    public void onAcceptAction() {
        sC.play("button");
        screen.drawProposal();
        ((Stage) root.getScene().getWindow()).close();
    }

    public void onDeclineAction() {
        sC.play("button");
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
