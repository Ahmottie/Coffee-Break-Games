package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GameScreen extends Controller implements Initializable {
    PieceSettings settings = PieceSettings.getInstance();

    @FXML
    private Parent root;

    @FXML
    private ImageView p1PawnImg, p1RookImg, p1KnightImg, p1BishopImg, p1QueenImg, p1KingImg;
    @FXML
    private ImageView p2PawnImg, p2RookImg, p2KnightImg, p2BishopImg, p2QueenImg, p2KingImg;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<ImageView> p1Pieces = settings.getP1Pieces();
        if  (p1Pieces != null) {
            p1PawnImg.setImage(p1Pieces.get(0).getImage());
            p1RookImg.setImage(p1Pieces.get(1).getImage());
            p1KnightImg.setImage(p1Pieces.get(2).getImage());
            p1BishopImg.setImage(p1Pieces.get(3).getImage());
            p1QueenImg.setImage(p1Pieces.get(4).getImage());
            p1KingImg.setImage(p1Pieces.get(5).getImage());
        }
        List<ImageView> p2Pieces = settings.getP2Pieces();
        if (p2Pieces != null) {
            p2PawnImg.setImage(p2Pieces.get(0).getImage());
            p2RookImg.setImage(p2Pieces.get(1).getImage());
            p2KnightImg.setImage(p2Pieces.get(2).getImage());
            p2BishopImg.setImage(p2Pieces.get(3).getImage());
            p2QueenImg.setImage(p2Pieces.get(4).getImage());
            p2KingImg.setImage(p2Pieces.get(5).getImage());
        }

        StringBuilder style = new StringBuilder();
        if (settings.getDarkTiles() != null){
            style.append("-dark-poly-color: ")
                    .append(toCssColor(settings.getDarkTiles()))
                    .append(";");
            root.setStyle(style.toString());
        }
        if (settings.getNormalTiles() != null){
            style.append("-normal-poly-color: ")
                    .append(toCssColor(settings.getNormalTiles()))
                    .append(";");
            root.setStyle(style.toString());
        }
        if (settings.getLightTiles() != null){
            style.append("-light-poly-color: ")
                    .append(toCssColor(settings.getLightTiles()))
                    .append(";");
            root.setStyle(style.toString());
        }
    }
    private static String toCssColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
    }
}
