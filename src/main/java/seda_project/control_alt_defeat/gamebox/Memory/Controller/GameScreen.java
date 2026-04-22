package seda_project.control_alt_defeat.gamebox.Memory.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.GameBox;
import seda_project.control_alt_defeat.gamebox.Memory.Configuration;
import seda_project.control_alt_defeat.gamebox.Memory.ViewStack;

public class GameScreen {
    ViewStack vS;
    @FXML
    private VBox header;

    @FXML
    private Label sboardP1,sboardP2,sboardScoreP1,sboardScoreP2,activePlayerLabel;

    @FXML
    private AnchorPane gamePane;

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

        createBoard(tupleSize,deckSize);
    }

    private void createBoard(int tupleSize, int deckSize) {
        GridPane playingGrid  = new GridPane();

        playingGrid.setHgap(10);
        playingGrid.setVgap(10);
        playingGrid.setPadding(new Insets(10));

        int col = (int)Math.sqrt(deckSize);
        int row = (int) deckSize/col;

        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                Button cell = new Button("?");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // fill cell
                playingGrid.add(cell, j, i);

                GridPane.setHgrow(cell,Priority.ALWAYS);
                GridPane.setVgrow(cell,Priority.ALWAYS);
            }
        }

        gamePane.getChildren().add(playingGrid);
        AnchorPane.setBottomAnchor(playingGrid,20.0);
        AnchorPane.setTopAnchor(playingGrid,20.0);
        AnchorPane.setLeftAnchor(playingGrid,20.0);
        AnchorPane.setRightAnchor(playingGrid,20.0);
    }


}
