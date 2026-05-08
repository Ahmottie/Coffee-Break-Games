package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;
import seda_project.control_alt_defeat.gamebox.Memory.engine.Decks;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameConfig;
import seda_project.control_alt_defeat.gamebox.Memory.engine.GameSetup;
import seda_project.control_alt_defeat.gamebox.network.GameMessage;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;

public class ResultScreen {
    ViewStack vS;
    Configuration c = new Configuration();

    String player1Name,player2Name, pointsPlayer1, pointsPlayer2;
    int tupleSize,deckSize, winner;

    @FXML
    private VBox header;

    @FXML
    private Label matchSizeLabel, deckSizeLabel,looserLabel,winnerLabel, positionWinnerLabel, positionLooserLabel, winnerPointsLabel,looserPointsLabel;

    @FXML private Button playAgainButton;

    private boolean disconnected = false;

    @FXML
    private void onExitGameAction(){
        try{
            Session.clear();
            vS.emtyStack();
            String address = "/Views/Memory/MemoryMenu.fxml";

            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            MemoryMenu controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS,c);

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
    private void onPlayAgainAction() {
        NetworkLayer net = Session.current().network;
        if (net == null) {
            // Local play: same as before — fresh game with same names + K + deck.
            startNewLocalGame();
            return;
        }

        // LAN: only the host can start a new game.
        if (!Session.current().isHost) return;     // safeguard; button is also disabled

        GameConfig cfg = new GameConfig(tupleSize, deckSize, player1Name, player2Name);
        GameSetup setup = Decks.prepare(cfg);

        Session.current().config = cfg;
        Session.current().setup  = setup;

        net.send(new GameMessage.NewGame(cfg, setup));
        startNewGameWithSetup(cfg, setup);
    }

    private void startNewLocalGame() {
        try {
            String address = "/Views/Memory/GameScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            GameScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);
            controller.passMemoryData(player1Name, player2Name, tupleSize, deckSize);
            controller.startGame(player1Name, player2Name);

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startNewGameWithSetup(GameConfig cfg, GameSetup setup) {
        Session.current().config = cfg;
        Session.current().setup  = setup;
        try {
            String address = "/Views/Memory/GameScreen.fxml";
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(address));
            Parent root = loader.load();
            GameScreen controller = loader.getController();

            vS.addFxmlLoaders(address);
            controller.handViewStack(vS);
            controller.passLanData();
            controller.startGame(cfg.player1Name(), cfg.player2Name());

            Scene newScene = new Scene(root, 800, 600);
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDisconnect(String reason) {
        if (disconnected) return;
        disconnected = true;
        Alert alert = new Alert(Alert.AlertType.WARNING,
                "Connection to opponent lost: " + reason,
                ButtonType.OK);
        alert.setTitle("Disconnected");
        alert.setHeaderText("Opponent disconnected");
        alert.showAndWait();
        Session.clear();
        onExitGameAction();
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
                break;
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

        // LAN mode: register listener for NewGame / Disconnect
        NetworkLayer net = Session.current().network;
        if (net != null) {
            if (!Session.current().isHost && playAgainButton != null) {
                playAgainButton.setText("Waiting for host...");
                playAgainButton.setDisable(true);
            }
            net.addListener(new NetworkListener() {
                @Override
                public void onMessage(GameMessage msg) {
                    if (msg instanceof GameMessage.NewGame ng) {
                        Platform.runLater(() -> startNewGameWithSetup(ng.config(), ng.setup()));
                    }
                }
                @Override
                public void onDisconnected(String reason) {
                    Platform.runLater(() -> handleDisconnect(reason));
                }
            });
        }

    }

    public void handViewStack(ViewStack vs){
        this.vS = vs;
    }
}
