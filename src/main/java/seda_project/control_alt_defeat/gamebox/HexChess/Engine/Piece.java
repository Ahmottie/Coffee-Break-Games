package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

public class Piece {
    private PieceType type;
    private final PlayerColor player;
    private boolean moved;
    private HexCoord position;

    public Piece(PieceType type, PlayerColor player){
        this.type = type;
        this.player = player;
        this.moved = false;
    }

    public PieceType getType() {
        return type;
    }

    public PlayerColor getPlayer() {
        return player;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public HexCoord getPosition() {
        return position;
    }

    public void setPosition(HexCoord position) {
        this.position = position;
    }
}
