package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.util.List;

public class TetrisAdvancedSettings {

    // Normal Configuration is with a vertical screen and the item that swaps the boards
    private boolean vertical = true;
    private long itemSpawnRate = 30000;
    private long itemDespawnRate = 7000;
    private boolean swapBoards = true;
    private boolean swapBlocks = false;
    private boolean portals = false;
    private boolean opponentSlowDown = false;
    private boolean opponentSpeedUp = false;
    private boolean opponentDelayRotation = false;
    private boolean selfDelayRotation = false;
    private boolean selfSpeedUp = false;
    private boolean radialBomb = false;
    private boolean columnBomb = false;
    private boolean twoBlocks = false;


    private static TetrisAdvancedSettings instance;

    public static TetrisAdvancedSettings getInstance(){
        if (instance == null) instance = new TetrisAdvancedSettings();
        return instance;
    }

    private TetrisAdvancedSettings (){}

    public void saveBoolSettings(List<Boolean> booleanList ){
        this.vertical = booleanList.get(0);
        this.swapBoards = booleanList.get(1);
        this.swapBlocks = booleanList.get(2);
        this.portals = booleanList.get(3);
        this.opponentSlowDown = booleanList.get(4);
        this.opponentSpeedUp = booleanList.get(5);
        this.opponentDelayRotation = booleanList.get(6);
        this.selfDelayRotation = booleanList.get(7);
        this.selfSpeedUp = booleanList.get(8);
        this.radialBomb = booleanList.get(9);
        this.columnBomb = booleanList.get(10);
    }

    public void saveIntSettings(int itemSpawnRate, int itemDespawnRate){
        this.itemDespawnRate = itemDespawnRate* 1000L;
        this.itemSpawnRate = itemSpawnRate* 1000L;
    }

    public boolean isVertical() {
        return vertical;
    }

    public long getItemSpawnRate() {
        return itemSpawnRate;
    }

    public long getItemDespawnRate() {
        return itemDespawnRate;
    }

    public boolean isSwapBoards() {
        return swapBoards;
    }

    public boolean isSwapBlocks() {
        return swapBlocks;
    }

    public boolean isPortals() {
        return portals;
    }

    public boolean isOpponentSlowDown() {
        return opponentSlowDown;
    }

    public boolean isOpponentSpeedUp() {
        return opponentSpeedUp;
    }

    public boolean isOpponentDelayRotation() {
        return opponentDelayRotation;
    }

    public boolean isSelfDelayRotation() {
        return selfDelayRotation;
    }

    public boolean isSelfSpeedUp() {
        return selfSpeedUp;
    }

    public boolean isRadialBomb() {
        return radialBomb;
    }

    public boolean isColumnBomb() {
        return columnBomb;
    }

    public boolean isTwoBlocks() {
        return twoBlocks;
    }

    public void setTwoBlocks(boolean twoBlocks) {
        this.twoBlocks = twoBlocks;
    }
}
