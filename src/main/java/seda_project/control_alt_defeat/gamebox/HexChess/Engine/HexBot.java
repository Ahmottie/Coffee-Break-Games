package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import java.util.ArrayList;
import java.util.List;

public class HexBot {

    public static String[] getBestMove(String fenState, PlayerColor botColor) {
        GameEngine rootGhost = new GameEngine();
        rootGhost.setupInitalState(fenState);

        List<String[]> allLegalMoves = getAllLegalMoves(rootGhost, botColor);
        if (allLegalMoves.isEmpty()) return null;

        for (String[] move : allLegalMoves) {
            GameEngine ghost = new GameEngine();
            ghost.setupInitalState(fenState);
            makeSimulatedMove(ghost, move[0], move[1]);

            if (ghost.isGameOver() && isKingInCheck(ghost, getOpponent(botColor))) {
                return move;
            }
        }

        List<String[]> safeMoves = new ArrayList<>();

        for (String[] botMove : allLegalMoves) {
            GameEngine ghost = new GameEngine();
            ghost.setupInitalState(fenState);
            makeSimulatedMove(ghost, botMove[0], botMove[1]);

            boolean allowsMate = false;

            if (!ghost.isGameOver()) {
                List<String[]> opponentResponses = getAllLegalMoves(ghost, getOpponent(botColor));
                for (String[] oppMove : opponentResponses) {
                    GameEngine oppGhost = new GameEngine();
                    oppGhost.setupInitalState(ghost.getBoard().createNotation(ghost.getCurrentTurn()));
                    makeSimulatedMove(oppGhost, oppMove[0], oppMove[1]);

                    if (oppGhost.isGameOver() && isKingInCheck(oppGhost, botColor)) {
                        allowsMate = true;
                        break;
                    }
                }
            }

            if (!allowsMate) {
                safeMoves.add(botMove);
            }
        }

        List<String[]> candidateMoves = safeMoves.isEmpty() ? allLegalMoves : safeMoves;
        return pickBestMaterialMove(fenState, candidateMoves, botColor);
    }

    private static void makeSimulatedMove(GameEngine engine, String from, String to) {
        PlayerColor initialTurn = engine.getCurrentTurn();
        engine.handleMove(from, to);
        if (engine.getCurrentTurn() == initialTurn && !engine.isGameOver()) {
            engine.promote(PieceType.QUEEN);
        }
    }

    private static List<String[]> getAllLegalMoves(GameEngine engine, PlayerColor color) {
        List<String[]> moves = new ArrayList<>();
        List<Piece> pieces = (color == PlayerColor.WHITE) ? engine.getBoard().whitePieces() : engine.getBoard().blackPieces();

        for (Piece p : pieces) {
            String fromId = p.getPosition().transformHextoId();
            List<HexCell> legalCells = engine.getLegalMoves(p);
            for (HexCell cell : legalCells) {
                moves.add(new String[]{fromId, cell.getCoords().transformHextoId()});
            }
        }
        return moves;
    }

    private static PlayerColor getOpponent(PlayerColor color) {
        return (color == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;
    }

    private static boolean isKingInCheck(GameEngine engine, PlayerColor player) {
        List<Piece> attackers = (player == PlayerColor.WHITE) ? engine.getBoard().blackPieces() : engine.getBoard().whitePieces();
        List<Piece> defenders = (player == PlayerColor.WHITE) ? engine.getBoard().whitePieces() : engine.getBoard().blackPieces();

        Piece king = defenders.stream().filter(p -> p.getType() == PieceType.KING).findFirst().orElse(null);
        if (king == null) return false;

        HexCoord kingPos = king.getPosition();
        return attackers.stream()
                .flatMap(p -> engine.getRawMoves(p).stream())
                .anyMatch(cell -> cell.getCoords().col == kingPos.col && cell.getCoords().row == kingPos.row);
    }

    private static String[] pickBestMaterialMove(String fenState, List<String[]> moves, PlayerColor botColor) {
        String[] bestMove = moves.get(0);
        int maxScore = Integer.MIN_VALUE;

        for (String[] move : moves) {
            GameEngine ghost = new GameEngine();
            ghost.setupInitalState(fenState);
            makeSimulatedMove(ghost, move[0], move[1]);

            int score = countMaterial(ghost, botColor) - countMaterial(ghost, getOpponent(botColor));
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private static int countMaterial(GameEngine engine, PlayerColor color) {
        List<Piece> pieces = (color == PlayerColor.WHITE) ? engine.getBoard().whitePieces() : engine.getBoard().blackPieces();
        int score = 0;
        for (Piece p : pieces) {
            score += switch (p.getType()) {
                case PAWN -> 1;
                case KNIGHT, BISHOP -> 3;
                case ROOK -> 5;
                case QUEEN -> 9;
                case KING -> 0;
            };
        }
        return score;
    }
}
