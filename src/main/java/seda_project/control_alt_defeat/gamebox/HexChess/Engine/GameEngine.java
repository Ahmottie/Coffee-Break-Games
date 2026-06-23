package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceType.PAWN;

public class GameEngine {
    private final Board board;
    private PlayerColor currentTurn;
    private boolean isGameOver;
    private final List<ChessEventListener> listeners = new ArrayList<>();
    private boolean enpassent = false;
    private boolean enpassentPending = false;
    private HexCoord enpassentCoordGhost;
    private HexCoord enpassentMovedTo;
    private int halfmove;
    private int fullmove;
    private Map<String,Integer> fenMap = new HashMap<>();
    private HexCoord pendingPromotionCoord;
    private static final int[] ROW_OFFSETS = {0,0,0,0,0,0,1,2,3,4,5};

    public GameEngine() {
        fullmove = 1;
        halfmove = 1;
        this.board = new Board();
        this.currentTurn = PlayerColor.WHITE;
        this.isGameOver = false;
    }

    public void addListener(ChessEventListener listener){
        listeners.add(listener);
    }

    public boolean handleMove(String fromId, String toId) {
        if (isGameOver) {
            return false;
        }

        HexCell fromCell = board.getCellById(fromId);
        // Check if there's actually a piece to move
        if (!fromCell.hasPiece()) {
            return false;
        }

        Piece pieceToMove = fromCell.getPiece();

        // Only allow the current Player to move
        if (pieceToMove.getPlayer() != currentTurn) {
            return false;
        }

        HexCoord fromCoord = HexCoord.transformIdToHex(fromId);
        HexCoord toCoord = HexCoord.transformIdToHex(toId);

        if (!isValidMove(fromCoord, toCoord, pieceToMove)) {
            return false;
        }
        boolean e = false;
        if (pieceToMove.getType() == PAWN){
            int diff = getDiff(fromId,toId);
            if (diff == 2){
                e = true;
                enpassentCoordGhost = getEnpassentCoord(fromId,toId);
                enpassentMovedTo = toCoord;
            }
        }
        // Actual Movement
        Piece capturedPiece = board.movePiece(fromCoord, toCoord);
        pieceToMove.setMoved(true);

        if (pieceToMove.getType() == PAWN
                && enpassent
                && toCoord.col == enpassentCoordGhost.col
                && toCoord.row == enpassentCoordGhost.row) {
            HexCell epcell = board.getCellByCoord(enpassentMovedTo.col,enpassentMovedTo.row);
            Piece epPiece = epcell.getPiece();
            board.removePiece(enpassentMovedTo); // remove the skipped pawn
            listeners.forEach(l -> l.enpassent(enpassentMovedTo.transformHextoId(),epPiece));
        }

        if (pieceToMove.getType() == PAWN) {
            boolean isPromotion = (currentTurn == PlayerColor.WHITE)
                    ? board.WHITE_PROMOTION.stream().anyMatch(c -> c.col == toCoord.col && c.row == toCoord.row)
                    : board.BLACK_PROMOTION.stream().anyMatch(c -> c.col == toCoord.col && c.row == toCoord.row);
            if (isPromotion) {
                pendingPromotionCoord = toCoord;
                listeners.forEach(l -> l.promotion(currentTurn));
            }
        }

        if (capturedPiece != null) {
            if (capturedPiece.getType() == PieceType.KING) {
                isGameOver = true;
                listeners.forEach( l -> l.gameEnd(capturedPiece.getPlayer()));
                return true;
            }
        }

        if (capturedPiece == null) {
            listeners.forEach(l -> l.move(fromId,toId));
        }
        else {
            listeners.forEach(l->l.capture(fromId,toId,capturedPiece));
        }


        incrementCounter(pieceToMove, capturedPiece);
        if (checkRemis()) {
            isGameOver = true;
            listeners.forEach(l ->l.remis());
            return true;
        }

        if (pendingPromotionCoord != null) {
            return true;
        }

        PlayerColor nextTurn = (currentTurn == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;

        if (!playerLegalMoves(nextTurn)) {
            isGameOver = true;
            if (isKingInCheck(nextTurn)) {
                listeners.forEach(l -> l.gameEnd(nextTurn));
            } else {
                listeners.forEach(l -> l.stalemate(nextTurn));
            }
            return true;
        }

        checkEndangered();
        switchTurn(e);
        return true;
    }

    public boolean isKingInCheck(PlayerColor player) {
        List<Piece> attackers = (player == PlayerColor.WHITE) ? board.blackPieces() : board.whitePieces();
        List<Piece> defenders = (player == PlayerColor.WHITE) ? board.whitePieces() : board.blackPieces();

        Piece king = defenders.stream()
                .filter(p -> p.getType() == PieceType.KING)
                .findFirst()
                .orElse(null);
        if (king == null) return false;

        HexCoord kingPos = king.getPosition();
        return attackers.stream()
                .flatMap(p -> getRawMoves(p).stream())
                .anyMatch(cell -> cell.getCoords().col == kingPos.col
                        && cell.getCoords().row == kingPos.row);
    }

    private boolean playerLegalMoves(PlayerColor player) {
        List<Piece> pieces = (player == PlayerColor.WHITE) ? board.whitePieces() : board.blackPieces();
        return pieces.stream().anyMatch(p -> !getLegalMoves(p).isEmpty());
    }

    public void promote(PieceType chosenType) {
        if (pendingPromotionCoord == null) return;

        HexCell cell = board.getCellByCoord(pendingPromotionCoord.col, pendingPromotionCoord.row);
        Piece pawn = cell.getPiece();
        if (pawn == null) { pendingPromotionCoord = null; return; }

        Piece promoted = new Piece(chosenType, pawn.getPlayer());
        promoted.setMoved(true);
        promoted.setPosition(pendingPromotionCoord);

        if (pawn.getPlayer() == PlayerColor.WHITE) {
            board.whitePieces().remove(pawn);
            board.whitePieces().add(promoted);
        } else {
            board.blackPieces().remove(pawn);
            board.blackPieces().add(promoted);
        }
        cell.setPiece(promoted);

        String coordId = pendingPromotionCoord.transformHextoId();
        pendingPromotionCoord = null;

        listeners.forEach(l -> l.onPromoted(coordId, promoted));

        if (checkRemis()) {
            isGameOver = true;
            listeners.forEach(l ->l.remis());
        }


        PlayerColor nextTurn = (currentTurn == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;

        if (!playerLegalMoves(nextTurn)) {
            isGameOver = true;
            if (isKingInCheck(nextTurn)) {
                listeners.forEach(l -> l.gameEnd(nextTurn));
            } else {
                listeners.forEach(l -> l.stalemate(nextTurn));
            }
        }

        checkEndangered();
        switchTurn(false);
    }

    private boolean checkRemis() {
        if (halfmove == 100){
            return true;
        }
        if (checkMaterial()){
            return true;
        }
        String notation = board.createNotation(currentTurn);
        if (enpassentPending) {
            notation = notation + " " + enpassentCoordGhost.transformHextoId();
        }
        else {
            notation = notation + " -";
        }
        if (fenMap.get(notation)!= null){
            int amount = fenMap.get(notation);
            if (amount == 2){
                return true;
            }
            else {
                fenMap.put(notation,amount+1);
            }
        }
        else {
            fenMap.put(notation,1);
        }
        return false;
    }

    private void incrementCounter(Piece pieceToMove, Piece capturedPiece) {
        if (pieceToMove.getType() == PAWN || capturedPiece != null){
            halfmove = 1;
        }
        else {
            halfmove ++;
        }
        if (pieceToMove.getPlayer() == PlayerColor.BLACK){
            fullmove ++;
        }
    }

    private boolean checkStaleMate() {
        List<Piece> activePieces = (currentTurn == PlayerColor.WHITE)
                ? board.whitePieces()
                : board.blackPieces();

        List<Piece> enemyPieces = (currentTurn == PlayerColor.WHITE) ? board.blackPieces() : board.whitePieces();
        List<HexCell> allactiveMoves = (List<HexCell>) activePieces.stream().flatMap(p -> getRawMoves(p).stream()).toList();

        Piece enemyKing = enemyPieces.stream().filter(p -> p.getType() == PieceType.KING).toList().getFirst();
        List<HexCell> possibleKingmoves = getRawMoves(enemyKing);

        for (HexCell move : allactiveMoves) {
            if(possibleKingmoves.isEmpty()){
                return true;
            }
            possibleKingmoves.remove(move);
        }
        return possibleKingmoves.isEmpty();
    }



    public boolean checkMaterial(){
        List<Piece> white = board.whitePieces();
        List<Piece> black = board.blackPieces();

        if (white.size() == 1 && black.size() == 1){
            return true;
        }
        if (checkKingKingKnightKnight(white, black) || checkKingKingKnightKnight(black, white)){
            return true;
        }
        if (checkKingKingBishop(black,white) || checkKingKingBishop(white,black)){
            return true;
        }
        if (checkKingKnighKing(black,white) || checkKingKnighKing(white,black)){
            return true;
        }
        if (checkKingBishopKingBishop(black,white)){
            return true;
        }
        return false;
    }

    private boolean checkKingKingBishop(List<Piece> first, List<Piece> second) {
        if (first.size() == 1 && second.size() == 2) {
            long bishopcount = second.stream().filter(p -> p.getType() == PieceType.BISHOP).count();
            if (bishopcount == 1){
                return true;
            }
        }
        return false;
    }

    private boolean checkKingKnighKing(List<Piece> first, List<Piece> second) {
        if (first.size() == 1 && second.size() == 2) {
            long knightcount = second.stream().filter(p -> p.getType() == PieceType.KNIGHT).count();
            if (knightcount == 1){
                return true;
            }
        }
        return false;
    }

    private boolean checkKingBishopKingBishop(List<Piece> first, List<Piece> second) {
        if (first.size() == second.size() && first.size() == 2){
            long firstBishop = first.stream().filter(p -> p.getType() == PieceType.BISHOP).count();
            long secondBishop = second.stream().filter(p -> p.getType() == PieceType.BISHOP).count();
            if (firstBishop == 1 && secondBishop == 1){
                return true;
            }
        }
        return false;
    }

    private boolean checkKingKingKnightKnight(List<Piece> first, List<Piece> second) {
        if (first.size() == 1 && second.size() == 3){
            long knightcount = second.stream().filter(p -> p.getType() == PieceType.KNIGHT).count();
            if (knightcount == 2){
                return true;
            }
        }
        return false;
    }

    private int getDiff(String fromId, String toId) {
        int from = Integer.parseInt(fromId.replaceAll("[abcdefghijk]",""));
        int to = Integer.parseInt(toId.replaceAll("[abcdefghijk]",""));
        int diff = Math.abs(from-to);
        return diff;
    }

    private boolean isValidMove(HexCoord from, HexCoord to, Piece piece) {
        List<HexCell> legal = getRawMoves(piece);
        return legal.stream().anyMatch(cell ->
                cell.getCoords().col == to.col && cell.getCoords().row == to.row
        );
    }

    private void switchTurn(boolean e) {
        enpassent = e;
        enpassentPending = false;
        this.currentTurn = (this.currentTurn == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;
        listeners.forEach(l -> l.activePlayer(currentTurn));
    }

    // Getters for your UI layer to inspect the board state
    public Board getBoard() {
        return this.board;
    }
    public PlayerColor getCurrentTurn() {
        return this.currentTurn;
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    public List<HexCell> getLegalMoves(Piece piece){
        HexCoord currentPos = piece.getPosition();
        List<HexCell> candidates  = getRawMoves(piece);
        candidates.removeIf(cell -> wouldLeaveKingInCheck(piece, currentPos, cell.getCoords()));
        return candidates;
    }

    public List<HexCell> getRawMoves(Piece piece) {

        HexCoord coord = piece.getPosition();
        List<HexCell> candidates = new ArrayList<>();

        switch (piece.getType()) {
            case ROOK   -> candidates = board.getallOrthogonals(coord);
            case BISHOP -> candidates = board.getAllDiagonals(coord);
            case KNIGHT -> candidates = board.getJumpNeighbours(coord);
            case QUEEN  -> {
                candidates.addAll(board.getallOrthogonals(coord));
                candidates.addAll(board.getAllDiagonals(coord));
            }
            case KING   -> {
                candidates.addAll(board.getDirectOrthogonalNeighbors(coord));
                candidates.addAll(board.getDirectDiagonalNeighbors(coord));
            }
            case PAWN   -> candidates = getPawnMoves(coord, piece.getPlayer(), piece.isMoved());
        }

        // Filter out cells occupied by own pieces
        PlayerColor ownColor = piece.getPlayer();
        candidates.removeIf(cell -> cell.hasPiece() && cell.getPiece().getPlayer() == ownColor);

        return candidates;
    }

    private boolean wouldLeaveKingInCheck(Piece piece, HexCoord from, HexCoord to) {
        HexCell fromCell = board.getCellByCoord(from.col,from.row);
        HexCell toCell = board.getCellByCoord(to.col,to.row);

        Piece originalAtTo = toCell.getPiece();

        //Simulate move
        toCell.setPiece(piece);
        fromCell.setPiece(null);
        piece.setPosition(to);

        PlayerColor ownColor = piece.getPlayer();
        List<Piece> ownPieces = (ownColor == PlayerColor.WHITE) ? board.whitePieces() : board.blackPieces();
        List<Piece> enemyPieces = (ownColor == PlayerColor.WHITE) ? board.blackPieces() : board.whitePieces();

        Piece king = (piece.getType() == PieceType.KING) ? piece : ownPieces.stream().filter(p -> p.getType() == PieceType.KING).findFirst().orElse(null);

        boolean inCheck = false;

        if (king != null) {
            HexCoord kingPos = king.getPosition();
            int kingCol = kingPos.col;
            int kingRow = kingPos.row;
            inCheck = enemyPieces.stream()
                    .filter(p -> p != originalAtTo)  // exclude captured piece without mutating the list
                    .flatMap(enemy -> getRawMoves(enemy).stream())
                    .anyMatch(cell -> cell.getCoords().col == kingCol
                            && cell.getCoords().row == kingRow);
        }

        // Undo the move
        fromCell.setPiece(piece);
        toCell.setPiece(originalAtTo);
        piece.setPosition(from);


        return inCheck;
    }

    private List<HexCell> getPawnMoves(HexCoord coord, PlayerColor color, boolean moved) {
        List<HexCell> moves = new ArrayList<>();
        int col = coord.col;
        int row = coord.row;

        int dir = (color == PlayerColor.WHITE) ? 0 : 1; // dir 0 = up, dir 1 = down
        int[] stepsize = moved ? new int[]{1} : new int[]{1,2};
        for (int step : stepsize) {
            int fwdRow = (dir == 0) ? row + step : row - step;
            if (coord.isValid(col, fwdRow)) {
                HexCell fwd = board.getCellByCoord(col, fwdRow);
                if (!fwd.hasPiece()) {
                    moves.add(fwd);
                }
                else break;
            }
        }

        int leftRowOffset  = (col <= 5) ? 0 : 1;
        int rightRowOffset = (col >= 5) ? 0 : 1;

        int[] captureCols = { col - 1, col + 1 };
        int[] captureRows = (color == PlayerColor.WHITE)
                ? new int[]{ row + leftRowOffset, row + rightRowOffset }
                : new int[]{ row - 1 + leftRowOffset, row - 1 + rightRowOffset };

        for (int i = 0; i < 2; i++) {
            if (coord.isValid(captureCols[i], captureRows[i])) {
                HexCell cell = board.getCellByCoord(captureCols[i], captureRows[i]);
                if (cell.hasPiece() && cell.getPiece().getPlayer() != color) {
                    moves.add(cell);
                }
                if (enpassent && enpassentCoordGhost != null
                        && enpassentCoordGhost.col == captureCols[i]
                        && enpassentCoordGhost.row == captureRows[i]) {
                    moves.add(cell);
                }
            }
        }

        return moves;
    }

    private HexCoord getEnpassentCoord(String fromId, String toId) {
        int from = Integer.parseInt(fromId.replaceAll("[abcdefghijk]",""));
        int to = Integer.parseInt(toId.replaceAll("[abcdefghijk]",""));
        String enpassentId = "";
        if (from > to ){
            enpassentId = (fromId.charAt(0)) + String.valueOf(to+1);
        }
        else {
            enpassentId = (fromId.charAt(0)) + String.valueOf(from+1);
        }
        HexCoord coord = HexCoord.transformIdToHex(enpassentId);
        return coord;
    }

    public void setupInitialState(){

        //Standart placement
        setupPiece("b1", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("c2", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("d3", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("e4", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("f5", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("g4", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("h3", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("i2", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("j1", new Piece(PAWN, PlayerColor.WHITE));
        setupPiece("c1", new Piece(PieceType.ROOK, PlayerColor.WHITE));
        setupPiece("i1", new Piece(PieceType.ROOK, PlayerColor.WHITE));
        setupPiece("d1", new Piece(PieceType.KNIGHT, PlayerColor.WHITE));
        setupPiece("h1", new Piece(PieceType.KNIGHT, PlayerColor.WHITE));
        setupPiece("f1", new Piece(PieceType.BISHOP, PlayerColor.WHITE));
        setupPiece("f2", new Piece(PieceType.BISHOP, PlayerColor.WHITE));
        setupPiece("f3", new Piece(PieceType.BISHOP, PlayerColor.WHITE));
        setupPiece("e1", new Piece(PieceType.QUEEN, PlayerColor.WHITE));
        setupPiece("g1", new Piece(PieceType.KING, PlayerColor.WHITE));

        setupPiece("b7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("c7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("d7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("e7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("f7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("g7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("h7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("i7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("j7", new Piece(PAWN, PlayerColor.BLACK));
        setupPiece("c8", new Piece(PieceType.ROOK, PlayerColor.BLACK));
        setupPiece("i8", new Piece(PieceType.ROOK, PlayerColor.BLACK));
        setupPiece("d9", new Piece(PieceType.KNIGHT, PlayerColor.BLACK));
        setupPiece("h9", new Piece(PieceType.KNIGHT, PlayerColor.BLACK));
        setupPiece("f11", new Piece(PieceType.BISHOP, PlayerColor.BLACK));
        setupPiece("f10", new Piece(PieceType.BISHOP, PlayerColor.BLACK));
        setupPiece("f9", new Piece(PieceType.BISHOP, PlayerColor.BLACK));
        setupPiece("e10", new Piece(PieceType.QUEEN, PlayerColor.BLACK));
        setupPiece("g10", new Piece(PieceType.KING, PlayerColor.BLACK));
    }

    public void setupInitalState(String boardState){
        currentTurn = boardState.split(" ")[1].equals("1") ? PlayerColor.WHITE : PlayerColor.BLACK;
        String[] rows = boardState.split(" ")[0].split("/");

        Pattern p = Pattern.compile("[PRNBQKprnbqk]|\\d+");
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];
            Matcher m = p.matcher(row);
            int pos = 0;

            while (m.find()) {
                String token = m.group();
                if (Character.isDigit(token.charAt(0))) {
                    int gap = Integer.parseInt(token);
                    pos += gap;
                } else {
                    //String piece = getPiece(token);
                    String rowName = String.valueOf((char) ('a' + pos+ROW_OFFSETS[i]));
                    String colName = String.valueOf(i+1);
                    String cell = rowName + colName;
                    Piece piece = switch(token){
                        case "P" -> new Piece(PieceType.PAWN, PlayerColor.WHITE);
                        case "p" -> new Piece(PieceType.PAWN, PlayerColor.BLACK);
                        case "R" -> new Piece(PieceType.ROOK, PlayerColor.WHITE);
                        case "r" -> new Piece(PieceType.ROOK, PlayerColor.BLACK);
                        case "N" -> new Piece(PieceType.KNIGHT, PlayerColor.WHITE);
                        case "n" -> new Piece(PieceType.KNIGHT, PlayerColor.BLACK);
                        case "B" -> new Piece(PieceType.BISHOP, PlayerColor.WHITE);
                        case "b" -> new Piece(PieceType.BISHOP, PlayerColor.BLACK);
                        case "Q" -> new Piece(PieceType.QUEEN, PlayerColor.WHITE);
                        case "q" -> new Piece(PieceType.QUEEN, PlayerColor.BLACK);
                        case "K" -> new Piece(PieceType.KING, PlayerColor.WHITE);
                        case "k" -> new Piece(PieceType.KING, PlayerColor.BLACK);
                        default -> new Piece(PieceType.PAWN, PlayerColor.WHITE);
                    };
                    setupPiece(cell, piece);
                    pos ++;
                }
            }
        }
    }

    private void setupPiece(String cellId, Piece piece) {
        board.placePiece(cellId, piece);
        listeners.forEach(listener -> listener.onPlaced(cellId, board.getCellById(cellId).getPiece()));
    }

    private void checkEndangered(){
        PlayerColor nextTurn = (currentTurn == PlayerColor.WHITE) ? PlayerColor.BLACK : PlayerColor.WHITE;

        List<Piece> attackers = (currentTurn == PlayerColor.WHITE) ? board.whitePieces() : board.blackPieces();
        List<Piece> defenders = (nextTurn == PlayerColor.WHITE) ? board.whitePieces() : board.blackPieces();

        Piece king = defenders.stream()
                .filter(p -> p.getType() == PieceType.KING)
                .findFirst().orElse(null);
        if (king == null) return;

        boolean endangered = attackers.stream()
                .flatMap(p -> getRawMoves(p).stream())
                .anyMatch(cell -> cell.getCoords().col == king.getPosition().col
                        && cell.getCoords().row == king.getPosition().row);
        listeners.forEach(l -> l.endangered(king, endangered));
    }

    public PlayerColor getActivePlayer(){
        return this.currentTurn;
    }

    public HexCoord getEnpassentCoordGhost(){
        return this.enpassentCoordGhost;
    }

    public HexCoord getEnpassentMovedTo(){
        return this.enpassentMovedTo;
    }

    public boolean isEnpassent() {
        return enpassent;
    }
}
