package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import seda_project.control_alt_defeat.gamebox.HexChess.Controller.BoardDesigner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonHandler {
    public JsonHandler(){}
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private final File externalSaveFile = new File("BoardDesign.json");

    public List<BoardDesignState> readBoardStates(){
        List<BoardDesignState> listofBoards = new ArrayList<>();
        try {
            String path = "/HexChess/BoardDesign.json";
            JsonNode nodes = null;

            if (externalSaveFile.exists()) {
                nodes = jsonMapper.readTree(externalSaveFile);
            }
            else {
                try (InputStream is = BoardDesigner.class.getResourceAsStream(path)) {
                    if (is != null){
                        nodes = jsonMapper.readTree(is);
                    }
                }
            }

            if (nodes == null || nodes.isMissingNode()) {
                nodes = jsonMapper.createArrayNode();
            }
            for (JsonNode x : nodes){
                listofBoards.add(new BoardDesignState(x));
            }
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
        return listofBoards;
    }

    public BoardDesignState createNewState(String notation, Map<String, Integer> pieceAmounts, int starting){
        ObjectNode newBoard = jsonMapper.createObjectNode();
        newBoard.put("FENState", notation);
        newBoard.put("startingPlayer",starting);
        newBoard.put("p1Pawn", pieceAmounts.get("p1PawnImg"));
        newBoard.put("p1Rook", pieceAmounts.get("p1RookImg"));
        newBoard.put("p1Knight", pieceAmounts.get("p1KnightImg"));
        newBoard.put("p1Bishop", pieceAmounts.get("p1BishopImg"));
        newBoard.put("p1Queen", pieceAmounts.get("p1QueenImg"));
        newBoard.put("p1King", pieceAmounts.get("p1KingImg"));
        newBoard.put("p2Pawn", pieceAmounts.get("p2PawnImg"));
        newBoard.put("p2Rook", pieceAmounts.get("p2RookImg"));
        newBoard.put("p2Knight", pieceAmounts.get("p2KnightImg"));
        newBoard.put("p2Bishop", pieceAmounts.get("p2BishopImg"));
        newBoard.put("p2Queen", pieceAmounts.get("p2QueenImg"));
        newBoard.put("p2King", pieceAmounts.get("p2KingImg"));
        return new BoardDesignState(newBoard);
    }

    public void writeBoardStates(List<BoardDesignState> listofBoards) {
        ArrayNode arrayNode = jsonMapper.createArrayNode();
        listofBoards.forEach(board -> arrayNode.add(board.getBoard()));

        try {
            File file;
            var path = BoardDesigner.class.getResource("/HexChess/BoardDesign.json");
            if (path != null) {
                file = new File(path.toURI());
            } else {
                file = new File("src/main/resources/HexChess/BoardDesign.json");
            }
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
