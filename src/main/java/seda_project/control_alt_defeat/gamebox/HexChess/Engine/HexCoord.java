package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

public class HexCoord {

    public final int row;
    public final int col;

    private static final int[] COL_SIZES = {6,7,8,9,10,11,10,9,8,7,6};

    public HexCoord(int col, int row){
        this.row = row;
        this.col = col;
    }

    public static HexCoord transformIdToHex(String id){
        char[] parts = id.toCharArray();
        int col = parts[0]- 'a';
        int row = Integer.parseInt(id.replaceAll("[abcdefghijk]",""));
        return new HexCoord(col,row);
    }

    public String transformHextoId(){
        return String.valueOf((char)(col +'a'))+row;
    }

    public boolean isValid(int col, int row){
        if (col < 0 || col > 10) {
            return false;
        }
        boolean rowvalid = (row >= 1 && row <= COL_SIZES[col]);
        return rowvalid;
    }

}
