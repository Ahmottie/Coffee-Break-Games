package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.GameEngine;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceType;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

import java.util.List;
import java.util.Map;

public class Promotion {

    private GameEngine gameEngine;
    private ImageView selected;

    private static final Map<String, PieceType> VIEW_TO_TYPE = Map.of(
            "RookImgView",   PieceType.ROOK,
            "KnightImgView", PieceType.KNIGHT,
            "BishopImgView", PieceType.BISHOP,
            "QueenImgView",  PieceType.QUEEN
    );


    @FXML
    private StackPane stackpane;

    @FXML
    private ImageView RookImgView, KnightImgView,  BishopImgView,  QueenImgView;

    public void sendPieces(List<ImageView> views, GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        RookImgView.setImage(views.get(1).getImage());
        KnightImgView.setImage(views.get(2).getImage());
        BishopImgView.setImage(views.get(3).getImage());
        QueenImgView.setImage(views.get(4).getImage());
    }

    @FXML
    protected void onPieceClickedAction(MouseEvent event){
        deselectall();
        ImageView view = (ImageView) event.getTarget();
        view.getStyleClass().add("selectedPiece");
        selected = view;
    }

    private void deselectall() {
        RookImgView.getStyleClass().clear();
        KnightImgView.getStyleClass().clear();
        BishopImgView.getStyleClass().clear();
        QueenImgView.getStyleClass().clear();
    }

    @FXML
    protected void onPromoteAction(){
        if (selected == null){
            Toast.makeText(stackpane,"You need to select a Pice!");
            return;
        }
        PieceType chosenType = VIEW_TO_TYPE.get(selected.getId());
        if (chosenType == null) {
            Toast.makeText(stackpane, "Invalid piece selection!");
            return;
        }
        gameEngine.promote(chosenType);

        ((Stage) stackpane.getScene().getWindow()).close();
    }
}
