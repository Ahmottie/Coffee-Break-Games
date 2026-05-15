package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;

public record PowerUp(int playerNum, int row, int col, long spawnTime) implements Serializable {}