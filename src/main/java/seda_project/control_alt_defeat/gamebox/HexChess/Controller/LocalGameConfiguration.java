package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PlayerColor;
import seda_project.control_alt_defeat.gamebox.ui.ToggleSwitch;

import java.net.URL;
import java.util.ResourceBundle;

public class LocalGameConfiguration extends Controller implements Initializable {
    private String boardState;

    @FXML
    private TextField player1TF, player2TF;

    @FXML
    private Label statusLabel;

    @FXML
    private ToggleSwitch isP1Bot, isP2Bot;

    @FXML
    public void onBackAction() {
        sC.play("button");
        c.backScene(header, vS);
    }

    @FXML
    public void onStartAction() {
        sC.play("button");
        String player1Name = c.checkNameInput(player1TF.getText(),1);
        String player2Name = c.checkNameInput(player2TF.getText(),2);
        if (c.checkNameLength(player1Name,1, statusLabel) && c.checkNameLength(player2Name,2,statusLabel)) {
            sC.stopLooping();
            sC.playLooping("chess_background",.2);
            GameScreen controller = (GameScreen) c.changeScene("/Views/HexChess/GameScreen.fxml", header, vS);
            GameEngine gameEngine = new GameEngine();
            controller.setGameEngine(gameEngine);

            if (player2Name.equalsIgnoreCase("Bot")) {
                controller.setBotMode(true, PlayerColor.BLACK);
            } else {
                controller.setBotMode(false, null);
            }
            if (c.checkFlip(player1Name,player2Name)){
                controller.flip();
            }
            if (c.checkRainbow(player1Name,player2Name)){
                controller.rainbow();
            }
            if (player1Name.equals("Duck")) {
                controller.p1Duck();
            }
            if (player2Name.equals("Duck")){
                controller.p2Duck();
            }
            if (boardState == null) {
                controller.init(header.getScene());
            }
            else{
                controller.init(boardState, header.getScene());
            }
            controller.setNames(player1Name, player2Name);
            controller.setPoints(0, 0);
        }
        else{
            statusLabel.setVisible(true);
            sC.play("error");
        }
    }

    @FXML
    public void onCustomBoardAction() {
        BoardDesigner controller = (BoardDesigner) c.changeScene("/Views/HexChess/BoardDesigner.fxml",header,vS);
        controller.handNames(player1TF.getText(),player2TF.getText());
    }



    public void boardSelection(String notation, String p1Name, String p2Name) {
        boardState = notation;
        if (p1Name != null){
            player1TF.setText(p1Name);
        }
        if (p2Name != null){
            player2TF.setText(p2Name);
        }
    }

    private void changeBot(boolean newValue, int player){
        if (player == 1 && newValue){
            if (isP2Bot.switchOnProperty().getValue()){
                player2TF.setText("");
                isP2Bot.setSwitchedOn(false);
            }
            player1TF.setText("Bot");
        }
        if (player == 2 && newValue){
            if (isP1Bot.switchOnProperty().getValue()){
                player1TF.setText("");
                isP1Bot.setSwitchedOn(false);
            }
            player2TF.setText("Bot");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        isP1Bot.switchOnProperty().addListener((_,_,newValue) ->{
            changeBot(newValue,1);
        });
        isP2Bot.switchOnProperty().addListener((_,_,newValue) ->{
            changeBot(newValue,2);
        });
    }
}
