package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import seda_project.control_alt_defeat.gamebox.HexChess.Engine.PieceSettings;
import seda_project.control_alt_defeat.gamebox.ui.Controller;
import seda_project.control_alt_defeat.gamebox.ui.Toast;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ChessSettings extends Controller implements Initializable {

    PieceSettings settings = PieceSettings.getInstance();

    @FXML
    protected VBox header;

    @FXML
    protected StackPane stackPane;

    @FXML
    private ImageView p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King;
    @FXML
    private ImageView p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King;

    @FXML
    private ColorPicker p1ColorPicker, p2ColorPicker, darkTilesColorPicker, normalTilesColorPicker, lightTilesColorPicker;

    @FXML
    private Polygon darkPolygon, normalPolygon, lightPolygon;

    private Color p1Color = Color.WHITE;
    private Color p2Color = Color.BLACK;
    private Color darkTileColor = Color.web("#5C4033");
    private Color normalTileColor = Color.web("#8B5A2B");
    private Color lightTileColor = Color.web("#C19A6B");

    private final Color p1DefaultColor = Color.WHITE;
    private final Color p2DefaultColor = Color.BLACK;
    private final Color darkTileDefaultColor = Color.web("#5C4033");
    private final Color normalTileDefaultColor = Color.web("#8B5A2B");
    private final Color lightTileDefaultColor = Color.web("#C19A6B");


    private List<ImageView> p1Pieces;
    private List<ImageView> p2Pieces;
    private List<Image> p1OriginalImages;
    private List<Image> p2OriginalImages;

    @FXML
    protected void onBackAction() {
        sC.play("button");
        c.backScene(header,vS);
    }

    @FXML
    protected void onSaveAction() {
        sC.play("button");
        settings.setP1Color(p1Color);
        settings.setP2Color(p2Color);
        settings.setP1Pieces(p1Pieces);
        settings.setP2Pieces(p2Pieces);
        settings.setDarkTiles(darkTileColor);
        settings.setNormalTiles(normalTileColor);
        settings.setLightTiles(lightTileColor);

        String toastMsg = "Saved Settings";
        Toast.makeText(stackPane, toastMsg);
    }

    @FXML
    protected void onDefaultAction() {
        sC.play("button");
        for (int i = 0; i < p1Pieces.size(); i++) {
            p1Pieces.get(i).setImage(p1OriginalImages.get(i));
        }
        for (int i = 0; i < p2Pieces.size(); i++) {
            p2Pieces.get(i).setImage(p2OriginalImages.get(i));
        }
        p1Color = p1DefaultColor;
        p2Color = p2DefaultColor;

        p1ColorPicker.setValue(p1Color);
        p2ColorPicker.setValue(p2Color);

        darkTileColor = darkTileDefaultColor;
        normalTileColor = normalTileDefaultColor;
        lightTileColor = lightTileDefaultColor;

        darkTilesColorPicker.setValue(darkTileColor);
        normalTilesColorPicker.setValue(normalTileColor);
        lightTilesColorPicker.setValue(lightTileColor);

        darkPolygon.setFill(darkTileColor);
        normalPolygon.setFill(normalTileColor);
        lightPolygon.setFill(lightTileColor);
    }

    @FXML
    protected void onP1ColorAction(){
        Color c = p1ColorPicker.getValue();
        redraw(c, p1Pieces,p1OriginalImages, p1DefaultColor);
        p1Color = c;
    }

    @FXML
    protected void onP2ColorAction(){
        Color c = p2ColorPicker.getValue();
        redraw(c, p2Pieces, p2OriginalImages,p2DefaultColor);
        p2Color = c;
    }
    private void redraw(Color c, List<ImageView> pieces,List<Image> originals, Color oldColor) {
        for (int i = 0; i < pieces.size(); i++) {
            Image original = originals.get(i);
            Image helper = pieces.get(i).getImage();
            WritableImage writable = new WritableImage(helper.getPixelReader(), (int) helper.getWidth(), (int) helper.getHeight());
            PixelWriter writer = writable.getPixelWriter();
            PixelReader reader = original.getPixelReader();
            for (int x = 0; x < helper.getWidth(); x++) {
                for (int y = 0; y < helper.getHeight(); y++) {
                    Color color = reader.getColor(x, y);
                    if (color.equals(oldColor)) {
                        writer.setColor(x, y, c);
                    }
                }
            }
            pieces.get(i).setImage(writable);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        p1OriginalImages = Stream.of(p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King).map(ImageView::getImage).toList();
        p2OriginalImages = Stream.of(p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King).map(ImageView::getImage).toList();
        p1Pieces = List.of(p1Pawn, p1Rook, p1Knight, p1Bishop, p1Queen, p1King);
        p2Pieces = List.of(p2Pawn, p2Rook, p2Knight, p2Bishop, p2Queen, p2King);

        if (settings.getP1Pieces() != null){
            p1Color = settings.getP1Color();
            p1ColorPicker.setValue(p1Color);
            redraw(p1Color,p1Pieces,p1OriginalImages,p1DefaultColor);
        }
        if (settings.getP2Pieces() != null){
            p2Color = settings.getP2Color();
            p2ColorPicker.setValue(p2Color);
            redraw(p2Color,p2Pieces,p2OriginalImages,p2DefaultColor);
        }

        if (settings.getDarkTiles() != null){
            darkTileColor = settings.getDarkTiles();
            darkTilesColorPicker.setValue(darkTileColor);
        }
        else {
            darkTilesColorPicker.setValue(darkTileColor);
        }
        if (settings.getNormalTiles() != null) {
            normalTileColor = settings.getNormalTiles();
            normalTilesColorPicker.setValue(normalTileColor);
        }
        else {
            normalTilesColorPicker.setValue(normalTileColor);
        }

        if (settings.getLightTiles() != null){
            lightTileColor = settings.getLightTiles();
            lightTilesColorPicker.setValue(lightTileColor);
        }
        else {
            lightTilesColorPicker.setValue(lightTileColor);
        }
        darkPolygon.setFill(darkTileColor);
        normalPolygon.setFill(normalTileColor);
        lightPolygon.setFill(lightTileColor);

    }
    @FXML
    protected void onDarkTileAction() {
        darkTileColor = changeTileColor(darkTilesColorPicker,darkPolygon);
    }

    @FXML
    protected void onLightTileaAction() {
        lightTileColor = changeTileColor(lightTilesColorPicker,lightPolygon);
    }

    @FXML
    protected void onNormalTileAction() {
        normalTileColor = changeTileColor(normalTilesColorPicker,normalPolygon);
    }

    private Color changeTileColor(ColorPicker colorPicker, Polygon polygon){
        Color newColor = colorPicker.getValue();
        polygon.setFill(newColor);
        return newColor;
    }
}
