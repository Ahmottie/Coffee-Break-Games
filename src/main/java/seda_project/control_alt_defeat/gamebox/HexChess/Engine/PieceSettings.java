package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisSettings;

import java.util.List;

public class PieceSettings {
    private Color p1Color, p2Color;
    private List<ImageView> p1Pieces, p2Pieces;

    private static PieceSettings instance;

    public static PieceSettings getInstance() {
        if (instance == null) {
            instance = new PieceSettings();
        }
        return instance;
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
}


