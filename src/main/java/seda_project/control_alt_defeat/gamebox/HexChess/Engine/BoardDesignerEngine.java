package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import javafx.scene.shape.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BoardDesignerEngine {
    private Map<String, Integer> pieceAmounts;
    private int startingPlayer = 1;

    public static Map<String, Integer> defaultPieceAmounts() {
        return new HashMap<>(Map.ofEntries(
                Map.entry("p1PawnImg",   9),
                Map.entry("p1RookImg",   2),
                Map.entry("p1KnightImg", 2),
                Map.entry("p1BishopImg", 3),
                Map.entry("p1QueenImg",  1),
                Map.entry("p1KingImg",   1),
                Map.entry("p2PawnImg",   9),
                Map.entry("p2RookImg",   2),
                Map.entry("p2KnightImg", 2),
                Map.entry("p2BishopImg", 3),
                Map.entry("p2QueenImg",  1),
                Map.entry("p2KingImg",   1)
        ));
    }

    public BoardDesignerEngine() {
        this.pieceAmounts = defaultPieceAmounts();
    }

    public Map<String, Integer> getPieceAmounts() {
        return pieceAmounts;
    }

    public Map<String, String> computeIncrement(String pieceId) {
        String playerPrefix = pieceId.contains("1") ? "p1" : "p2";
        int amount = pieceAmounts.get(pieceId);
        pieceAmounts.put(pieceId, amount + 1);
        for (String key : pieceAmounts.keySet()) {
            if (key.equals(pieceId)) {
                if (key.startsWith(playerPrefix)) {
                    System.err.println(key);
                    if (!key.contains("King") && !key.contains("Pawn")) {
                        System.out.println("NO KING or PAWN");
                        if ((key.contains("Rook") || key.contains("Knight")) && amount >= 2) {
                            System.err.println("Decrement ROOK|KNIGHT");
                            pieceAmounts.put(pieceId, amount);
                            pieceAmounts.put(playerPrefix + "PawnImg",
                                    pieceAmounts.get(playerPrefix + "PawnImg") + 1);
                        }
                        if (key.contains("Bishop") && amount >= 3) {
                            System.err.println("Decrement BISHOP");
                            pieceAmounts.put(pieceId, amount);
                            pieceAmounts.put(playerPrefix + "PawnImg",
                                    pieceAmounts.get(playerPrefix + "PawnImg") + 1);
                        }
                        if (key.contains("Queen") && amount >= 1) {
                            System.err.println("Decrement QUEEN");
                            System.err.println("second Queen Detected");
                            pieceAmounts.put(pieceId, amount);
                            pieceAmounts.put(playerPrefix + "PawnImg",
                                    pieceAmounts.get(playerPrefix + "PawnImg") + 1);
                        }
                    }
                }
            }
        }
        return buildLabelValues(playerPrefix);
    }

    public Map<String, String> computeAdapt(String pieceId) {
        String playerPrefix = pieceId.contains("1") ? "p1" : "p2";
        boolean isPawn = pieceId.contains("Pawn");

        if (isPawn) {
            int val = pieceAmounts.get(playerPrefix + "PawnImg");
            if (val > 0) pieceAmounts.put(playerPrefix + "PawnImg", val - 1);
        } else {
            int currentAmount = pieceAmounts.get(pieceId);
            if (currentAmount > 0) {
                pieceAmounts.put(pieceId, currentAmount - 1);
            } else {
                int pawnAmount = pieceAmounts.get(playerPrefix + "PawnImg");
                pieceAmounts.put(playerPrefix + "PawnImg", pawnAmount - 1);
            }
        }

        return buildLabelValues(playerPrefix);
    }

    public Map<String, String> buildLabelValues(String playerPrefix) {
        Map<String, String> result = new HashMap<>();
        for (String key : pieceAmounts.keySet()) {
            if (!key.startsWith(playerPrefix)) continue;
            if (!key.contains("King") && !key.contains("Pawn")) {
                int totalAvailable = pieceAmounts.get(key)
                        + pieceAmounts.get(playerPrefix + "PawnImg");
                result.put(key, String.valueOf(totalAvailable));
                System.out.println(key + " Piece Count " + pieceAmounts.get(key));
                System.out.println(key + " Total Available " +  totalAvailable);
            } else if (key.contains("Pawn") || key.contains("King")) {
                result.put(key, String.valueOf(pieceAmounts.get(key)));
                System.out.println(key + " Total Available " + pieceAmounts.get(key));
            }
        }
        return result;
    }

    public String createNotation(List<List<Polygon>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(rows.stream()
                .map(this::encodeRow)
                .collect(Collectors.joining("/")));
        sb.append(" ");
        sb.append(startingPlayer);
        return sb.toString();
    }

    private String encodeRow(List<Polygon> polygons) {
        int emptyCells = 0;
        StringBuilder sb = new StringBuilder();
        for (Polygon polygon : polygons) {
            Object userData = polygon.getUserData();
            if (userData != null) {
                String data = userData.toString();
                if (emptyCells > 0) {
                    sb.append(emptyCells);
                    emptyCells = 0;
                }
                String piece = data.replace("Img", "").replaceFirst("p[12]", "");
                String symbol = switch (piece) {
                    case "Pawn" -> "p";
                    case "Rook" -> "r";
                    case "Knight" -> "n";
                    case "Bishop" -> "b";
                    case "Queen" -> "q";
                    case "King" -> "k";
                    default -> "?";
                };
                if (data.contains("p1")) {
                    symbol = symbol.toUpperCase();
                }
                sb.append(symbol);
            } else {
                emptyCells++;
            }
        }
        if (emptyCells > 0) {
            sb.append(emptyCells);
        }
        return sb.toString();
    }

    public void loadExport(BoardDesignState state) {
        pieceAmounts = new HashMap<>(Map.ofEntries(
                Map.entry("p1PawnImg",   state.getP1Pawn()),
                Map.entry("p1RookImg",   state.getP1Rook()),
                Map.entry("p1KnightImg", state.getP1Knight()),
                Map.entry("p1BishopImg", state.getP1Bishop()),
                Map.entry("p1QueenImg",  state.getP1Queen()),
                Map.entry("p1KingImg",   state.getP1King()),
                Map.entry("p2PawnImg",   state.getP2Pawn()),
                Map.entry("p2RookImg",   state.getP2Rook()),
                Map.entry("p2KnightImg", state.getP2Knight()),
                Map.entry("p2BishopImg", state.getP2Bishop()),
                Map.entry("p2QueenImg",  state.getP2Queen()),
                Map.entry("p2KingImg",   state.getP2King())
        ));
        pieceAmounts.entrySet().forEach(System.out::println);
    }

    public void activePlayer(int x){
        this.startingPlayer = x;
    }
    public int getActivePlayer(){
        return this.startingPlayer;
    }
}