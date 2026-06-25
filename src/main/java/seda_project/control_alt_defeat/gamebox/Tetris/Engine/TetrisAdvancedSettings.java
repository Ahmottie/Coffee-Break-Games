package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public class TetrisAdvancedSettings implements Serializable {

    // Normal Configuration is with a vertical screen and the item that swaps the boards
    private boolean vertical = true;
    private long itemSpawnRate = 30000;
    private long itemDespawnRate = 7000;
    private int bombChance = 70;
    private boolean swapBoards = true;
    private boolean swapBlocks = false;
    private boolean portals = false;
    private boolean opponentSlowDown = false;
    private boolean opponentSpeedUp = false;
    private boolean opponentDelayRotation = false;
    private boolean selfDelayRotation = false;
    private boolean selfSpeedDown = false;
    private boolean radialBomb = false;
    private boolean columnBomb = false;
    private boolean twoBlocks = false;
    private boolean isBoardChange = false;


    private static TetrisAdvancedSettings instance;

    public static TetrisAdvancedSettings getInstance(){
        if (instance == null) instance = new TetrisAdvancedSettings();
        return instance;
    }

    private TetrisAdvancedSettings (){}

    public void setVertical(boolean vertical) { this.vertical = vertical; }
    public void setSwapBoards(boolean swapBoards) { this.swapBoards = swapBoards; }
    public void setSwapBlocks(boolean swapBlocks) { this.swapBlocks = swapBlocks; }
    public void setPortals(boolean portals) { this.portals = portals; }
    public void setOpponentSlowDown(boolean opponentSlowDown) { this.opponentSlowDown = opponentSlowDown; }
    public void setOpponentSpeedUp(boolean opponentSpeedUp) { this.opponentSpeedUp = opponentSpeedUp; }
    public void setOpponentDelayRotation(boolean opponentDelayRotation) { this.opponentDelayRotation = opponentDelayRotation; }
    public void setSelfDelayRotation(boolean selfDelayRotation) { this.selfDelayRotation = selfDelayRotation; }
    public void setSelfSpeedDown(boolean selfSpeedDown) { this.selfSpeedDown = selfSpeedDown; }
    public void setRadialBomb(boolean radialBomb) { this.radialBomb = radialBomb; }
    public void setColumnBomb(boolean columnBomb) { this.columnBomb = columnBomb; }
    public void setBoardChange(boolean isBoardChange) { this.isBoardChange = isBoardChange; }

    public void saveIntSettings(int itemSpawnRate, int itemDespawnRate, int bombChance){
        this.itemDespawnRate = itemDespawnRate* 1000L;
        this.itemSpawnRate = itemSpawnRate* 1000L;
        this.bombChance = bombChance;
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

    public int getBombChance() { return bombChance; }

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

    public boolean isSelfSpeedDown() {
        return selfSpeedDown;
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

    public boolean isBoardChange() {
        return isBoardChange;
    }
}
