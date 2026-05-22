package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

    private int p1Level = 1;
    private int p2Level = 1;

    private List<TBlock> p1Blocks;
    private List<TBlock> p2Blocks;

    private final BlockRegistry blockRegistry;
    private final TetrisAdvancedSettings advancedSettings;

    private final List<TetrisEventListener> listeners = new CopyOnWriteArrayList<>();

    private int p1LinesCleared = 0;
    private int p2LinesCleared = 0;

    private static final long INITIAL_TICK_INTERVAL_MS = 800;
    private static final long MIN_TICK_INTERVAL_MS     = 100;

    private long p1TickInterval = INITIAL_TICK_INTERVAL_MS;
    private long p2TickInterval = INITIAL_TICK_INTERVAL_MS;


    // Power-up mechanics
    private final List<PowerUp> activePowerUps = new CopyOnWriteArrayList<>();
    private long lastPowerUpSpawnTime;
    private static long POWERUP_INTERVAL_MS = 10000;
    private static long POWERUP_LIFESPAN_MS  = 7000;
    private static final Random RANDOM = new Random();

    private List<BombType> activeBombs;

    private boolean isStopped = false;

    public TetrisEngine(String p1Name, String p2Name, int p1Level, int p2Level, BlockRegistry registry, TetrisAdvancedSettings advancedSettings) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;

        this.blockRegistry = registry;
        this.advancedSettings = advancedSettings;

        this.p1Board = new Board(false);
        this.p2Board = new Board(true);

        this.p1Score = 0;
        this.p2Score = 0;

        this.p1Level = p1Level;
        this.p2Level = p2Level;

        POWERUP_INTERVAL_MS = advancedSettings.getItemSpawnRate();
        POWERUP_LIFESPAN_MS = advancedSettings.getItemDespawnRate();

        activeBombs = new ArrayList<>();
        if (advancedSettings.isRadialBomb()) activeBombs.add(BombType.RADIUS);
        if (advancedSettings.isColumnBomb()) activeBombs.add(BombType.CLEAR_BELOW);

        if (p1Level > 1){
            p1TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p1Level - 1)
            );
        }

        if (p2Level > 1){
            p2TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p2Level - 1)
            );
        }

        this.p1Lost = false;
        this.p2Lost = false;
        this.isGameOver = false;
        this.lastPowerUpSpawnTime = System.currentTimeMillis();

        p1Blocks = generateBlocks();
        p2Blocks = generateBlocks();

        spawnNewBlock(1);
        spawnNewBlock(2);
    }

    private List<TBlock> generateBlocks() {
        List<TBlock> allPieces = new ArrayList<>(blockRegistry.getAllPieces());
        Collections.shuffle(allPieces);
        return allPieces;
    }

    public synchronized void stop(){
        if (isStopped) return;
        isStopped = true;
        GameState snapStopped = getSnapshot();
        listeners.forEach(l -> l.onStopped(snapStopped));
    }

    public synchronized long getTickIntervalMs(int player) {
        return player == 1 ? p1TickInterval : p2TickInterval;

    }

    public synchronized void addListener(TetrisEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeListener(TetrisEventListener listener) {
        listeners.remove(listener);
    }

    public synchronized void tick(int player) {
        if (isGameOver || isStopped) return;

        if (!p1Lost && player == 1) applyGravity(1, p1ActiveBlock, p1Board);
        if (!p2Lost && player == 2) applyGravity(2, p2ActiveBlock, p2Board);

        checkWinCondition();
        managePowerUpSpawning();

        GameState snap = getSnapshot();
        listeners.forEach(l -> l.onTick(snap, player));
    }

    private void managePowerUpSpawning() {
        long now = System.currentTimeMillis();

        List<PowerUp> expired = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            if (now - p.spawnTime() >= POWERUP_LIFESPAN_MS) {
                expired.add(p);
            }
        }
        if (!expired.isEmpty()){
            activePowerUps.removeAll(expired);
            GameState snapExpired = getSnapshot();
            listeners.forEach(l -> l.onPowerUpSpawned(snapExpired));
        }

        if (now - lastPowerUpSpawnTime >= POWERUP_INTERVAL_MS) {
            spawnRandomPowerUp(now);
            lastPowerUpSpawnTime = now;
        }
    }

    private void spawnRandomPowerUp(long now ) {
        int playerNum = RANDOM.nextBoolean() ? 1 : 2;
        if (p1Lost) {playerNum = 2;}
        if (p2Lost) {playerNum = 1;}

        Board board = (playerNum == 1) ? p1Board : p2Board;
        String[][] grid = board.getGrid();

        // Find an empty cell
        int attempts = 0;
        while (attempts < 50) {
            int r = RANDOM.nextInt(board.getHeight()-3);
            int c = RANDOM.nextInt(board.getWidth());
            if (playerNum == 1 && grid[r+3][c] == null && !isPowerUpAt(playerNum, r+3, c)) {
                activePowerUps.add(new PowerUp(playerNum, r+3, c,now));
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
                break;
            }
            else if (playerNum == 2 && grid[r][c] == null && !isPowerUpAt(playerNum, r, c)) {
                activePowerUps.add(new PowerUp(playerNum, r, c,now));
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
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

        if (isGameOver || isStopped) return;
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
        listeners.forEach(l -> l.onBlockMovement(getSnapshot(),playerNum));
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
            if (activeBlock instanceof BombBlock bb){
                explode(playerNum, bb, board);
                GameState snapExplode = getSnapshot();
                listeners.forEach(l -> l.onBlockMovement(snapExplode,playerNum));
            }
            else {
                board.lockBlock(activeBlock);

                GameState snapLock = getSnapshot();
                listeners.forEach(l -> l.onBlockLocked(playerNum, snapLock));
            }
            int linesCleared = board.clearLines();
            if (linesCleared > 0) {
                if (playerNum == 1) {
                    p1LinesCleared +=  linesCleared;
                    p1Score += calculateScore(linesCleared);
                }
                else {
                    p2LinesCleared +=  linesCleared;
                    p2Score += calculateScore(linesCleared);
                }

                GameState snapLines = getSnapshot();
                final int finalLinesCleared = linesCleared;
                listeners.forEach(l -> l.onLinesCleared(playerNum, finalLinesCleared, snapLines));

                handleLevelProgression(playerNum);
            }
            spawnNewBlock(playerNum);
        }
        else if (checkPowerUpTrigger(playerNum, activeBlock)) {
                executePlayerSwap();
                GameState snapSwap = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(playerNum, snapSwap));
            }


    }

    private void explode(int playerNum, BombBlock bb, Board board) {
        BombType type = bb.getType();
        int posX = bb.getX(); //Spalte
        int posY = bb.getY(); //Zeile
        String[][] grid = board.getGrid();
        if (type == BombType.RADIUS) {
            for (int i = 0; i <= 3; i++) {
                for (int j = 0; j <= 3; j++) {
                    int posXminus = posX - i;
                    int posXplus  = posX + i;
                    int posYminus = posY - j;
                    int posYplus  = posY + j;

                    if (posXminus >= 0 && posYminus >= 0)
                        grid[posYminus][posXminus] = null;
                    if (posXminus >= 0 && posYplus < grid.length)
                        grid[posYplus][posXminus] = null;
                    if (posXplus < grid[0].length && posYplus < grid.length)
                        grid[posYplus][posXplus] = null;
                    if (posXplus < grid[0].length && posYminus >= 0)
                        grid[posYminus][posXplus] = null;
                }
            }
        }
        if (type == BombType.CLEAR_BELOW){
            int i = posY;
            while (playerNum == 1 ? i < grid.length : i >= 0) {
                grid[i][posX] = null;
                if (playerNum == 1) i++; else i--;
            }
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
        String[][] tempGrid = deepCopy(p1Board.getGrid());
        p1Board.overwriteGrid(rotateGrid180(p2Board.getGrid()));
        p2Board.overwriteGrid(rotateGrid180(tempGrid));

        Block tempBlock = p1ActiveBlock;
        p1ActiveBlock = p2ActiveBlock;
        p2ActiveBlock = tempBlock;

        rotateBlock180(p1ActiveBlock);
        rotateBlock180(p2ActiveBlock);

        boolean swapped = false;
        if (p1Lost && !p2Lost) {
            p1Lost = false;
            p2Lost = true;
            swapped = true;
        }
        if (!p1Lost && p2Lost && !swapped) {
            p1Lost = true;
            p2Lost = false;
        }

        // Swap power-up ownership mappings
        List<PowerUp> swappedPowerUps = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            swappedPowerUps.add(new PowerUp(p.playerNum() == 1 ? 2 : 1, Math.abs(20-p.row()), Math.abs(10-p.col()),p.spawnTime()));
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

    private void rotateBlock180(Block activeBlock) {
        activeBlock.setX((10 - 1) - (activeBlock.getX() + activeBlock.getShape().length - 1));
        activeBlock.setY( (20 - 1) - (activeBlock.getY() + activeBlock.getShape()[0].length - 1));

        boolean[][] shape = activeBlock.getShape();
        int rows = shape.length;
        int cols = shape[0].length;

        boolean[][] rotated = new boolean[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                rotated[row][col] = shape[rows-1-row][cols-1-col];
            }
        }
        activeBlock.setShape(rotated);
    }

    private static String[][] rotateGrid180(String[][] src) {
        int rows = src.length;
        int cols = src[0].length;
        String[][] rotated = new String[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[r][c] = src[rows - 1 - r][cols - 1 - c];
            }
        }
        return rotated;
    }

    private void handleLevelProgression(int player) {
        int level = player == 1 ? p1Level : p2Level;
        int playerLines = player == 1 ? p1LinesCleared : p2LinesCleared;

        int newLevel = (playerLines / 10) + 1;
        if (newLevel > level) {
            long newTickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, level - 1)
            );
            if (player == 1){
                p1Level = newLevel;
                p1TickInterval = newTickInterval;
            }
            else if (player == 2){
                p2Level = newLevel;
                p2TickInterval = newTickInterval;
            }

            GameState snapLevel = getSnapshot();
            listeners.forEach(l -> l.onLevelChanged(newTickInterval, snapLevel, player));
        }
    }

    private void spawnNewBlock(int playerNum) {
        boolean bomb = !activeBombs.isEmpty() && RANDOM.nextInt(1, 100) > 70;
        if (bomb){
            BombType selectedType = activeBombs.get(RANDOM.nextInt(0,activeBombs.size()));
            BombBlock newBombBlock = new BombBlock(selectedType);
            if (playerNum == 1){
                p1ActiveBlock = newBombBlock;
                checkValidPosition(playerNum);
            }
            else {
                while (newBombBlock.getY() < 20 - newBombBlock.getShape()[0].length) {
                    newBombBlock.moveDown();
                }
                p2ActiveBlock = newBombBlock;
                checkValidPosition(playerNum);
            }
        }
        else {
            if (playerNum == 1) {
                refillBlocks(1);
                Block newBlock = p1Blocks.getFirst().toPiece();
                p1Blocks.removeFirst();
                p1ActiveBlock = newBlock;
                checkValidPosition(playerNum);

            } else {
                refillBlocks(2);
                Block newBlock = p2Blocks.getFirst().toPiece();
                p2Blocks.removeFirst();
                while (newBlock.getY() < 20 - newBlock.getShape()[0].length) {
                    newBlock.moveDown();
                }
                p2ActiveBlock = newBlock;
                checkValidPosition(playerNum);
            }
        }

    }

    private void refillBlocks(int i) {
        if (i == 1 && p1Blocks.isEmpty()) {
            p1Blocks = generateBlocks();
        } else if (i != 1 && p2Blocks.isEmpty()) {
            p2Blocks = generateBlocks();
        }
    }

    private void checkValidPosition(int playerNum){
        if (playerNum == 1) {
            if (!p1Board.isValidPosition(p1ActiveBlock)) {
                p1Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(1, snapLost));
            }
        }
        else {
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
        if (!isGameOver && !isStopped && p1Lost && p2Lost) {
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
                p1Level, p2Level,
                p1Name, p2Name,
                p1Lost, p2Lost,
                isGameOver,
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
            int        p1Level,
            int        p2Level,
            String     p1Name,
            String     p2Name,
            boolean    p1Lost,
            boolean    p2Lost,
            boolean    gameOver,
            List<PowerUp> powerUps
    ) implements Serializable {}

    public synchronized void reset() {
        p1Board.clear();
        p2Board.clear();
        p1Score = 0;
        p2Score = 0;
        p1Level = 1;
        p2Level = 1;
        p1Lost = false;
        p2Lost = false;
        isGameOver = false;

        p1LinesCleared = 0;
        p2LinesCleared = 0;

        p1TickInterval = INITIAL_TICK_INTERVAL_MS;
        p2TickInterval = INITIAL_TICK_INTERVAL_MS;

        activePowerUps.clear();
        lastPowerUpSpawnTime = System.currentTimeMillis();

        spawnNewBlock(1);
        spawnNewBlock(2);

        GameState snapReset = getSnapshot();
        listeners.forEach(l -> l.onReset(snapReset));
    }
}