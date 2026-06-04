package seda_project.control_alt_defeat.gamebox.HexChess.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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
    private ColorPicker p1ColorPicker, p2ColorPicker;

    @FXML
    private Color p1Color = Color.WHITE;
    private Color p2Color = Color.BLACK;

    @FXML
    private final Color p1DefaultColor = Color.WHITE;
    @FXML
    private final Color p2DefaultColor = Color.BLACK;

    private List<ImageView> p1Pieces;
    private List<ImageView> p2Pieces;
    private List<Image> p1OriginalImages;
    private List<Image> p2OriginalImages;

    @FXML
    protected void onBackAction(ActionEvent actionEvent) {
        c.backScene(header,vS);
    }

    @FXML
    protected void onSaveAction() {
        settings.setP1Color(p1Color);
        settings.setP2Color(p2Color);
        settings.setP1Pieces(p1Pieces);
        settings.setP2Pieces(p2Pieces);
        String toastMsg = "Saved Settings";
        Toast.makeText(stackPane, toastMsg);
    }

    @FXML
    protected void onDefaultAction() {
        for (int i = 0; i < p1Pieces.size(); i++) {
            p1Pieces.get(i).setImage(p1OriginalImages.get(i));
        }
        for (int i = 0; i < p2Pieces.size(); i++) {
            p2Pieces.get(i).setImage(p2OriginalImages.get(i));
        }
        p1Color = p1DefaultColor;
        p2Color = p2DefaultColor;
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

    }
}
