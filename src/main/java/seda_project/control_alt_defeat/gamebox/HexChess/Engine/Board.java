package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Board {

    final List<HexCoord> WHITE_PROMOTION = List.of(
            new HexCoord(0,6), new HexCoord(1,7), new HexCoord(2,8), new HexCoord(3,9),
            new HexCoord(4,10), new HexCoord(5,11), new HexCoord(6,10), new HexCoord(7,9),
            new HexCoord(8,8), new HexCoord(9,7), new HexCoord(10,6)
    );

    final List<HexCoord> BLACK_PROMOTION = List.of(
            new HexCoord(0,1), new HexCoord(1,1), new HexCoord(2,1), new HexCoord(3,1),
            new HexCoord(4,1), new HexCoord(5,1), new HexCoord(6,1), new HexCoord(7,1),
            new HexCoord(8,1), new HexCoord(9,1), new HexCoord(10,1)
    );

    final List<HexCoord> BLACK_PAWN_STARTING = List.of(
            new HexCoord(1,7), new HexCoord(2,7), new HexCoord(3,7), new HexCoord(4,7),
            new HexCoord(5,7), new HexCoord(6,7), new HexCoord(7,7), new HexCoord(8,7),
            new HexCoord(9,7)
    );

    final List<HexCoord> WHITE_PAWN_STARTING = List.of(
            new HexCoord(1,1), new HexCoord(2,2), new HexCoord(3,3), new HexCoord(4,4),
            new HexCoord(5,5), new HexCoord(6,4), new HexCoord(7,3), new HexCoord(8,2),
            new HexCoord(9,1)
    );


    private static final int[] ROW_SIZES = {11,11,11,11,11,11,9,7,5,3,1};
    private static final int[] ROW_OFFSETS = {0,0,0,0,0,0,1,2,3,4,5};

    private final List<List<HexCell>> cells;
    private List<Piece> white = new ArrayList<>();
    private List<Piece> black = new ArrayList<>();

    public Board(){
        cells = new ArrayList<>();
        for (int row = 1; row <= 11; row++) {
            List<HexCell> rowList = new ArrayList<>();
            int offset = ROW_OFFSETS[row - 1];
            for (int i = 0; i < ROW_SIZES[row - 1]; i++) {
                int col = offset + i;   // actual column letter (a=0 … k=10)
                rowList.add(new HexCell(new HexCoord(col, row)));
            }
            cells.add(rowList);
        }
    }

    public HexCell getCellById(String id){
        HexCoord cell = HexCoord.transformIdToHex(id);
        System.out.println(id);
        return getCellByCoord(cell.col, cell.row);
    }

    public HexCell getCellByCoord(int col, int row){
        int index = col - ROW_OFFSETS[row - 1];
        return cells.get(row - 1).get(index);
    }

    public void placePiece(String id, Piece piece){
        HexCell cell = getCellById(id);
        cell.setPiece(piece);
        piece.setPosition(cell.getCoords());
        if (piece.getPlayer() == PlayerColor.WHITE) {
            white.add(piece);
        } else {
            black.add(piece);
        }
    }

    public void removePiece(HexCoord coord){
        HexCell cell = getCellByCoord(coord.col, coord.row);
        Piece piece = cell.getPiece();
        if (piece.getPlayer() == PlayerColor.WHITE) {
            white.remove(piece);
        }
        else{
            black.remove(piece);
        }
        cell.setPiece(null);
    }

    public Piece movePiece(HexCoord from , HexCoord to){
        HexCell fromCell = getCellByCoord(from.col, from.row);
        HexCell toCell = getCellByCoord(to.col, to.row);

        Piece fromPiece = fromCell.getPiece();
        Piece toPiece = toCell.getPiece();

        toCell.setPiece(fromPiece);
        fromCell.setPiece(null);
        fromPiece.setPosition(toCell.getCoords());

        return toPiece;
    }

    public List<HexCell> getDirectOrthogonalNeighbors(HexCoord coord){
        List<HexCell> neighbors = new ArrayList<>();
        int col = coord.col;
        int row = coord.row;
        //Orthogonal over
        if (coord.isValid(col,row+1)){
            neighbors.add(getCellByCoord(col,row+1));
        }
        int leftRowOffset = (col <=5) ? 0 : 1;
        int rightRowOffset = (col >= 5) ? 0 : 1;

        //Orthonal over right
        if (coord.isValid(col + 1, row + rightRowOffset)){
            neighbors.add(getCellByCoord(col + 1, row + rightRowOffset));
        }
        //Orthogonal over left
        if (coord.isValid(col - 1, row + leftRowOffset)){
            neighbors.add(getCellByCoord(col - 1, row + leftRowOffset));
        }

        //Orthogonal under
        if (coord.isValid(col,row-1)){
            neighbors.add(getCellByCoord(col,row-1));
        }
        //Orthogonal under right
        if (coord.isValid(col + 1, row - 1 + rightRowOffset)){
            neighbors.add(getCellByCoord(col + 1, row - 1 + rightRowOffset));
        }
        //Orthogonal under left
        if (coord.isValid(col - 1, row - 1 + leftRowOffset)){
            neighbors.add(getCellByCoord(col - 1, row - 1 + leftRowOffset));
        }

        return neighbors;
    }

    public List<HexCell> getallOrthogonals(HexCoord coord){
        List<HexCell> neighbors = new ArrayList<>();

        //Column Offset in Order:Up, Down, Upper-Right, Lower-Right, Upper-Left, Lower-Left
        int[] colDirs = {0,  0, 1, 1, -1, -1};

        for (int dir = 0; dir < 6; dir++) {
            int currentCol = coord.col;
            int currentRow = coord.row;
            int dCol = colDirs[dir];

            while (true) {
                int nextCol = currentCol + dCol;
                int nextRow = currentRow;

                // Straight Up
                if (dir == 0) {
                    nextRow = currentRow + 1;
                }
                // Straight Down
                else if (dir == 1) {
                    nextRow = currentRow - 1;
                }
                // Upper Right
                else if (dir == 2) {
                    nextRow = currentRow + ((currentCol >= 5) ? 0 : 1);
                }
                // Lower Right
                else if (dir == 3) {
                    nextRow = currentRow - 1 + ((currentCol >= 5) ? 0 : 1);
                }
                // Upper Left
                else if (dir == 4) {
                    nextRow = currentRow + ((currentCol <= 5) ? 0 : 1);
                }
                // Lower Left
                else if (dir == 5) {
                    nextRow = currentRow - 1 + ((currentCol <= 5) ? 0 : 1);
                }

                if (!coord.isValid(nextCol, nextRow)) {
                    break;
                }

                HexCell cell = getCellByCoord(nextCol, nextRow);
                neighbors.add(cell);

                if (cell.hasPiece()) {
                    break;
                }

                currentCol = nextCol;
                currentRow = nextRow;
            }
        }
        return neighbors;
    }

    public List<HexCell> getDirectDiagonalNeighbors(HexCoord coord){
        List<HexCell> neighbors = new ArrayList<>();
        int col = coord.col;
        int row = coord.row;

        //Upper Left Diagonal
        int adaptedRow = row + ((col <= 5) ? 1 : 2);
        if (coord.isValid(col - 1, adaptedRow)){
            neighbors.add(getCellByCoord(col - 1, adaptedRow));
        }

        //Upper Right Diagonal
        adaptedRow = row + ((col >= 5) ? 1 : 2);
        if (coord.isValid(col + 1, adaptedRow)){
            neighbors.add(getCellByCoord(col + 1, adaptedRow));
        }

        //Right Diagonal
        adaptedRow = row - 1 + ((col < 4) ? 2 : (col == 4) ? 1 : 0);
        if (coord.isValid(col + 2, adaptedRow)){
            neighbors.add(getCellByCoord(col + 2, adaptedRow));
        }

        //Left Diagonal
        adaptedRow = row - 1 + ((col > 6) ? 2 : (col == 6) ? 1 : 0);
        if (coord.isValid(col - 2, adaptedRow)){
            neighbors.add(getCellByCoord(col - 2, adaptedRow));
        }

        //Lower Left Diagonal
        adaptedRow = row - ((col <= 5) ? 2 : 1);
        if  (coord.isValid(col - 1, adaptedRow)){
            neighbors.add(getCellByCoord(col - 1, adaptedRow));
        }

        //Lower Right Diagonal
        adaptedRow = row - ((col >= 5) ? 2 : 1);
        if (coord.isValid(col + 1, adaptedRow)){
            neighbors.add(getCellByCoord(col + 1, adaptedRow));
        }

        return neighbors;
    }

    public List<HexCell> getAllDiagonals(HexCoord coord) {
        List<HexCell> neighbors = new ArrayList<>();

        // The 6 diagonal directions matching getDirectDiagonalNeighbors:
        // 0: Upper Left, 1: Upper Right, 2: Right, 3: Left, 4: Lower Left, 5: Lower Right
        int[] colDirs = {-1, 1, 2, -2, -1, 1};

        for (int dir = 0; dir < 6; dir++) {
            int currentCol = coord.col;
            int currentRow = coord.row;
            int dCol = colDirs[dir];

            while (true) {
                int nextCol = currentCol + dCol;
                int nextRow = currentRow;

                // 0. Upper Left Diagonal
                if (dir == 0) {
                    nextRow = currentRow + ((currentCol <= 5) ? 1 : 2);
                }
                // 1. Upper Right Diagonal
                else if (dir == 1) {
                    nextRow = currentRow + ((currentCol >= 5) ? 1 : 2);
                }
                // 2. Right Diagonal
                else if (dir == 2) {
                    nextRow = currentRow - 1 + ((currentCol < 4) ? 2 : (currentCol == 4) ? 1 : 0);
                }
                // 3. Left Diagonal
                else if (dir == 3) {
                    nextRow = currentRow - 1 + ((currentCol > 6) ? 2 : (currentCol == 6) ? 1 : 0);
                }
                // 4. Lower Left Diagonal
                else if (dir == 4) {
                    nextRow = currentRow - ((currentCol <= 5) ? 2 : 1);
                }
                // 5. Lower Right Diagonal
                else if (dir == 5) {
                    nextRow = currentRow - ((currentCol >= 5) ? 2 : 1);
                }

                // If we step out of the hexagonal boundaries, stop this ray
                if (!coord.isValid(nextCol, nextRow)) {
                    break;
                }

                HexCell cell = getCellByCoord(nextCol, nextRow);
                neighbors.add(cell);

                // Hit a piece? Stop sliding further in this direction
                if (cell.hasPiece()) {
                    break;
                }

                // Update tracking position to continue sliding along the ray
                currentCol = nextCol;
                currentRow = nextRow;
            }
        }
        return neighbors;
    }

    public List<HexCell> getJumpNeighbours(HexCoord coord) {
        List<HexCell> neighbors = new ArrayList<>();
        int col = coord.col;
        int row = coord.row;

        int[] dCol = {0, 0, 1, 1, -1, -1};

        // Correct 60° turn pairs for each direction.
        // Clockwise order of directions: 0(Up), 2(UpperRight), 3(LowerRight), 1(Down), 5(LowerLeft), 4(UpperLeft)
        // For each dir, the two valid knight-turn targets are its immediate CW neighbors in that ring.
        int[][] turnDirs = {
                {2, 4}, // dir 0 (Up)          → UpperRight, UpperLeft
                {3, 5}, // dir 1 (Down)        → LowerRight, LowerLeft
                {0, 3}, // dir 2 (UpperRight)  → Up, LowerRight
                {2, 1}, // dir 3 (LowerRight)  → UpperRight, Down
                {5, 0}, // dir 4 (UpperLeft)   → LowerLeft, Up
                {1, 4}, // dir 5 (LowerLeft)   → Down, UpperLeft
        };

        for (int dir = 0; dir < 6; dir++) {

            // Step 1: first orthogonal step
            int col1 = col + dCol[dir];
            int row1 = getOrthogonalRowOffset(col, row, dir);
            if (!coord.isValid(col1, row1)) continue;

            // Step 2: second orthogonal step (from new position context)
            int col2 = col1 + dCol[dir];
            int row2 = getOrthogonalRowOffset(col1, row1, dir);
            if (!coord.isValid(col2, row2)) continue;

            // Step 3: one 60° turn to either side
            for (int turnDir : turnDirs[dir]) {
                int finalCol = col2 + dCol[turnDir];
                int finalRow = getOrthogonalRowOffset(col2, row2, turnDir);
                if (coord.isValid(finalCol, finalRow)) {
                    neighbors.add(getCellByCoord(finalCol, finalRow));
                }
            }
        }
        return neighbors;
    }

    // Helper method to accurately track row alterations as cells shift context
    private int getOrthogonalRowOffset(int currentC, int currentR, int dir) {
        switch (dir) {
            case 0: return currentR + 1;
            case 1: return currentR - 1;
            case 2: return currentR + ((currentC >= 5) ? 0 : 1);
            case 3: return currentR - 1 + ((currentC >= 5) ? 0 : 1);
            case 4: return currentR + ((currentC <= 5) ? 0 : 1);
            case 5: return currentR - 1 + ((currentC <= 5) ? 0 : 1);
            default: return currentR;
        }
    }

    public List<Piece> whitePieces(){
        return white;
    }

    public List<Piece> blackPieces(){
        return black;
    }

    public String createNotation(PlayerColor startingPlayer) {
        StringBuilder sb = new StringBuilder();
        sb.append(cells.stream()
                .map(this::encodeRow)
                .collect(Collectors.joining("/")));
        sb.append(" ");
        sb.append(startingPlayer == PlayerColor.WHITE ? "w" : "b");
        return sb.toString();
    }

    private String encodeRow(List<HexCell> row) {
        int emptyCells = 0;
        StringBuilder sb = new StringBuilder();
        for (HexCell cell : row) {
            if (cell.hasPiece()) {
                if (emptyCells > 0) {
                    sb.append(emptyCells);
                    emptyCells = 0;
                }
                Piece piece = cell.getPiece();
                String symbol = switch (piece.getType()) {
                    case PAWN   -> "p";
                    case ROOK   -> "r";
                    case KNIGHT -> "n";
                    case BISHOP -> "b";
                    case QUEEN  -> "q";
                    case KING   -> "k";
                };
                // White pieces are uppercase, black pieces are lowercase (standard FEN convention)
                if (piece.getPlayer() == PlayerColor.WHITE) {
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

    public List<List<HexCell>> getCells(){
        return cells;
    }
}
