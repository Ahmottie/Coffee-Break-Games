package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.*;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardDesigner extends Controller implements Initializable {

    private final BoardDesignerEngine engine = new BoardDesignerEngine();
    private final PieceSettings settings = PieceSettings.getInstance();
    private boolean initial = false;
    private List<List<Polygon>> rows;
    private Map<String, Label> pieceLabels;
    private JsonHandler jsonHandler = new JsonHandler();
    private List<BoardDesignState> listofBoards;
    private String p1Name, p2Name;

    @FXML
    private VBox header;

    @FXML
    private StackPane stackPane;

    @FXML
    private ImageView p1PawnImg, p1RookImg, p1KnightImg, p1BishopImg, p1QueenImg, p1KingImg;
    @FXML
    private ImageView p2PawnImg, p2RookImg, p2KnightImg, p2BishopImg, p2QueenImg, p2KingImg;

    @FXML
    private Label p1PawnAmountLabel, p1RookAmountLabel, p1KnightAmountLabel, p1BishopAmountLabel, p1QueenAmountLabel, p1KingAmountLabel;
    @FXML
    private Label p2PawnAmountLabel, p2RookAmountLabel, p2KnightAmountLabel, p2BishopAmountLabel, p2QueenAmountLabel, p2KingAmountLabel;

    @FXML
    private Pane boardPane;

    @FXML
    private Button p1GoFirst, p2GoFirst, useButton;

    @FXML
    private Polygon a6, a5, a4, a3, a2, a1,
            b7, b6, b5, b4, b3, b2, b1,
            c8, c7, c6, c5, c4, c3, c2, c1,
            d9, d8, d7, d6, d5, d4, d3, d2, d1,
            e10, e9, e8, e7, e6, e5, e4, e3, e2, e1,
            f11, f10, f9, f8, f7, f6, f5, f4, f3, f2, f1,
            g10, g9, g8, g7, g6, g5, g4, g3, g2, g1,
            h9, h8, h7, h6, h5, h4, h3, h2, h1,
            i8, i7, i6, i5, i4, i3, i2, i1,
            j7, j6, j5, j4, j3, j2, j1,
            k6, k5, k4, k3, k2, k1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        buildPieceLabelsMap();
        loadImages();
        setCustomStyle();
        listofBoards = jsonHandler.readBoardStates();
        rows = List.of(
                List.of(a1, b1, c1, d1, e1, f1, g1, h1, i1, j1, k1),
                List.of(a2, b2, c2, d2, e2, f2, g2, h2, i2, j2, k2),
                List.of(a3, b3, c3, d3, e3, f3, g3, h3, i3, j3, k3),
                List.of(a4, b4, c4, d4, e4, f4, g4, h4, i4, j4, k4),
                List.of(a5, b5, c5, d5, e5, f5, g5, h5, i5, j5, k5),
                List.of(a6, b6, c6, d6, e6, f6, g6, h6, i6, j6, k6),
                List.of(b7, c7, d7, e7, f7, g7, h7, i7, j7),
                List.of(c8, d8, e8, f8, g8, h8, i8),
                List.of(d9, e9, f9, g9, h9),
                List.of(e10, f10, g10),
                List.of(f11)
        );
    }

    @FXML
    public void dragStart(MouseEvent mouseEvent) {
        initial = true;
        ImageView source = (ImageView) mouseEvent.getSource();
        String id = source.getId();
        var parent = source.getParent();

        // Do not allow placing a piece if its count is 0
        if (engine.getPieceAmounts().get(id) == 0 && parent instanceof HBox) {
            String pieceHelper = id.replace("Img", "").replaceFirst("p[12]", "");
            if (pieceHelper.equals("Pawn") || pieceHelper.equals("King")) {
                mouseEvent.consume();
                return;
            } else {
                String pawnKey = id.contains("1") ? "p1PawnImg" : "p2PawnImg";
                if (engine.getPieceAmounts().get(pawnKey) == 0) {
                    mouseEvent.consume();
                    return;
                }
            }
        }

        if (!(parent instanceof HBox)) {
            initial = false;
        }
        Image img = source.getImage();
        Dragboard db = source.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(id);
        content.putImage(img);
        db.setContent(content);
        mouseEvent.consume();
    }

    @FXML
    private void dragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
            Polygon target = (Polygon) event.getSource();
            target.setStroke(Color.RED);
            target.setStrokeWidth(2);
        }
        event.consume();
    }

    @FXML
    private void dragDrop(DragEvent event) {
        Object target = event.getSource();
        if (target instanceof Polygon pt) {
            String piece = event.getDragboard().getString();
            String fieldId = pt.getId();
            handlePiecePlaced(piece, fieldId);
            event.setDropCompleted(true);
            initial = false;
        } else {
            String piece = event.getDragboard().getString();
            handlePiecePlaced(piece, "");
            applyLabelValues(engine.computeIncrement(piece));
            event.setDropCompleted(true);
            initial = false;
        }
        event.consume();
    }

    @FXML
    private void dragExited(DragEvent event) {
        Polygon target = (Polygon) event.getSource();
        target.setStroke(Color.BLACK);
        target.setStrokeWidth(1);
        target.setStrokeType(StrokeType.INSIDE);
    }

    @FXML
    private void dragDone(DragEvent event) {
        if (event.getTransferMode() == null) {
            if (!initial) {
                String pieceId = ((ImageView) event.getSource()).getId();
                applyLabelValues(engine.computeIncrement(pieceId));
            }
        }
        event.consume();
    }

    @FXML
    protected void onBackAction(){
        sC.play("button");
        Object controller = c.backScene(header,vS);
        if (controller instanceof LocalGameConfiguration lGC){
            lGC.boardSelection(null, p1Name, p2Name);
        }
        if (controller instanceof HostLanConfiguration hLC){
            hLC.boardSelection(null, p1Name);
        }
    }

    @FXML
    protected void onExportAction(){
        sC.play("button");
        boolean exist = false;
        for (BoardDesignState state :listofBoards){
            if(state.getFENState().contains(engine.createNotation(rows))){
                exist = true;
            }
        }
        if (!exist) {
            if (validateBoardState()){
                listofBoards.add(jsonHandler.createNewState(engine.createNotation(rows), engine.getPieceAmounts(),engine.getActivePlayer()));
                jsonHandler.writeBoardStates(listofBoards);
                Toast.makeText(stackPane,"State was added!");
            }
            else {
                sC.play("error");
                Toast.makeText(stackPane,"You can only add valid States");
            }
        }
        else {
            sC.play("error");
            Toast.makeText(stackPane,"This State does already exist");
        }
    }

    @FXML
    protected void onImportAction(){
        sC.play("button");
        try {
            Stage selectionStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/BoardSelection.fxml"));
            Parent root = loader.load();
            selectionStage.setScene(new Scene(root));
            selectionStage.setTitle("Select Board State");
            selectionStage.initModality(Modality.APPLICATION_MODAL);
            selectionStage.show();

            BoardSelection controller = loader.getController();
            controller.passBoards(this,listofBoards);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onP1FirstAcion(){
        sC.play("button");
        p1GoFirst.getStyleClass().add("ready");
        p2GoFirst.getStyleClass().remove("ready");
        engine.activePlayer(1);
    }

    @FXML
    protected void onP2FirstAction(){
        sC.play("button");
        p2GoFirst.getStyleClass().add("ready");
        p1GoFirst.getStyleClass().remove("ready");
        engine.activePlayer(2);
    }

    @FXML
    protected void onUseAction(){
        sC.play("button");
        if (validateBoardState()) {
            Object controller = c.backScene(header,vS);
            if (controller instanceof LocalGameConfiguration lGC){
                lGC.boardSelection(engine.createNotation(rows), p1Name, p2Name);
            }
            if (controller instanceof HostLanConfiguration hLC) {
                hLC.boardSelection(engine.createNotation(rows), p1Name);
            }
        }
        else {
            sC.play("error");
            Toast.makeText(stackPane,"You can only use valid States");
        }
    }

    public void disableUse() {
        useButton.setDisable(true);
    }

    private void handlePiecePlaced(String pieceId, String fieldId) {
        Polygon field = (Polygon) boardPane.lookup("#" + fieldId);
        field.setUserData(pieceId);

        // Remove any existing piece on this field
        ImageView existingPiece = null;
        for (Node node : boardPane.getChildren()) {
            if (node instanceof ImageView && fieldId.equals(node.getUserData())) {
                existingPiece = (ImageView) node;
                break;
            }
        }

        boolean same = false;
        if (existingPiece != null) {
            String removedPieceId = existingPiece.getId();
            if (pieceId.equals(removedPieceId)) {
                same = true;
            }
            applyLabelValues(engine.computeIncrement(removedPieceId));
            boardPane.getChildren().remove(existingPiece);
        }

        if (!same && initial) {
            applyLabelValues(engine.computeAdapt(pieceId));
        }

        // Build and place the new piece ImageView
        ImageView sourceView = (ImageView) boardPane.getScene().lookup("#" + pieceId);
        Image img = sourceView.getImage();

        ImageView piece = new ImageView(img);
        piece.setFitWidth(40);
        piece.setFitHeight(40);
        piece.setPreserveRatio(true);
        piece.setUserData(fieldId);
        piece.setId(pieceId);
        piece.onDragDetectedProperty().set(event -> {
            field.setUserData(null);
            dragStart(event);
            boardPane.getChildren().remove(piece);
        });
        piece.setOnDragDone(this::dragDone);

        piece.setLayoutX(field.getLayoutX() - 20);
        piece.setLayoutY(field.getLayoutY() - 20);

        boardPane.getChildren().add(piece);
    }

    private void applyLabelValues(Map<String, String> values) {
        values.forEach((key, text) -> {
            Label label = pieceLabels.get(key);
            if (label != null) {
                label.setText(text);
            }
        });
    }

    private void buildPieceLabelsMap() {
        pieceLabels = new HashMap<>(Map.ofEntries(
                Map.entry("p1PawnImg",   p1PawnAmountLabel),
                Map.entry("p1RookImg",   p1RookAmountLabel),
                Map.entry("p1KnightImg", p1KnightAmountLabel),
                Map.entry("p1BishopImg", p1BishopAmountLabel),
                Map.entry("p1QueenImg",  p1QueenAmountLabel),
                Map.entry("p1KingImg",   p1KingAmountLabel),
                Map.entry("p2PawnImg",   p2PawnAmountLabel),
                Map.entry("p2RookImg",   p2RookAmountLabel),
                Map.entry("p2KnightImg", p2KnightAmountLabel),
                Map.entry("p2BishopImg", p2BishopAmountLabel),
                Map.entry("p2QueenImg",  p2QueenAmountLabel),
                Map.entry("p2KingImg",   p2KingAmountLabel)
        ));
    }

    private void loadImages() {
        List<ImageView> p1Pieces = settings.getP1Pieces();
        if (p1Pieces != null) {
            p1PawnImg.setImage(p1Pieces.get(0).getImage());
            p1RookImg.setImage(p1Pieces.get(1).getImage());
            p1KnightImg.setImage(p1Pieces.get(2).getImage());
            p1BishopImg.setImage(p1Pieces.get(3).getImage());
            p1QueenImg.setImage(p1Pieces.get(4).getImage());
            p1KingImg.setImage(p1Pieces.get(5).getImage());
        }
        List<ImageView> p2Pieces = settings.getP2Pieces();
        if (p2Pieces != null) {
            p2PawnImg.setImage(p2Pieces.get(0).getImage());
            p2RookImg.setImage(p2Pieces.get(1).getImage());
            p2KnightImg.setImage(p2Pieces.get(2).getImage());
            p2BishopImg.setImage(p2Pieces.get(3).getImage());
            p2QueenImg.setImage(p2Pieces.get(4).getImage());
            p2KingImg.setImage(p2Pieces.get(5).getImage());
        }
    }

    private void setCustomStyle() {
        StringBuilder style = new StringBuilder();
        if (settings.getDarkTiles() != null) {
            style.append("-dark-poly-color: ").append(toCssColor(settings.getDarkTiles())).append(";");
            root.setStyle(style.toString());
        }
        if (settings.getNormalTiles() != null) {
            style.append("-normal-poly-color: ").append(toCssColor(settings.getNormalTiles())).append(";");
            root.setStyle(style.toString());
        }
        if (settings.getLightTiles() != null) {
            style.append("-light-poly-color: ").append(toCssColor(settings.getLightTiles())).append(";");
            root.setStyle(style.toString());
        }
    }

    private static String toCssColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) (color.getRed()   * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue()  * 255)
        );
    }

    public void loadBoard(BoardDesignState state){
        boardPane.getChildren().removeIf(node -> node instanceof ImageView);
        for (List<Polygon> row : rows) {
            for (Polygon polygon : row) {
                polygon.setUserData(null);
            }
        }
        engine.loadExport(state);
        applyLabelValues(engine.buildLabelValues("p1"));
        applyLabelValues(engine.buildLabelValues("p2"));
        drawFen(state.getFENState().replace("\"",""));
    }

    private void drawFen(String fenState) {
        String[] starting = fenState.split(" ");
        if (Integer.parseInt(starting[1]) == 1) {
            onP1FirstAcion();
        }
        else {
            onP2FirstAction();
        }
        String[] fenrows = starting[0].split("/");
        Pattern p = Pattern.compile("[PRNBQKprnbqk]|\\d+");
        for (int i = 0; i < fenrows.length; i++) {
            String row = fenrows[i];
            Matcher m = p.matcher(row);
            int pos = 0;

            while (m.find()) {
                String token = m.group();
                if (Character.isDigit(token.charAt(0))) {
                    int gap = Integer.parseInt(token);
                    pos += gap;
                } else {
                    String piece = getPiece(token);
                    handlePiecePlaced(piece,rows.get(i).get(pos).getId());
                    pos ++;
                }
            }
        }
    }

    private String getPiece(String s) {
        return switch (s) {
            case "P" -> "p1PawnImg";
            case "p" -> "p2PawnImg";
            case "R" -> "p1RookImg";
            case "r" -> "p2RookImg";
            case "N" -> "p1KnightImg";
            case "n" -> "p2KnightImg";
            case "B" -> "p1BishopImg";
            case "b" -> "p2BishopImg";
            case "Q" -> "p1QueenImg";
            case "q" -> "p2QueenImg";
            case "K" -> "p1KingImg";
            case "k" -> "p2KingImg";
            default -> "?";
        };
    }

    public void handNames(String text, String text1) {
        p1Name = text;
        p2Name = text1;
    }

    private boolean validateBoardState() {
        int activePlayer = p1GoFirst.getStyleClass().contains("ready") ? 1 : 2;
        String fenNotation = engine.createNotation(rows) + " " + activePlayer;
        System.out.println(fenNotation);
        GameEngine validator = new GameEngine();
        validator.setupInitalState(fenNotation);

        long whiteKings = validator.getBoard().whitePieces().stream()
                .filter(p -> p.getType() == PieceType.KING).count();
        long blackKings = validator.getBoard().blackPieces().stream()
                .filter(p -> p.getType() == PieceType.KING).count();

        if (whiteKings != 1 || blackKings != 1) {
            return false;
        }

        boolean invalidWhitePawn = validator.getBoard().whitePieces().stream()
                .filter(p -> p.getType() == PieceType.PAWN)
                .anyMatch(p -> {
                    HexCoord pos = p.getPosition();
                    return validator.getBoard().getWhitePromotions().stream()
                            .anyMatch(c -> c.col == pos.col && c.row == pos.row);
                });

        boolean invalidBlackPawn = validator.getBoard().blackPieces().stream()
                .filter(p -> p.getType() == PieceType.PAWN)
                .anyMatch(p -> {
                    HexCoord pos = p.getPosition();
                    return validator.getBoard().getBlackPromotions().stream()
                            .anyMatch(c -> c.col == pos.col && c.row == pos.row);
                });

        if (invalidWhitePawn || invalidBlackPawn) {
            return false;
        }

        if (validator.isKingInCheck(PlayerColor.BLACK) || validator.isKingInCheck(PlayerColor.WHITE)) return false;

        return !validator.checkMaterial();
    }
}