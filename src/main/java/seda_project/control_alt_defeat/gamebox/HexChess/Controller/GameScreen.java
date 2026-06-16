package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import seda_project.control_alt_defeat.gamebox.Configuration;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.*;
import seda_project.control_alt_defeat.gamebox.HexChess.Network.ChessMessage;
import seda_project.control_alt_defeat.gamebox.network.Message;
import seda_project.control_alt_defeat.gamebox.network.NetworkLayer;
import seda_project.control_alt_defeat.gamebox.network.NetworkListener;
import seda_project.control_alt_defeat.gamebox.network.Session;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameScreen extends Controller implements Initializable, ChessEventListener {
    PieceSettings settings = PieceSettings.getInstance();
    private GameEngine gameEngine;
    private ImageView endangeredKingView = null;
    private PlayerColor activePlayer;
    private boolean isNetworkPromotion = false;
    
    @FXML
    private Parent root;

    @FXML
    private VBox header, p1, p2;


    @FXML
    private StackPane stackPane;

    @FXML
    private Pane boardPane;

    @FXML
    private ImageView p1PawnImg, p1RookImg, p1KnightImg, p1BishopImg, p1QueenImg, p1KingImg;
    @FXML
    private ImageView p2PawnImg, p2RookImg, p2KnightImg, p2BishopImg, p2QueenImg, p2KingImg;

    @FXML
    private Label p1Score, p2Score, p1NameLabel, p2NameLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        applyTileColor();
        applyPiecePreviewImages();
    }

    public void setNames(String p1Name, String p2Name){
        p1NameLabel.setText(p1Name);
        p2NameLabel.setText(p2Name);

    }

    public void setPoints (double p1Points, double p2Points){
        p1Score.setText(String.valueOf(p1Points));
        p2Score.setText(String.valueOf(p2Points));
    }

    public void init(){
        this.gameEngine.addListener(this);
        this.gameEngine.setupInitialState();
        activePlayer = gameEngine.getActivePlayer();
    }

    public void setGameEngine(GameEngine gameEngine){
        this.gameEngine = gameEngine;
    }

    public void init(String boardState) {
        this.gameEngine.addListener(this);
        this.gameEngine.setupInitalState(boardState);
        activePlayer = gameEngine.getActivePlayer();
    }

    @Override
    public void onPlaced(String id, Piece piece){
        String pieceId = piecetoString(piece);
        Polygon field = (Polygon) boardPane.lookup("#" + id);

        ImageView sourceView = (ImageView) boardPane.getScene().lookup("#" + pieceId);
        Image img = sourceView.getImage();

        ImageView newPiece = new ImageView(img);
        newPiece.setFitWidth(40);
        newPiece.setFitHeight(40);
        newPiece.setPreserveRatio(true);
        newPiece.setUserData(id);
        newPiece.setId(pieceId);

        newPiece.setLayoutX(field.getLayoutX() - 20);
        newPiece.setLayoutY(field.getLayoutY() - 20);
        newPiece.setMouseTransparent(true);
        boardPane.getChildren().add(newPiece);
    }

    private void applyTileColor() {
        StringBuilder style = new StringBuilder();
        if (settings.getDarkTiles() != null)
            style.append("-dark-poly-color: ").append(toCssColor(settings.getDarkTiles())).append("; ");
        if (settings.getNormalTiles() != null)
            style.append("-normal-poly-color: ").append(toCssColor(settings.getNormalTiles())).append("; ");
        if (settings.getLightTiles() != null)
            style.append("-light-poly-color: ").append(toCssColor(settings.getLightTiles())).append("; ");
        if (!style.isEmpty())
            root.setStyle(style.toString());
    }

    private void applyPiecePreviewImages() {
        List<ImageView> p1 = settings.getP1Pieces();
        if (p1 != null && p1.size() >= 6) {
            p1PawnImg.setImage(p1.get(0).getImage());
            p1RookImg.setImage(p1.get(1).getImage());
            p1KnightImg.setImage(p1.get(2).getImage());
            p1BishopImg.setImage(p1.get(3).getImage());
            p1QueenImg.setImage(p1.get(4).getImage());
            p1KingImg.setImage(p1.get(5).getImage());
        }
        List<ImageView> p2 = settings.getP2Pieces();
        if (p2 != null && p2.size() >= 6) {
            p2PawnImg.setImage(p2.get(0).getImage());
            p2RookImg.setImage(p2.get(1).getImage());
            p2KnightImg.setImage(p2.get(2).getImage());
            p2BishopImg.setImage(p2.get(3).getImage());
            p2QueenImg.setImage(p2.get(4).getImage());
            p2KingImg.setImage(p2.get(5).getImage());
        }
    }

    private static String toCssColor(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
    }
    @FXML
    public void handleTileClick(MouseEvent mouseEvent) {
        Session s = Session.current();
        clearHighlights();

        Polygon polygon = (Polygon) mouseEvent.getSource();
        String currentTileId = polygon.getId();

        ImageView pieceOnTile = getPieceAtPolygon(currentTileId);
        if (pieceOnTile != null) {
            PlayerColor playerColor = (pieceOnTile.getId().contains("1")) ? PlayerColor.WHITE : PlayerColor.BLACK;

            if (s.network != null) {
                PlayerColor myColor = s.isHost ? PlayerColor.WHITE : PlayerColor.BLACK;

                if (playerColor != myColor || activePlayer != myColor) {
                    return;
                }
            }

            pieceOnTile.getStyleClass().add("selectedTile");
            Piece p = gameEngine.getBoard().getCellById(currentTileId).getPiece();
            List<HexCell> legalMoves = gameEngine.getLegalMoves(p);

            for (HexCell cell :legalMoves ){;
                String targetTileId = cell.getCoords().transformHextoId();

                if(boardPane.lookup("#"+targetTileId)instanceof  Polygon targetPolygon){

                    ImageView pieceToCapture = getPieceAtPolygon(targetTileId);
                    if (pieceToCapture != null) {
                        pieceToCapture.getStyleClass().add("capture");
                    }
                    else {
                        if (gameEngine.isEnpassent() && targetTileId.equals(gameEngine.getEnpassentCoordGhost().transformHextoId())&& p.getType() == PieceType.PAWN){
                            enpassentCoord(targetTileId, gameEngine.getEnpassentMovedTo().transformHextoId());
                        }
                        Circle moveDot = new Circle(10);
                        moveDot.setLayoutX(targetPolygon.getLayoutX());
                        moveDot.setLayoutY(targetPolygon.getLayoutY());
                        if (s.network != null){
                            if (s.isHost){
                                if (playerColor == PlayerColor.WHITE) {
                                    moveDot.getStyleClass().add("selfDot");
                                }
                                else {
                                    moveDot.getStyleClass().add("enemyDot");
                                }
                            }
                            else{
                                if (playerColor == PlayerColor.BLACK){
                                    moveDot.getStyleClass().add("selfDot");
                                }
                                else {
                                    moveDot.getStyleClass().add("enemyDot");
                                }
                            }
                        }
                        else {
                            if (playerColor == activePlayer) {
                                moveDot.getStyleClass().add("selfDot");
                            } else {
                                moveDot.getStyleClass().add("enemyDot");
                            }
                        }
                        moveDot.setMouseTransparent(true);
                        boardPane.getChildren().add(moveDot);
                    }
                    targetPolygon.setOnMouseClicked(event -> {
                        clearMove();
                        if (s.network != null) {
                            if (s.isHost) {
                                boolean ok = gameEngine.handleMove(currentTileId, targetTileId);
                                if (ok) {
                                    s.network.send(new ChessMessage.StateUpdate(currentTileId, targetTileId, null));
                                }
                            } else {
                                s.network.send(new ChessMessage.Input(currentTileId, targetTileId));
                            }
                        }
                        else {
                            gameEngine.handleMove(currentTileId, targetTileId);
                        }
                        clearHighlights();
                        event.consume();
                        for (Node n : boardPane.getChildren()){
                            if (n instanceof Polygon poly){
                                poly.setOnMouseClicked(this::handleTileClick);
                            }
                        }
                    });
                }
            }
        }
    }

    private void clearMove() {
        for (Node node : boardPane.getChildren()) {
            if (node instanceof Polygon poly) {
                poly.getStyleClass().remove("movedFrom");
                poly.getStyleClass().remove("movedTo");
            }
        }
    }

    private void clearHighlights() {
        for (Node node : boardPane.getChildren()) {
            if ( node instanceof ImageView iv ){
                iv.getStyleClass().remove("capture");
                iv.getStyleClass().remove("selectedTile");
            }
            if (node instanceof Polygon poly) {
                poly.setOnMouseClicked(this::handleTileClick);
            }
        }

        boardPane.getChildren().removeIf(node -> node instanceof Path || node instanceof Circle);
    }

    @Override
    public void move(String fromId, String toId) {
        ImageView piece = null;
        Polygon old = null;
        Polygon poly= null;
        for (Node n : boardPane.getChildren()) {
            if (n.getId() != null) {
                if (n.getId().equals(fromId)) {
                    piece = getPieceAtPolygon(fromId);
                    old = (Polygon) n;
                }
                if (n.getId().equals(toId)) {
                    poly = (Polygon) n;
                }
            }
        }
        old.getStyleClass().add("movedFrom");
        poly.getStyleClass().add("movedTo");

        old.setUserData(null);
        piece.setUserData(toId);
        piece.setLayoutX(poly.getLayoutX() - 20);
        piece.setLayoutY(poly.getLayoutY() - 20);
        clearHighlights();
    }


    @Override
    public void capture(String fromId, String toId, Piece capturedPiece) {

        ImageView captured = getPieceAtPolygon(toId);
        boardPane.getChildren().remove(captured);
        move(fromId, toId);
        adaptLabels(capturedPiece);
    }

    private void adaptLabels(Piece capturedPiece) {
        String labelId = piecetoString(capturedPiece).replace("Img", "");

        VBox helper = (labelId.contains("1")) ? p1 : p2;
        Label capturedLabel = (Label) helper.getScene().lookup("#" + labelId);
        if (capturedLabel != null) {
            int current = Integer.parseInt(capturedLabel.getText().replace("x",""));
            String newValue = (labelId.contains("1")) ? "x"+(current + 1):(current + 1)+"x";
            capturedLabel.setText(newValue);
        }
    }

    @Override 
    public void enpassent(String enpassentId, Piece enpassentPiece){
        ImageView img = getPieceAtPolygon(enpassentId);
        boardPane.getChildren().remove(img);
        adaptLabels(enpassentPiece);
    }


    @Override
    public void gameEnd(PlayerColor player) {
        int winner = (player == PlayerColor.WHITE) ? 2 : 1;
        handleNetworkGameEnd("WIN", winner);
    }
    private void resign(PlayerColor playerColor) {
        int winner = (playerColor == PlayerColor.WHITE) ? 1 : 2;
        handleNetworkGameEnd("RESIGN",winner);
    }


    @Override
    public void remis() {
        handleNetworkGameEnd("REMIS", 0);
    }

    public void drawProposal() {
        handleNetworkGameEnd("PROPOSAL", 0);
    }

    @Override
    public void stalemate(PlayerColor currentTurn) {
        int winner = (currentTurn == PlayerColor.WHITE) ? 2 : 1;
        handleNetworkGameEnd("STALEMATE", winner);
    }

    private void handleNetworkGameEnd(String reason, int winner) {
        Session s = Session.current();

        if (s.network != null) {
            if (s.isHost) {
                s.network.send(new ChessMessage.GameEnded(reason, winner));
                showResultScreen(reason, winner);
            }
            else{
                s.network.send(new ChessMessage.GameEnded(reason, winner));
            }
        } else {
            showResultScreen(reason, winner);
        }
    }

    public void showResultScreen(String reason, int winner) {
        ResultScreen controller = (ResultScreen) c.changeScene("/Views/HexChess/ResultScreen.fxml", header, vS);
        controller.handData(p1NameLabel.getText(), p2NameLabel.getText(), p1Score.getText(), p2Score.getText());
        controller.initNetwork();

        switch (reason) {
            case "WIN" -> controller.winner(winner);
            case "STALEMATE" -> controller.stalemate(winner);
            case "REMIS" -> controller.remis();
            case "PROPOSAL" -> controller.draw();
            case "RESIGN" -> controller.resign(winner);
        }
    }

    @Override
    public void promotion(PlayerColor currentTurn) {
        Session s = Session.current();
        if  (s.network != null) {
            if (s.isHost && currentTurn == PlayerColor.WHITE) {
                prom(currentTurn);
            }
            else if(!s.isHost && currentTurn == PlayerColor.BLACK){
                prom(currentTurn);
            }
        }
        else {
            prom(currentTurn);
        }
    }

    public void prom(PlayerColor currentTurn){
        try {
            Stage promotionStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/PromotionStage.fxml"));
            Parent root = loader.load();
            promotionStage.setScene(new Scene(root));
            promotionStage.setTitle("Promotion Stage");
            promotionStage.initModality(Modality.APPLICATION_MODAL);
            promotionStage.show();

            Promotion controller = loader.getController();
            List<ImageView> views = (currentTurn == PlayerColor.WHITE) ? settings.getP1Pieces() : settings.getP2Pieces();
            controller.sendPieces(views, gameEngine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPromoted(String coordId, Piece piece){
        ImageView pawnView = getPieceAtPolygon(coordId);
        if (pawnView == null) return;
        String newPieceId = piecetoString(piece);
        ImageView sourceView = (ImageView) boardPane.getScene().lookup("#" + newPieceId);

        if (sourceView != null) {
            pawnView.setImage(sourceView.getImage());
        }

        pawnView.setId(newPieceId);
        Session s = Session.current();
        if (s.network != null && !isNetworkPromotion) {
            s.network.send(new ChessMessage.PromotionChoice(coordId, piece.getType()));
        }
    }

    @Override
    public void endangered(Piece king, boolean isEndangered) {
        if (endangeredKingView != null) {
            endangeredKingView.getStyleClass().remove("endangered");
            endangeredKingView = null;
        }

        if (!isEndangered) return;

        String kingId = king.getPosition().transformHextoId();
        ImageView kingView = getPieceAtPolygon(kingId);
        if (kingView == null) return;

        kingView.getStyleClass().add("endangered");
        endangeredKingView = kingView;
    }

    @Override
    public void activePlayer(PlayerColor currentTurn) {
        this.activePlayer = currentTurn;
    }

    public void enpassentCoord(String from, String to) {
        System.out.println("CALLED IN SCREEN");
        Polygon fromPoly = (Polygon) boardPane.lookup("#" + from);
        Polygon toPoly = (Polygon) boardPane.lookup("#" + to);

        double fromX = fromPoly.getLayoutX();
        double fromY = fromPoly.getLayoutY();

        double toX = toPoly.getLayoutX();
        double toY = toPoly.getLayoutY();

        double leftX = fromX-10;
        double rightX = fromX+10;

        double radius = 4;


        Path filledArrow = new Path();
        filledArrow.getStyleClass().add("enpassant-arrow");

        double endY = (fromY>toY)? toY+15 : toY-15;
        double controlY = fromY + (toY - fromY) * 0.5;

        filledArrow.getElements().addAll(
                new MoveTo(leftX, fromY),
                new ArcTo(radius, radius, 0, rightX, fromY, false, (fromY > toY)),
                new QuadCurveTo(rightX, controlY, toX, endY),
                new QuadCurveTo(leftX, controlY, leftX, fromY),
                new ClosePath()
        );

        boardPane.getChildren().add(filledArrow);

        ImageView img = getPieceAtPolygon(to);
        img.getStyleClass().add("capture");
    }


    @FXML
    protected void onExitAction(){
        Session s = Session.current();
        vS.emtyStack();
        c.changeScene("/Views/StartingScreen.fxml",header,vS);
    }

    private ImageView getPieceAtPolygon(String tileId) {
        for (Node node : boardPane.getChildren()) {
            if (node instanceof ImageView && tileId.equals(node.getUserData())) {
                return (ImageView) node;
            }
        }
        return null;
    }

    private String piecetoString(Piece piece){
        StringBuilder sb = new StringBuilder();
        switch(piece.getPlayer()){
            case BLACK -> sb.append("p2");
            case WHITE -> sb.append("p1");
        }
        switch(piece.getType()){
            case PAWN -> sb.append("PawnImg");
            case KING -> sb.append("KingImg");
            case ROOK -> sb.append("RookImg");
            case QUEEN -> sb.append("QueenImg");
            case BISHOP -> sb.append("BishopImg");
            case KNIGHT -> sb.append("KnightImg");
        }
        return sb.toString();
    }

    public void attachHostBridge(NetworkLayer network, GameEngine engine) {
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof ChessMessage.Input input) {
                    Platform.runLater(() -> {
                        boolean ok = engine.handleMove(input.fromId(), input.toId());
                        if (ok) {
                            // broadcast confirmed move back to client
                            network.send(new ChessMessage.StateUpdate(
                                    input.fromId(), input.toId(), null));
                        }
                    });
                }
                else if (msg instanceof ChessMessage.PromotionChoice choice) {
                    Platform.runLater(() -> {
                        isNetworkPromotion = true;
                        engine.promote(choice.pieceType());
                        isNetworkPromotion = false;
                    });
                }
                // --- NEW: Handle Client End Game / Resignation ---
                else if (msg instanceof ChessMessage.GameEnded endMsg) {
                    Platform.runLater(() -> {
                        network.send(endMsg); // Broadcast to client to finalize their UI
                        showResultScreen(endMsg.reason(), endMsg.winner());
                    });
                }
                // --- NEW: Handle Client offering a Draw ---
                else if (msg instanceof ChessMessage.DrawOffer offer) {
                    Platform.runLater(() -> drawProposal(offer.getProposing()));
                }
                // --- NEW: Handle Client declining a Draw ---
                else if (msg instanceof ChessMessage.DrawDeclined declinedMsg) {
                    Platform.runLater(() -> showDeclinedToast(declinedMsg.getProposing()));
                }
            }
        });
    }

    public void attachClientBridge(NetworkLayer network, GameEngine engine) {
        network.addListener(new NetworkListener() {
            @Override
            public void onMessage(Message msg) {
                if (msg instanceof ChessMessage.StateUpdate update) {
                    Platform.runLater(() -> {
                        engine.handleMove(update.fromId(), update.toId());
                    });
                } else if (msg instanceof ChessMessage.GameEnded endMsg) {
                    Platform.runLater(() -> {
                        showResultScreen(endMsg.reason(), endMsg.winner());
                    });
                } else if (msg instanceof ChessMessage.PromotionChoice choice) {
                    Platform.runLater(() -> {
                        isNetworkPromotion = true;
                        engine.promote(choice.pieceType());
                        isNetworkPromotion = false;
                    });
                }
                // --- NEW: Handle Host offering a Draw ---
                else if (msg instanceof ChessMessage.DrawOffer offer) {
                    Platform.runLater(() -> drawProposal(offer.getProposing()));
                }
                // --- NEW: Handle Host declining a Draw ---
                else if (msg instanceof ChessMessage.DrawDeclined declinedMsg) {
                    Platform.runLater(() -> showDeclinedToast(declinedMsg.getProposing()));
                }
            }
        });
    }

    public void drawProposal(int proposing){
        try {
            Stage proposalStage = new Stage();
            FXMLLoader loader = new FXMLLoader(Configuration.class.getResource("/Views/HexChess/DrawProposal.fxml"));
            Parent root = loader.load();
            proposalStage.setScene(new Scene(root));
            proposalStage.setTitle("Promotion Stage");
            proposalStage.initModality(Modality.APPLICATION_MODAL);
            proposalStage.show();

            DrawProposal controller = loader.getController();
            controller.sendGameScreen(this);
            controller.sendPlayer(proposing);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onP1DrawAction() {
        Session s = Session.current();
        if (s.network != null) {
            if (!s.isHost) return;
            s.network.send(new ChessMessage.DrawOffer(1));
        } else {
            drawProposal(1);
        }
    }

    @FXML
    protected void onP1ResignAction() {
        Session s = Session.current();
        if (s.network != null && !s.isHost) return;
        resign(PlayerColor.BLACK);
    }

    @FXML
    protected void onP2DrawAction() {
        Session s = Session.current();
        if (s.network != null) {
            if (s.isHost) return;
            s.network.send(new ChessMessage.DrawOffer(2));
        } else {
            drawProposal(2);
        }
    }

    @FXML
    protected void onP2ResignAction() {
        Session s = Session.current();
        if (s.network != null && s.isHost) return;
        resign(PlayerColor.WHITE);
    }


    public void declined(int proposing) {
        Session s = Session.current();
        if (s.network != null) {
            s.network.send(new ChessMessage.DrawDeclined(proposing));
        }
        showDeclinedToast(proposing);
    }

    private void showDeclinedToast(int proposing) {
        if (proposing == 1){
            Toast.makeText(stackPane, p2NameLabel.getText() + " has declined the Draw Proposal");
        }
        else{
            Toast.makeText(stackPane, p1NameLabel.getText() + " has declined the Draw Proposal");
        }
    }
}
