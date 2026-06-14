package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisSettings;

import java.util.Arrays;
import java.util.List;

public class PieceSettings {
    private Color p1Color, p2Color, darkTiles, normalTiles, lightTiles;
    private List<ImageView> p1Pieces, p2Pieces;
    private static final String[] PIECE_NAMES = {"pawn","rook","knight","bishop", "queen", "king"};

    private static PieceSettings instance;

    public static PieceSettings getInstance() {
        if (instance == null) {
            instance = new PieceSettings();
        }
        return instance;
    }

    private PieceSettings(){
        initDefaultPieces();
    }

    private void initDefaultPieces() {
        p1Pieces = loadPieces("White");
        p2Pieces = loadPieces("Black");
    }

    private List<ImageView> loadPieces(String color) {
        ImageView[] views = new ImageView[PIECE_NAMES.length];
        for (int i = 0; i < PIECE_NAMES.length; i++) {
            String path = "/Images/HexChess/" + color + "/" + color.toLowerCase() + "-" + PIECE_NAMES[i] + ".png";
            Image image = new Image(getClass().getResourceAsStream(path));
            views[i] = new ImageView(image);
        }
        return Arrays.asList(views);
    }

    public Color getP1Color() {
        return p1Color;
    }

    public void setP1Color(Color p1Color) {
        this.p1Color = p1Color;
    }

    public Color getP2Color() {
        return p2Color;
    }

    public void setP2Color(Color p2Color) {
        this.p2Color = p2Color;
    }

    public List<ImageView> getP1Pieces() {
        return p1Pieces;
    }

    public void setP1Pieces(List<ImageView> p1Pieces) {
        this.p1Pieces = p1Pieces;
    }

    public List<ImageView> getP2Pieces() {
        return p2Pieces;
    }

    public void setP2Pieces(List<ImageView> p2Pieces) {
        this.p2Pieces = p2Pieces;
    }

    public Color getDarkTiles() {
        return darkTiles;
    }

    public void setDarkTiles(Color darkTiles) {
        this.darkTiles = darkTiles;
    }

    public Color getNormalTiles() {
        return normalTiles;
    }

    public void setNormalTiles(Color normalTiles) {
        this.normalTiles = normalTiles;
    }

    public Color getLightTiles() {
        return lightTiles;
    }

    public void setLightTiles(Color lightTiles) {
        this.lightTiles = lightTiles;
    }
}


