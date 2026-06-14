package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

public class HexCell {
    private HexCoord coords;
    private Piece piece;

    public HexCell (HexCoord coord){
        this.coords = coord;
    }

    public HexCoord getCoords() {
        return coords;
    }

    public void setPiece (Piece newPiece){
        this.piece = newPiece;
    }

    public Piece getPiece(){
        return this.piece;
    }

    public boolean hasPiece(){
        return this.piece != null;
    }

    public boolean noPiece(){
        return this.piece == null;
    }

}
