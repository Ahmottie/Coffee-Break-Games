package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class TetrisEngine {
    private Board p1Board;
    private Board p2Board;

    private Block p1ActiveBlock;
    private Block p2ActiveBlock;

    private int p1Score;
    private int p2Score;

    private final String p1Name;
    private final String p2Name;

    private boolean p1Lost;
    private boolean p2Lost;
    private boolean isGameOver;
    private int level = 1;

    private final BlockRegistry blockRegistry;

    private final List<TetrisEventListener> listeners = new CopyOnWriteArrayList<>();
    private int totalLinesCleared = 0;

    private static final long INITIAL_TICK_INTERVAL_MS = 800;
    private static final long MIN_TICK_INTERVAL_MS     = 100;
    private long tickIntervalMs = INITIAL_TICK_INTERVAL_MS;

    // Power-up mechanics
    private final List<PowerUp> activePowerUps = new CopyOnWriteArrayList<>();
    private long lastPowerUpSpawnTime;
    private static final long POWERUP_INTERVAL_MS = 30000;
    private static final Random RANDOM = new Random();

    public TetrisEngine(String p1Name, String p2Name, BlockRegistry registry) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;
        this.blockRegistry = registry;

        this.p1Board = new Board(false);
        this.p2Board = new Board(true);

        this.p1Score = 0;
        this.p2Score = 0;

        this.p1Lost = false;
        this.p2Lost = false;
        this.isGameOver = false;
        this.lastPowerUpSpawnTime = System.currentTimeMillis();

        spawnNewBlock(1);
        spawnNewBlock(2);
    }

    public synchronized long getTickIntervalMs() {
        return tickIntervalMs;
    }

    public synchronized void addListener(TetrisEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(TetrisEventListener listener) {
        listeners.remove(listener);
    }

    public synchronized void tick() {
        if (isGameOver) return;

        if (!p1Lost) applyGravity(1, p1ActiveBlock, p1Board);
        if (!p2Lost) applyGravity(2, p2ActiveBlock, p2Board);

        checkWinCondition();
        managePowerUpSpawning();

        GameState snap = getSnapshot();
        listeners.forEach(l -> l.onTick(snap));
    }

    private void managePowerUpSpawning() {
        long now = System.currentTimeMillis();
        if (now - lastPowerUpSpawnTime >= POWERUP_INTERVAL_MS) {
            spawnRandomPowerUp();
            lastPowerUpSpawnTime = now;
        }
    }

    private void spawnRandomPowerUp() {
        int playerNum = RANDOM.nextBoolean() ? 1 : 2;
        Board board = (playerNum == 1) ? p1Board : p2Board;
        String[][] grid = board.getGrid();

        // Find an empty cell
        int attempts = 0;
        while (attempts < 50) {
            int r = RANDOM.nextInt(board.getHeight());
            int c = RANDOM.nextInt(board.getWidth());
            if (grid[r][c] == null && !isPowerUpAt(playerNum, r, c)) {
                activePowerUps.add(new PowerUp(playerNum, r, c));
                break;
            }
            attempts++;
        }
    }

    private boolean isPowerUpAt(int playerNum, int row, int col) {
        for (PowerUp p : activePowerUps) {
            if (p.playerNum() == playerNum && p.row() == row && p.col() == col) {
                return true;
            }
        }
        return false;
    }

    public synchronized void processInput(int playerNum, String action) {
        if (isGameOver) return;
        if (playerNum == 1 && p1Lost) return;
        if (playerNum == 2 && p2Lost) return;

        Block block = (playerNum == 1) ? p1ActiveBlock : p2ActiveBlock;
        Board board = (playerNum == 1) ? p1Board : p2Board;

        switch (action) {
            case "LEFT" -> {
                block.moveLeft();
                if (!board.isValidPosition(block)) block.moveRight();
            }
            case "RIGHT" -> {
                block.moveRight();
                if (!board.isValidPosition(block)) block.moveLeft();
            }
            case "ROTATE" -> {
                block.rotateClockwise();
                if (!board.isValidPosition(block)) block.rotateCounterClockwise();
            }
            case "DROP" -> applyGravity(playerNum, block, board);
        }
    }

    private void applyGravity(int playerNum, Block activeBlock, Board board) {
        if (board.isInverted()) {
            activeBlock.moveUp();
        } else {
            activeBlock.moveDown();
        }

        if (!board.isValidPosition(activeBlock)) {
            if (board.isInverted()) {
                activeBlock.moveDown();
            } else {
                activeBlock.moveUp();
            }

            boolean triggeredSwap = checkPowerUpTrigger(playerNum, activeBlock);
            board.lockBlock(activeBlock);

            GameState snapLock = getSnapshot();
            listeners.forEach(l -> l.onBlockLocked(playerNum, snapLock));

            int linesCleared = board.clearLines();
            if (linesCleared > 0) {
                if (playerNum == 1) p1Score += calculateScore(linesCleared);
                else p2Score += calculateScore(linesCleared);

                GameState snapLines = getSnapshot();
                final int finalLinesCleared = linesCleared;
                listeners.forEach(l -> l.onLinesCleared(playerNum, finalLinesCleared, snapLines));

                handleLevelProgression(linesCleared);
            }

            if (triggeredSwap) {
                executePlayerSwap();
                GameState snapSwap = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(playerNum, snapSwap));
            }

            spawnNewBlock(playerNum);
        }
    }

    private boolean checkPowerUpTrigger(int playerNum, Block block) {
        boolean triggered = false;
        boolean[][] shape = block.getShape();
        int bx = block.getX();
        int by = block.getY();

        List<PowerUp> toRemove = new ArrayList<>();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int boardX = bx + col;
                    int boardY = by + row;
                    for (PowerUp p : activePowerUps) {
                        if (p.playerNum() == playerNum && p.col() == boardX && p.row() == boardY) {
                            triggered = true;
                            toRemove.add(p);
                        }
                    }
                }
            }
        }
        activePowerUps.removeAll(toRemove);
        return triggered;
    }

    private void executePlayerSwap() {
        // Option C: Swap board contents and active falling pieces
        String[][] tempGrid = deepCopy(p1Board.getGrid());
        p1Board.overwriteGrid(p2Board.getGrid());
        p2Board.overwriteGrid(tempGrid);

        Block tempBlock = p1ActiveBlock;
        p1ActiveBlock = p2ActiveBlock;
        p2ActiveBlock = tempBlock;

        // Swap power-up ownership mappings
        List<PowerUp> swappedPowerUps = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            swappedPowerUps.add(new PowerUp(p.playerNum() == 1 ? 2 : 1, p.row(), p.col()));
        }
        activePowerUps.clear();
        activePowerUps.addAll(swappedPowerUps);

        // Validation immediately following swap
        if (p1ActiveBlock != null && !p1Board.isValidPosition(p1ActiveBlock)) {
            p1Lost = true;
            GameState snapLost = getSnapshot();
            listeners.forEach(l -> l.onPlayerLost(1, snapLost));
        }
        if (p2ActiveBlock != null && !p2Board.isValidPosition(p2ActiveBlock)) {
            p2Lost = true;
            GameState snapLost = getSnapshot();
            listeners.forEach(l -> l.onPlayerLost(2, snapLost));
        }
    }

    private void handleLevelProgression(int linesCleared) {
        totalLinesCleared += linesCleared;
        int newLevel = (totalLinesCleared / 10) + 1;

        if (newLevel > this.level) {
            this.level = newLevel;
            this.tickIntervalMs = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, this.level - 1)
            );

            GameState snapLevel = getSnapshot();
            listeners.forEach(l -> l.onLevelChanged(this.level, this.tickIntervalMs, snapLevel));
        }
    }

    private void spawnNewBlock(int playerNum) {
        Block newBlock = blockRegistry.generateRandomBlock();

        if (playerNum == 2) {
            while(newBlock.getY() < 19) {
                newBlock.moveDown();
            }
        }

        if (playerNum == 1) {
            p1ActiveBlock = newBlock;
            if (!p1Board.isValidPosition(p1ActiveBlock)) {
                p1Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(1, snapLost));
            }
        } else {
            p2ActiveBlock = newBlock;
            if (!p2Board.isValidPosition(p2ActiveBlock)) {
                p2Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(2, snapLost));
            }
        }
    }

    private int calculateScore(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 0;
        };
    }

    private void checkWinCondition() {
        if (!isGameOver && p1Lost && p2Lost) {
            isGameOver = true;
            GameState snapOver = getSnapshot();
            listeners.forEach(l -> l.onGameOver(snapOver));
        }
    }

    public synchronized GameState getSnapshot() {
        return new GameState(
                deepCopy(p1Board.getGrid()),
                deepCopy(p2Board.getGrid()),
                p1ActiveBlock != null ? p1ActiveBlock.cloneForSnapshot() : null,
                p2ActiveBlock != null ? p2ActiveBlock.cloneForSnapshot() : null,
                p1Score, p2Score,
                p1Name, p2Name,
                p1Lost, p2Lost,
                isGameOver,
                level,
                new ArrayList<>(activePowerUps)
        );
    }

    private static String[][] deepCopy(String[][] src) {
        String[][] dst = new String[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = src[i].clone();
        }
        return dst;
    }

    public record GameState(
            String[][] p1Grid,
            String[][] p2Grid,
            Block      p1ActiveBlock,
            Block      p2ActiveBlock,
            int        p1Score,
            int        p2Score,
            String     p1Name,
            String     p2Name,
            boolean    p1Lost,
            boolean    p2Lost,
            boolean    gameOver,
            int        level,
            List<PowerUp> powerUps
    ) implements Serializable {}

    public synchronized void reset() {
        p1Board.clear();
        p2Board.clear();
        p1Score = 0;
        p2Score = 0;
        p1Lost = false;
        p2Lost = false;
        isGameOver = false;

        level = 1;
        totalLinesCleared = 0;
        tickIntervalMs = INITIAL_TICK_INTERVAL_MS;

        activePowerUps.clear();
        lastPowerUpSpawnTime = System.currentTimeMillis();

        spawnNewBlock(1);
        spawnNewBlock(2);

        GameState snapReset = getSnapshot();
        listeners.forEach(l -> l.onReset(snapReset));
    }
}