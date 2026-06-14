package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import seda_project.control_alt_defeat.gamebox.Tetris.Engine.TetrisEngine;

public interface ChessEventListener {
    default void onPlaced(String id, Piece piece) {}

    void move(String fromId, String toId);

    void capture(String fromId, String toId, Piece capturedPiece);

    void gameEnd(PlayerColor player);

    void enpassent(String enpassentId, Piece enpassentPiece);

    void remis();

    void stalemate(PlayerColor currentTurn);

    void promotion(PlayerColor currentTurn);

    void onPromoted(String coordId, Piece promoted);

    void endangered(Piece king, boolean endangered);
}
