package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import java.util.ArrayList;
import java.util.List;

public class HexBot {

    private static final int SEARCH_DEPTH = 3;

    public static String[] getBestMove(String fenState, PlayerColor botColor) {
        GameEngine rootGhost = new GameEngine();
        rootGhost.setupInitalState(fenState);

        List<String[]> allLegalMoves = getAllLegalMoves(rootGhost, botColor);
        if (allLegalMoves.isEmpty()) return null;

        String[] bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (String[] move : allLegalMoves) {
            GameEngine ghost = new GameEngine();
            ghost.setupInitalState(fenState);
            makeSimulatedMove(ghost, move[0], move[1], move[2]);

            // immediate mate
            if (ghost.isGameOver() && isKingInCheck(ghost, getOpponent(botColor))) {
                return move;
            }

            String nextFen = ghost.getBoard().createNotation(ghost.getCurrentTurn());
            int boardValue = minimax(nextFen, SEARCH_DEPTH - 1, alpha, beta, false, botColor);

            if (boardValue > bestValue) {
                bestValue = boardValue;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestValue);
        }

        return bestMove != null ? bestMove : allLegalMoves.get(0);
    }

    private static int minimax(String fenState, int depth, int alpha, int beta, boolean isMaximizingPlayer, PlayerColor botColor) {
        GameEngine engine = new GameEngine();
        engine.setupInitalState(fenState);
        PlayerColor currentTurn = engine.getCurrentTurn();

        if (depth == 0 || engine.isGameOver()) {
            return evaluateBoard(engine, botColor, depth);
        }

        List<String[]> moves = getAllLegalMoves(engine, currentTurn);

        if (moves.isEmpty()) {
            if (isKingInCheck(engine, currentTurn)) {
                return isMaximizingPlayer ? -999999 + depth : 999999 - depth;
            }
            return 0;
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (String[] move : moves) {
                GameEngine ghost = new GameEngine();
                ghost.setupInitalState(fenState);
                makeSimulatedMove(ghost, move[0], move[1], move[2]);
                String nextFen = ghost.getBoard().createNotation(ghost.getCurrentTurn());

                int eval = minimax(nextFen, depth - 1, alpha, beta, false, botColor);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String[] move : moves) {
                GameEngine ghost = new GameEngine();
                ghost.setupInitalState(fenState);
                makeSimulatedMove(ghost, move[0], move[1], move[2]);
                String nextFen = ghost.getBoard().createNotation(ghost.getCurrentTurn());

                int eval = minimax(nextFen, depth - 1, alpha, beta, true, botColor);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private static int evaluateBoard(GameEngine engine, PlayerColor botColor, int depth) {
        if (engine.isGameOver()) {
            if (isKingInCheck(engine, getOpponent(botColor))) return 999999 - depth;
            if (isKingInCheck(engine, botColor)) return -999999 + depth;
            return 0;
        }
        return evaluatePieces(engine, botColor) - evaluatePieces(engine, getOpponent(botColor));
    }

    private static int evaluatePieces(GameEngine engine, PlayerColor color) {
        int score = 0;
        List<Piece> pieces = (color == PlayerColor.WHITE) ? engine.getBoard().whitePieces() : engine.getBoard().blackPieces();

        for (Piece p : pieces) {
            int value = switch (p.getType()) {
                case PAWN -> 100;
                case KNIGHT -> 320;
                case BISHOP -> 330;
                case ROOK -> 500;
                case QUEEN -> 900;
                case KING -> 20000;
            };

            HexCoord pos = p.getPosition();
            int distToCenter = Math.abs(pos.col - 5) + Math.abs(pos.row - 6);

            if (p.getType() == PieceType.KNIGHT) {
                value += (5 - distToCenter) * 15;
            } else if (p.getType() == PieceType.PAWN) {
                value += (5 - distToCenter) * 5;
                if (color == PlayerColor.WHITE) {
                    value += pos.row * 10;
                } else {
                    value += (11 - pos.row) * 10;
                }
            } else if (p.getType() == PieceType.KING) {
                value -= (5 - distToCenter) * 10;
            }
            score += value;
        }
        return score;
    }

    private static void makeSimulatedMove(GameEngine engine, String from, String to, String promoString) {
        PlayerColor initialTurn = engine.getCurrentTurn();
        engine.handleMove(from, to);
        if (engine.getCurrentTurn() == initialTurn && !engine.isGameOver()) {
            PieceType type = promoString.equals("NONE") ? PieceType.QUEEN : PieceType.valueOf(promoString);
            engine.promote(type);
        }
    }

    private static List<String[]> getAllLegalMoves(GameEngine engine, PlayerColor color) {
        List<String[]> moves = new ArrayList<>();
        List<Piece> pieces = (color == PlayerColor.WHITE) ? engine.getBoard().whitePieces() : engine.getBoard().blackPieces();
        List<HexCoord> promoZones = (color == PlayerColor.WHITE) ? engine.getBoard().getWhitePromotions() : engine.getBoard().getBlackPromotions();

        for (Piece p : pieces) {
            String fromId = p.getPosition().transformHextoId();
            List<HexCell> legalCells = engine.getLegalMoves(p);

            boolean isPawn = (p.getType() == PieceType.PAWN);

            for (HexCell cell : legalCells) {
                String toId = cell.getCoords().transformHextoId();

                boolean isPromotion = false;
                if (isPawn) {
                    isPromotion = promoZones.stream().anyMatch(c -> c.col == cell.getCoords().col && c.row == cell.getCoords().row);
                }

                if (isPromotion) {
                    moves.add(new String[]{fromId, toId, "QUEEN"});
                    moves.add(new String[]{fromId, toId, "KNIGHT"});
                    moves.add(new String[]{fromId, toId, "ROOK"});
                    moves.add(new String[]{fromId, toId, "BISHOP"});
                } else {
                    moves.add(new String[]{fromId, toId, "NONE"});
                }
            }
        }

        moves.sort((m1, m2) -> {
            boolean m1Cap = engine.getBoard().getCellById(m1[1]).hasPiece();
            boolean m2Cap = engine.getBoard().getCellById(m2[1]).hasPiece();
            return Boolean.compare(m2Cap, m1Cap);
        });

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
}
