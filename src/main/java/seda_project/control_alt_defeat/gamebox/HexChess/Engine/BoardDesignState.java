package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import com.fasterxml.jackson.databind.JsonNode;

public class BoardDesignState {
    private JsonNode board;
    private String FENState;
    private int p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King;
    private int p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King;

    public BoardDesignState(JsonNode board){
        this.board = board;

        this.FENState = board.get("FENState").toString();

        this.p1Pawn = board.get("p1Pawn").asInt();
        this.p1Rook = board.get("p1Rook").asInt();
        this.p1Knight = board.get("p1Knight").asInt();
        this.p1Bishop = board.get("p1Bishop").asInt();
        this.p1Queen = board.get("p1Queen").asInt();
        this.p1King = board.get("p1King").asInt();

        this.p2Pawn = board.get("p2Pawn").asInt();
        this.p2Rook = board.get("p2Rook").asInt();
        this.p2Knight = board.get("p2Knight").asInt();
        this.p2Bishop = board.get("p2Bishop").asInt();
        this.p2Queen = board.get("p2Queen").asInt();
        this.p2King = board.get("p2King").asInt();

        int starting = board.get("startingPlayer").asInt();
    }

    public String getFENState() {
        return FENState;
    }

    public int getP1Pawn() {
        return p1Pawn;
    }

    public int getP1Rook() {
        return p1Rook;
    }

    public int getP1Knight() {
        return p1Knight;
    }

    public int getP1Bishop() {
        return p1Bishop;
    }

    public int getP1Queen() {
        return p1Queen;
    }

    public int getP1King() {
        return p1King;
    }

    public int getP2King() {
        return p2King;
    }

    public int getP2Queen() {
        return p2Queen;
    }

    public int getP2Bishop() {
        return p2Bishop;
    }

    public int getP2Knight() {
        return p2Knight;
    }

    public int getP2Rook() {
        return p2Rook;
    }

    public int getP2Pawn() {
        return p2Pawn;
    }

    public JsonNode getBoard() {
        return board;
    }
}
