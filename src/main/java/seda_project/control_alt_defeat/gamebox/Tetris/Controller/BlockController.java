package seda_project.control_alt_defeat.gamebox.Tetris.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.CustomBlock;
import seda_project.control_alt_defeat.gamebox.Tetris.Engine.BlockRegistry;
import seda_project.control_alt_defeat.gamebox.ui.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class BlockController extends Controller implements Initializable {
    private BlockEditorController editorController;
    private Color selectedColor = Color.CYAN;
    private int selectedListIndex = -1;
    private boolean update = false;

    private final int RECTANGLE_SIZE = 75;

    @FXML
    private VBox header;

    @FXML
    private GridPane gridPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button saveButton;

    @FXML
    private ColorPicker colorPicker;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        BlockRegistry registry = BlockRegistry.getInstance();
        editorController = new BlockEditorController(registry);
        colorPicker.setValue(selectedColor);
        buildGrid();
        refreshList();

    }

    private void buildGrid() {
        gridPane.getChildren().clear();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Rectangle cell = createCell(row, col);
                gridPane.add(cell, col, row);
            }
        }
    }

    private Rectangle createCell(int row, int col) {
        Rectangle cell = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE);
        cell.setFill(Color.TRANSPARENT);
        cell.setStroke(Color.GRAY);
        cell.setStrokeWidth(0.5);

        cell.setOnMouseClicked(e -> {
            boolean[][] grid = editorController.getGrid();
            boolean alreadyActive = grid[row][col];

            if (alreadyActive || editorController.checkEmpty() || isAdjacentToActive(row, col)) {
                editorController.toggleCell(row, col);
                updateCellVisual(cell, row, col);
                updateAdjacent(row, col);
                updateAdjacentCellsVisual(row, col);
            }
        });

        return cell;
    }

    private void updateAdjacent(int row, int col) {
        try{
            updateAdjacentCellsVisual(row-1,col);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row+1,col);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row,col+1);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }
        try{
            updateAdjacentCellsVisual(row,col-1);
        }
        catch(NullPointerException npe){
            System.out.println(npe.getMessage());
        }

    }

    private void updateAdjacentCellsVisual(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) return;
        Rectangle cell = getCellAt(row,col);

        boolean[][] grid = editorController.getGrid();
        //Do not change already selected Cells
        if (grid[row][col]) return;

        boolean isAdjacentToActive = isAdjacentToActive(row, col);
        cell.setStroke(isAdjacentToActive ? Color.RED : Color.GRAY);
        cell.setStrokeWidth(isAdjacentToActive ? 1.5 : 0.5);
    }

    private boolean isAdjacentToActive(int row, int col) {
        boolean[][] grid = editorController.getGrid();
        int[][] directions = {{-1,0},{1,0},{0,1},{0,-1}};
        for (int[] d : directions) {
            int r = row + d[0], c = col + d[1];
            if (r >= 0 && r < 4 && c >= 0 && c < 4 && grid[r][c]) return true;
        }
        return false;
    }

    private Rectangle getCellAt(int row, int col) {
        for (var node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col)
                return (Rectangle) node;
        }
        return null;
    }

    private void updateCellVisual(Rectangle cell, int row, int col) {
        boolean active = editorController.getGrid()[row][col];
        cell.setFill(active ? selectedColor : Color.TRANSPARENT);
    }

    private void refreshGrid() {
        boolean[][] grid = editorController.getGrid();
        gridPane.getChildren().forEach(node -> {
            if (node instanceof Rectangle cell) {
                int col = GridPane.getColumnIndex(node);
                int row = GridPane.getRowIndex(node);
                cell.setFill(grid[row][col] ? selectedColor : Color.TRANSPARENT);
            }
        });
        for (int r = 0; r < 4; r++)
            for (int cc = 0; cc < 4; cc++)
                updateAdjacentCellsVisual(r, cc);
    }

    private void refreshList() {
        VBox list = new VBox(8);
        list.setStyle("-fx-padding: 8;");

        var customPieces = BlockRegistry.getInstance().getCustomPieces();

        for (int i = 0; i < customPieces.size(); i++) {
            final int index = i;
            CustomBlock piece = customPieces.get(i);

            HBox row = buildListRow(piece, index);
            list.getChildren().add(row);
        }
        if (customPieces.isEmpty()) selectedListIndex = -1;
        scrollPane.setContent(list);
    }

    private HBox buildListRow(CustomBlock piece, int index) {
        Pane preview = buildMiniPreview(piece);

        Label name = new Label("Custom " + (index + 1));
        name.setStyle("-fx-text-fill: -fx-text-base-color;");

        HBox row = new HBox(8, preview, name);
        row.setAlignment(Pos.CENTER);
        row.setStyle("-fx-padding: 4; -fx-cursor: hand;");

        row.setOnMouseClicked(e -> {
            selectedListIndex = index;
            ((VBox) scrollPane.getContent()).getChildren().forEach(
                    n -> n.setStyle("-fx-padding: 4; -fx-cursor: hand;")
            );
            row.setStyle("-fx-padding: 4; -fx-cursor: hand; -fx-background-color: -fx-selection-bar;");
        });
        return row;
    }

    private Pane buildMiniPreview(CustomBlock piece) {
        GridPane preview = new GridPane();
        boolean[][] shape = piece.getShape();
        int cellSize = 10;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setFill(shape[r][c] ? piece.getColor() : Color.TRANSPARENT);
                cell.setStroke(Color.GRAY);
                cell.setStrokeWidth(0.3);
                preview.add(cell, c, r);
            }
        }
        return preview;
    }


    @FXML
    protected void onBackAction(){
        c.backScene(header,vS);
    }

    @FXML
    protected void onLoadAction(){
        if (selectedListIndex < 0) return;

        var customPieces = BlockRegistry.getInstance().getCustomPieces();
        if (selectedListIndex >= customPieces.size()) return;

        CustomBlock piece = customPieces.get(selectedListIndex);
        update = true;
        saveButton.setText("Update");
        editorController.reset();
        editorController.loadForUpdate(piece);
        boolean[][] shape = piece.getShape();
        for (int r = 0; r < shape.length; r++)
            for (int c = 0; c < shape[r].length; c++)
                if (shape[r][c]) editorController.toggleCell(r, c);

        selectedColor = (Color) piece.getColor();
        refreshGrid();
        for (int r = 0; r < 4; r++)
            for (int cc = 0; cc < 4; cc++)
                updateAdjacentCellsVisual(r, cc);
    }

    @FXML
    protected void onSaveAction(){
        try {
            if (!update) {
                editorController.setColor(selectedColor);
                editorController.savePiece();
                editorController.reset();
                refreshGrid();
                refreshList();
            }
            else {
                editorController.setColor(selectedColor);
                editorController.updatePiece();
                editorController.reset();
                refreshGrid();
                refreshList();
                saveButton.setText("Save");
            }

        } catch (Exception ex) {
            saveButton.setText("Empty!");
            saveButton.setDisable(true);
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> {
                    saveButton.setText("Save");
                    saveButton.setDisable(false);
                });
            }).start();
        }
        update = false;
    }

    @FXML
    protected void onDeleteAction(){
        if (selectedListIndex < 0) return;

        BlockRegistry pR = BlockRegistry.getInstance();
        CustomBlock cP = pR.getCustomPieces().get(selectedListIndex);
        pR.removeCustomPiece(cP);
        refreshList();
        System.out.println(selectedListIndex);
    }

    @FXML
    protected void onColorAction(ActionEvent actionEvent){
        Color c = colorPicker.getValue();
        this.selectedColor = c;
        refreshGrid();
    }

    @FXML
    protected void onResetAction(){
        editorController.reset();
        refreshGrid();
        refreshList();
    }
}
