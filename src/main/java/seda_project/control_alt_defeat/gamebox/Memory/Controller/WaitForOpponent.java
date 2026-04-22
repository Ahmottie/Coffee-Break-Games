package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

public class WaitForOpponent {
    private ViewStack vS;
    private boolean host;
    private boolean ready;
    private String hostName;
    private String joinName;
    private int tupleSize;
    private int deckSize;

    @FXML
    private VBox header;

    @FXML
    private Button startGameButton;

    @FXML
    private Label yourNameLabel, opponentNameLabel, deckSizeLabel, matchSizeLabel, statusLabel;


    @FXML
    private void onBackAction(){
        try{
            vS.popFxmlLoader();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource(vS.getFxmlLoader()));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof HostLan c){
                c.handViewStack(vS);
                c.backTransfer(hostName,tupleSize,deckSize);
                Scene newScene = new Scene(root, 800, 600);
                Stage stage = (Stage) header.getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            }
            if (controller instanceof JoinLan c){
                c.handViewStack(vS);
                c.backTransfer(joinName);
                Scene newScene = new Scene(root, 800, 600);
                Stage stage = (Stage) header.getScene().getWindow();
                stage.setScene(newScene);
                stage.show();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onStartGameAction (){
        if (!host){
            //TODO Send Client Ready to Host
            //TODO Change Status to "Waiting for game start"
            //TODO Change Button from "Ready" to ->?
            if (!ready){
                startGameButton.setText("Not Ready");
                statusLabel.setText("Waiting for the Host to start the Game!");
                ready = !ready;
                //TODO Send this to the host
            }
            else{
                statusLabel.setText("Waiting for " + joinName + " to be ready!");
                startGameButton.setText("Ready");
                ready = !ready;
                //TODO Send this to the host
            }

        }
    }

    public void passJoinData(ViewStack vs, boolean host, String playerName){
        this.vS = vs;
        this.host = host;
        this.ready = false;
        this.joinName = playerName;

        startGameButton.setText("Ready");
        yourNameLabel.setText(playerName);

        //TODO get the data from the Host e.g. HostName, DeckSize, MatchSize
    }

    public void passHostData(ViewStack vS, boolean host, String hostName, int tupleSize, int deckSize) {
        this.vS = vS;
        this.host = host;
        this.hostName = hostName;
        this.tupleSize = tupleSize;
        this.deckSize = deckSize;

        yourNameLabel.setText(hostName);
        deckSizeLabel.setText(Integer.toString(deckSize));
        matchSizeLabel.setText(Integer.toString(tupleSize));

        statusLabel.setText("Waiting for an opponent to Join!");
    }
}
