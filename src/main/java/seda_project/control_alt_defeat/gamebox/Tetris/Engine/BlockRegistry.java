package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import javafx.scene.paint.Color;

import java.util.*;

public class BlockRegistry {
    private static final Random RANDOM = new Random();

    private final List<TBlock> pieces = new ArrayList<>();

    private static BlockRegistry instance;

    public static BlockRegistry getInstance() {
        if (instance == null) instance = new BlockRegistry();
        return instance;
    }

    private BlockRegistry() {
        pieces.addAll(Arrays.asList(BlockType.values()));
    }

    public void addCustomPiece(boolean[][] shape, Color color){
        pieces.add(new CustomBlock(shape,color));
    }

    public void updateCustomPiece(boolean[][] shape, Color color, CustomBlock customBlock){
        int position = pieces.indexOf(customBlock);
        if (position == -1 ) throw new IllegalArgumentException("Piece not found in registry");
        pieces.set(position, new CustomBlock(shape, color));
    }

    public void removeCustomPiece(CustomBlock customBlock){
        pieces.remove(customBlock);
    }

    public List<TBlock> getAllPieces(){
        return Collections.unmodifiableList(pieces);
    }

    public List<CustomBlock> getCustomPieces() {
        return pieces.stream()
                .filter(p -> p instanceof CustomBlock)
                .map(p -> (CustomBlock) p)
                .toList();
    }

    public Block generateRandomBlock() {
        int index = RANDOM.nextInt(getAllPieces().size());
        TBlock tBlock = getAllPieces().get(index);
        return tBlock.toPiece();//new Block(shape, index + 1);
    }
}
