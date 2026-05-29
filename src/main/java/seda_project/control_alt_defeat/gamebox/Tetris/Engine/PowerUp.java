package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public class PowerUp implements Serializable {

    private final int playerNum;
    private final int row;
    private final int col;
    private final long spawnTime;
    private final PowerUpType type;

    private boolean drawn;

    public PowerUp(int playerNum, int row, int col, long spawnTime, PowerUpType type, boolean drawn) {
        this.playerNum = playerNum;
        this.row = row;
        this.col = col;
        this.spawnTime = spawnTime;
        this.type = type;
        this.drawn = drawn;
    }

    public boolean isDrawn() {
        return drawn;
    }

    public void draw(){
        this.drawn = true;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public PowerUpType getType() {
        return type;
    }
}
