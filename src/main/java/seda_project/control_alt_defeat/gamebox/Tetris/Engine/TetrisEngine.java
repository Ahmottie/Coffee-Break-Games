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

    private Block[] p1ActiveBlocks = new Block[2];
    private Block[] p2ActiveBlocks = new Block[2];
    private boolean isTwoBlockMode = false;

    private ArrayList<Block> p1NextActiveBlock;
    private ArrayList<Block> p2NextActiveBlock;

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

    private long p1BaseTickInterval;
    private long p2BaseTickInterval;

    private final List<Integer> p1SlowEffects = new ArrayList<>();
    private final List<Integer> p2SlowEffects = new ArrayList<>();
    private final List<Integer> p1FastEffects = new ArrayList<>();
    private final List<Integer> p2FastEffects = new ArrayList<>();

    private int p1RotationDelayCounter;
    private int p2RotationDelayCounter;
    private boolean p1RotationDelayCheck;
    private boolean p2RotationDelayCheck;
    private boolean p1RotationLocked;
    private boolean p2RotationLocked;

    // Power-up mechanics
    private final List<PowerUp> activePowerUps = new CopyOnWriteArrayList<>();
    private long lastPowerUpSpawnTime;
    private static long POWERUP_INTERVAL_MS = 10000;
    private static long POWERUP_LIFESPAN_MS  = 7000;
    private static final Random RANDOM = new Random();

    private List<BombType> possibleBombs;
    private List<PowerUpType> possiblePowerUps;

    private boolean isStopped = false;

    public TetrisEngine(String p1Name, String p2Name, int p1Level, int p2Level, BlockRegistry registry, TetrisAdvancedSettings advancedSettings) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;

        this.p1NextActiveBlock = new ArrayList<>();
        this.p2NextActiveBlock = new ArrayList<>();

        this.blockRegistry = registry;
        this.advancedSettings = advancedSettings;

        this.p1Board = new Board(false);
        this.p2Board = new Board(true);

        this.p1Score = 0;
        this.p2Score = 0;

        this.p1Level = p1Level;
        this.p2Level = p2Level;

        this.isTwoBlockMode = advancedSettings.isTwoBlocks();

        POWERUP_INTERVAL_MS = advancedSettings.getItemSpawnRate();
        POWERUP_LIFESPAN_MS = advancedSettings.getItemDespawnRate();

        possibleBombs = new ArrayList<>();
        if (advancedSettings.isRadialBomb()) possibleBombs.add(BombType.RADIUS);
        if (advancedSettings.isColumnBomb()) possibleBombs.add(BombType.CLEAR_BELOW);

        possiblePowerUps = new ArrayList<>();
        if (advancedSettings.isOpponentDelayRotation()) possiblePowerUps.add(PowerUpType.OPPONENTROTATIONDELAY);
        if (advancedSettings.isOpponentSlowDown()) possiblePowerUps.add(PowerUpType.OPPONENTSPEEDDOWN);
        if (advancedSettings.isOpponentSpeedUp()) possiblePowerUps.add(PowerUpType.OPPONENTSPEEDUP);
        if (advancedSettings.isSelfDelayRotation()) possiblePowerUps.add(PowerUpType.SELFROTATIONDELAY);
        if (advancedSettings.isSelfSpeedDown()) possiblePowerUps.add(PowerUpType.SELFSPEEDDOWN);
        if (advancedSettings.isPortals()) possiblePowerUps.add(PowerUpType.PORTAL);
        if (advancedSettings.isSwapBlocks()) possiblePowerUps.add(PowerUpType.SWAPACTIVEBLOCKS);
        if (advancedSettings.isSwapBoards()) possiblePowerUps.add(PowerUpType.SWAPBOARDS);

        if (p1Level > 1){
            p1TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p1Level - 1)
            );
        }
        p1BaseTickInterval = p1TickInterval;

        if (p2Level > 1){
            p2TickInterval = (long) Math.max(
                    MIN_TICK_INTERVAL_MS,
                    INITIAL_TICK_INTERVAL_MS * Math.pow(0.85, p2Level - 1)
            );
        }
        p2BaseTickInterval = p2TickInterval;

        this.p1Lost = false;
        this.p2Lost = false;
        this.isGameOver = false;
        this.lastPowerUpSpawnTime = System.currentTimeMillis();

        p1Blocks = generateBlocks();
        p2Blocks = generateBlocks();

        spawnNewBlock(1, 0);
        spawnNewBlock(2, 0);

        if (isTwoBlockMode) {
            spawnNewBlock(1, 1);
            spawnNewBlock(2, 1);
        }
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

    private long recomputeInterval(int player) {
        long baseSpeed = (player == 1) ? p1BaseTickInterval : p2BaseTickInterval;
        List<Integer> slows = (player == 1) ? p1SlowEffects : p2SlowEffects;
        List<Integer> fasts = (player == 1) ? p1FastEffects : p2FastEffects;
        int exponent = slows.size() - fasts.size();
        if (exponent >= 0) {
            return (long) (baseSpeed * (Math.pow(2,exponent)));
        } else {
            return (long) (baseSpeed / (Math.pow(2,(-exponent))));
        }
    }

    private void processSpeedEffects(int player) {
        List<Integer> slows = (player == 1) ? p1SlowEffects : p2SlowEffects;
        List<Integer> fasts = (player == 1) ? p1FastEffects : p2FastEffects;

        boolean changed = tickDown(slows) | tickDown(fasts);

        if (changed) {
            long newInterval = recomputeInterval(player);
            if (player == 1) {
                p1TickInterval = newInterval;
            }
            else {
                p2TickInterval = newInterval;
            }
            listeners.forEach(l -> l.changeTickSpeed(player, newInterval));
        }
    }

    private boolean tickDown(List<Integer> effects) {
        boolean expired = effects.removeIf(t -> t <= 1);
        effects.replaceAll(t -> t - 1);
        return expired;
    }

    public synchronized void tick(int player) {
        if (isGameOver || isStopped) return;

        if (player == 1) {
            processSpeedEffects(1);
        } else {
            processSpeedEffects(2);
        }

        if (!p1Lost && player == 1) {
            if (p1ActiveBlocks[0] != null) applyGravity(1, 0, p1Board);
            if (isTwoBlockMode && p1ActiveBlocks[1] != null) applyGravity(1, 1, p1Board);
        }

        if (!p2Lost && player == 2) {
            if (p2ActiveBlocks[0] != null) applyGravity(2, 0, p2Board);
            if (isTwoBlockMode && p2ActiveBlocks[1] != null) applyGravity(2, 1, p2Board);
        }

        checkWinCondition();
        managePowerUpSpawning();

        GameState snap = getSnapshot();
        listeners.forEach(l -> l.onTick(snap, player));
    }

    private void managePowerUpSpawning() {
        long now = System.currentTimeMillis();

        List<PowerUp> expired = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            if (now - p.getSpawnTime() >= POWERUP_LIFESPAN_MS) {
                expired.add(p);
            }
        }
        if (!expired.isEmpty()){
            activePowerUps.removeAll(expired);
            for (PowerUp p : expired){
                GameState snapExpired = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(snapExpired,p));
            }
        }

        if (now - lastPowerUpSpawnTime >= POWERUP_INTERVAL_MS && !possiblePowerUps.isEmpty()) {
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
        PowerUpType selected = possiblePowerUps.get(RANDOM.nextInt(possiblePowerUps.size()));
        int attempts = 0;
        while (attempts < 50) {
            int r = RANDOM.nextInt(board.getHeight()-3);
            int c = RANDOM.nextInt(board.getWidth());
            if (playerNum == 1 && grid[r+3][c] == null && !isPowerUpAt(playerNum, r+3, c)) {
                PowerUp p = new PowerUp(playerNum, r+3, c,now,selected,false);
                activePowerUps.add(p);
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
                p.draw();
                break;
            }
            else if (playerNum == 2 && grid[r][c] == null && !isPowerUpAt(playerNum, r, c)) {
                PowerUp p = new PowerUp(playerNum, r, c,now,selected,false);
                activePowerUps.add(p);
                listeners.forEach(l -> l.onPowerUpSpawned(this.getSnapshot()));
                p.draw();
                break;
            }
            attempts++;
        }
    }

    private boolean isPowerUpAt(int playerNum, int row, int col) {
        for (PowerUp p : activePowerUps) {
            if (p.getPlayerNum() == playerNum && p.getRow() == row && p.getCol() == col) {
                return true;
            }
        }
        return false;
    }

    public synchronized void processInput(int playerNum, String action, int blockIndex) {
        if (isGameOver || isStopped) return;
        if (playerNum == 1 && p1Lost) return;
        if (playerNum == 2 && p2Lost) return;
        if (!isTwoBlockMode && blockIndex > 0) return;

        Block block = (playerNum == 1) ? p1ActiveBlocks[blockIndex] : p2ActiveBlocks[blockIndex];
        Block otherBlock = isTwoBlockMode ? ((playerNum == 1) ? p1ActiveBlocks[(blockIndex + 1) % 2] : p2ActiveBlocks[(blockIndex + 1) % 2]) : null;
        Board board = (playerNum == 1) ? p1Board : p2Board;

        if (block == null) return;

        switch (action) {
            case "LEFT" -> {
                block.moveLeft();
                if (!board.isValidPosition(block, otherBlock)) block.moveRight();
            }
            case "RIGHT" -> {
                block.moveRight();
                if (!board.isValidPosition(block, otherBlock)) block.moveLeft();
            }
            case "ROTATE" -> {
                if (playerNum == 1) {
                    if (p1RotationLocked && p1RotationDelayCheck) p1RotationLocked = false;
                    else {
                        p1RotationLocked = true;
                        if (p1RotationDelayCounter > 0) p1RotationDelayCounter--;
                        else p1RotationDelayCheck = false;
                        block.rotateClockwise();
                        if (!board.isValidPosition(block, otherBlock)) block.rotateCounterClockwise();
                    }
                } else {
                    if (p2RotationLocked && p2RotationDelayCheck) p2RotationLocked = false;
                    else {
                        p2RotationLocked = true;
                        if (p2RotationDelayCounter > 0) p2RotationDelayCounter--;
                        else p2RotationDelayCheck = false;
                        block.rotateClockwise();
                        if (!board.isValidPosition(block, otherBlock)) block.rotateCounterClockwise();
                    }
                }
            }
            case "DROP" -> applyGravity(playerNum, blockIndex, board);
        }
        listeners.forEach(l -> l.onBlockMovement(getSnapshot(), playerNum));
    }

    private void applyGravity(int playerNum, int blockIndex, Board board) {
        Block activeBlock = (playerNum == 1) ? p1ActiveBlocks[blockIndex] : p2ActiveBlocks[blockIndex];
        Block otherBlock = isTwoBlockMode ? ((playerNum == 1) ? p1ActiveBlocks[(blockIndex + 1) % 2] : p2ActiveBlocks[(blockIndex + 1) % 2]) : null;

        if (board.isInverted()) {
            activeBlock.moveUp();
        } else {
            activeBlock.moveDown();
        }

        if (!board.isValidPosition(activeBlock, otherBlock)) {
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
                    if (advancedSettings.isBoardChange() && !p1Lost && !p2Lost) {
                        p1Board.expand(linesCleared);
                        p2Board.shrink(linesCleared);
                        for(int i = 0; i < (isTwoBlockMode ? 2 : 1); i++) {
                            if (p2ActiveBlocks[i] != null) p2ActiveBlocks[i].setY(p2ActiveBlocks[i].getY() + linesCleared);
                        }
                        listeners.forEach(l -> l.onBoardSizeChange(playerNum,linesCleared,getSnapshot()));
                    }
                }
                else {
                    p2LinesCleared +=  linesCleared;
                    p2Score += calculateScore(linesCleared);
                    if( advancedSettings.isBoardChange() && !p1Lost && !p2Lost) {
                        p2Board.expand(linesCleared);
                        p1Board.shrink(linesCleared);
                        for(int i = 0; i < (isTwoBlockMode ? 2 : 1); i++) {
                            if (p1ActiveBlocks[i] != null) p1ActiveBlocks[i].setY(p1ActiveBlocks[i].getY() - linesCleared);
                        }
                        listeners.forEach(l -> l.onBoardSizeChange(playerNum,linesCleared,getSnapshot()));
                    }
                }

                GameState snapLines = getSnapshot();
                final int finalLinesCleared = linesCleared;
                listeners.forEach(l -> l.onLinesCleared(playerNum, finalLinesCleared, snapLines));

                handleLevelProgression(playerNum);
            }
            spawnNewBlock(playerNum, blockIndex);
        }
        else{
            PowerUp hit = checkPowerUpTrigger(playerNum, activeBlock);
            if (hit != null) {
                switch (hit.getType()) {
                    case PORTAL : {
                        executePortal(playerNum,activeBlock, blockIndex);
                        break;
                    }
                    case SELFSPEEDDOWN:{
                        if (playerNum == 1){
                            p1SlowEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(playerNum, p1TickInterval));
                        }
                        else {
                            p2SlowEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(playerNum, p2TickInterval));
                        }
                        break;
                    }
                    case OPPONENTSPEEDUP: {
                        if (playerNum == 1){
                            p2FastEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(2, p2TickInterval));;
                        }
                        else {
                            p1FastEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(1, p1TickInterval));
                        }
                        break;
                    }
                    case SWAPACTIVEBLOCKS: {
                        swapActiveBlocks();
                        listeners.forEach(l -> l.onBlockSwap(getSnapshot()));
                        break;
                    }
                    case OPPONENTSPEEDDOWN: {
                        if (playerNum == 1){
                            p2SlowEffects.add(10);
                            p2TickInterval = recomputeInterval(2);
                            listeners.forEach(l -> l.changeTickSpeed(2, p2TickInterval));
                        }
                        else {
                            p1SlowEffects.add(10);
                            p1TickInterval = recomputeInterval(1);
                            listeners.forEach(l -> l.changeTickSpeed(1, p1TickInterval));
                        }
                        break;
                    }
                    case SELFROTATIONDELAY: {
                        if (playerNum == 1) {
                            p1RotationDelayCounter += 10;
                            p1RotationDelayCheck = true;
                            p1RotationLocked = true;
                        } else {
                            p2RotationDelayCounter += 10;
                            p2RotationDelayCheck = true;
                            p2RotationLocked = true;
                        }
                        break;
                    }
                    case OPPONENTROTATIONDELAY: {
                        if (playerNum == 1) {
                            p2RotationDelayCounter += 10;
                            p2RotationDelayCheck = true;
                            p2RotationLocked = true;
                        } else {
                            p1RotationDelayCounter += 10;
                            p1RotationDelayCheck = true;
                            p1RotationLocked = true;
                        }
                        break;
                    }
                    case SWAPBOARDS:{
                        executePlayerSwap();
                        break;
                    }
                }
                GameState snapSwap = getSnapshot();
                listeners.forEach(l -> l.onPowerUpTriggered(snapSwap,hit));
            }
        }
    }

    private void swapActiveBlocks() {
        Block[] temp = p1ActiveBlocks;
        p1ActiveBlocks = p2ActiveBlocks;
        p2ActiveBlocks = temp;

        for (int i = 0; i < 2; i++) {
            if (p1ActiveBlocks[i] != null) swapBlockErrorCorrection(p1ActiveBlocks[i].getX(), p1ActiveBlocks[i].getY(), p1ActiveBlocks[i], p1Board);
            if (p2ActiveBlocks[i] != null) swapBlockErrorCorrection(p2ActiveBlocks[i].getX(), p2ActiveBlocks[i].getY(), p2ActiveBlocks[i], p2Board);
        }
    }

    private void swapBlockErrorCorrection(int posX, int posY, Block p1ActiveBlock, Board p1Board) {
        if (posX + p1ActiveBlock.getShape().length > p1Board.getWidth()){
            posX = posX+(p1Board.getWidth() - posX - p1ActiveBlock.getShape().length);

        }
        if (posY + p1ActiveBlock.getShape().length > p1Board.getHeight()){
            posY = posY + (p1Board.getHeight() - posY - p1ActiveBlock.getShape().length);
        }
        p1ActiveBlock.setX(posX);
        p1ActiveBlock.setY(posY);
    }

    private void executePortal(int playerNum, Block activeBlock, int sourceIndex) {
        if (playerNum == 1) {
            p2NextActiveBlock.add(activeBlock);
            spawnNewBlock(1, sourceIndex);
        } else {
            p1NextActiveBlock.add(activeBlock);
            spawnNewBlock(2, sourceIndex);
        }
    }

    private void explode(int playerNum, BombBlock bb, Board board) {
        BombType type = bb.getType();
        int posX = bb.getX();
        int posY = bb.getY();
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

    private PowerUp checkPowerUpTrigger(int playerNum, Block block) {
        PowerUp hit = null;
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
                        if (p.getPlayerNum() == playerNum && p.getCol() == boardX && p.getRow() == boardY) {
                            hit = p;
                            toRemove.add(p);
                        }
                    }
                }
            }
        }
        activePowerUps.removeAll(toRemove);
        return hit;
    }

    private void executePlayerSwap() {
        String[][] tempGrid = deepCopy(p1Board.getGrid());
        p1Board.overwriteGrid(rotateGrid180(p2Board.getGrid()));
        p2Board.overwriteGrid(rotateGrid180(tempGrid));

        int change = Math.abs(p1Board.getHeight()-p2Board.getHeight());
        if (change > 0 && p1Board.getHeight() > p2Board.getHeight()) {
            listeners.forEach(l -> l.onBoardSizeChange(1,change,getSnapshot()));
            for(int i=0; i<2; i++) {
                if(p2ActiveBlocks[i] != null) p2ActiveBlocks[i].setY(p2ActiveBlocks[i].getY()+change);
            }
        }
        else if(change > 0 && p2Board.getHeight() > p1Board.getHeight()) {
            listeners.forEach(l -> l.onBoardSizeChange(2,change,getSnapshot()));
            for(int i=0; i<2; i++) {
                if(p1ActiveBlocks[i] != null) p1ActiveBlocks[i].setY(p1ActiveBlocks[i].getY()-change);
            }
        }

        Block[] tempBlocks = p1ActiveBlocks;
        p1ActiveBlocks = p2ActiveBlocks;
        p2ActiveBlocks = tempBlocks;

        for (int i=0; i<2; i++) {
            if(p1ActiveBlocks[i] != null) rotateBlock180(p1Board,p1ActiveBlocks[i]);
            if(p2ActiveBlocks[i] != null) rotateBlock180(p2Board,p2ActiveBlocks[i]);
        }

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

        List<PowerUp> swappedPowerUps = new ArrayList<>();
        for (PowerUp p : activePowerUps) {
            PowerUp temp = null;
            if (p.getPlayerNum() == 1){
                temp = new PowerUp(1,Math.abs(p1Board.getHeight()-p.getRow()), Math.abs(p1Board.getWidth()-p.getCol()),p.getSpawnTime(),p.getType(),false);
            }
            else {
                temp = new PowerUp(1,Math.abs(p2Board.getHeight()-p.getRow()), Math.abs(p2Board.getWidth()-p.getCol()),p.getSpawnTime(),p.getType(),false);
            }
            swappedPowerUps.add(temp);
        }
        activePowerUps.clear();
        listeners.forEach( l -> l.clearPowerUps());
        activePowerUps.addAll(swappedPowerUps);
        listeners.forEach(l -> {
            l.onPowerUpSpawned(getSnapshot());
            l.onBlockMovement(getSnapshot(),1);
            l.onBlockMovement(getSnapshot(),2);
        });

        for(int i=0; i<2; i++) {
            if (p1ActiveBlocks[i] != null && !p1Board.isValidPosition(p1ActiveBlocks[i], isTwoBlockMode ? p1ActiveBlocks[(i + 1) % 2] : null)) {
                p1Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(1, snapLost));
            }
            if (p2ActiveBlocks[i] != null && !p2Board.isValidPosition(p2ActiveBlocks[i], isTwoBlockMode ? p2ActiveBlocks[(i + 1) % 2] : null)) {
                p2Lost = true;
                GameState snapLost = getSnapshot();
                listeners.forEach(l -> l.onPlayerLost(2, snapLost));
            }
        }
    }

    private void rotateBlock180(Board board,Block activeBlock) {
        activeBlock.setX((board.getWidth() - 1) - (activeBlock.getX() + activeBlock.getShape().length - 1));
        activeBlock.setY( (board.getHeight() - 1) - (activeBlock.getY() + activeBlock.getShape()[0].length - 1));

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

    private void spawnNewBlock(int playerNum, int index) {
        boolean bomb = !possibleBombs.isEmpty() && RANDOM.nextInt(1, 100) > 70;
        if (bomb){
            BombType selectedType = possibleBombs.get(RANDOM.nextInt(0, possibleBombs.size()));
            BombBlock newBombBlock = new BombBlock(selectedType);

            // Prevent collision in two block mode
            if (isTwoBlockMode) {
                newBombBlock.setX(index == 0 ? 1 : 6);
            } else {
                newBombBlock.setX(3); // Middle spawn
            }

            if (playerNum == 1){
                p1ActiveBlocks[index] = newBombBlock;
                checkValidPosition(playerNum, index);
            } else {
                while (newBombBlock.getY() < p2Board.getHeight() - newBombBlock.getShape()[0].length) {
                    newBombBlock.moveDown();
                }
                p2ActiveBlocks[index] = newBombBlock;
                checkValidPosition(playerNum, index);
            }
        } else {
            refillBlocks(playerNum);
            Block newBlock = null;
            List<Block> queue = (playerNum == 1) ? p1NextActiveBlock : p2NextActiveBlock;
            List<TBlock> blocks = (playerNum == 1) ? p1Blocks : p2Blocks;

            if (queue.isEmpty()) {
                newBlock = blocks.getFirst().toPiece();
                blocks.removeFirst();
            } else {
                newBlock = queue.getFirst();
                if (playerNum == 2) {
                    newBlock.setY(0);
                }
                queue.removeFirst();
            }

            // Adjust spawn X to prevent collision in Two Block Mode
            if (isTwoBlockMode) {
                newBlock.setX(index == 0 ? 1 : 6);
            } else {
                newBlock.setX(3);
            }

            if (playerNum == 1) {
                p1ActiveBlocks[index] = newBlock;
                checkValidPosition(playerNum, index);
            } else {
                while (newBlock.getY() < p2Board.getHeight() - newBlock.getShape()[0].length) {
                    newBlock.moveDown();
                }
                p2ActiveBlocks[index] = newBlock;
                checkValidPosition(playerNum, index);
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

    private void checkValidPosition(int playerNum, int index){
        if (playerNum == 1) {
            Block other = isTwoBlockMode ? p1ActiveBlocks[(index + 1) % 2] : null;
            if (!p1Board.isValidPosition(p1ActiveBlocks[index], other)) {
                p1Lost = true;
                listeners.forEach(l -> l.onPlayerLost(1, getSnapshot()));
            }
        } else {
            Block other = isTwoBlockMode ? p2ActiveBlocks[(index + 1) % 2] : null;
            if (!p2Board.isValidPosition(p2ActiveBlocks[index], other)) {
                p2Lost = true;
                listeners.forEach(l -> l.onPlayerLost(2, getSnapshot()));
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
        Block[] p1Clones = new Block[2];
        Block[] p2Clones = new Block[2];
        if (p1ActiveBlocks[0] != null) p1Clones[0] = p1ActiveBlocks[0].cloneForSnapshot();
        if (p1ActiveBlocks[1] != null) p1Clones[1] = p1ActiveBlocks[1].cloneForSnapshot();
        if (p2ActiveBlocks[0] != null) p2Clones[0] = p2ActiveBlocks[0].cloneForSnapshot();
        if (p2ActiveBlocks[1] != null) p2Clones[1] = p2ActiveBlocks[1].cloneForSnapshot();

        return new GameState(
                deepCopy(p1Board.getGrid()), deepCopy(p2Board.getGrid()),
                p1Clones, p2Clones,
                p1Score, p2Score, p1Level, p2Level, p1Name, p2Name, p1Lost, p2Lost, isGameOver, new ArrayList<>(activePowerUps), isTwoBlockMode
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
            Block[]    p1ActiveBlocks,
            Block[]    p2ActiveBlocks,
            int        p1Score,
            int        p2Score,
            int        p1Level,
            int        p2Level,
            String     p1Name,
            String     p2Name,
            boolean    p1Lost,
            boolean    p2Lost,
            boolean    gameOver,
            List<PowerUp> powerUps,
            boolean    isTwoBlockMode
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

        spawnNewBlock(1, 0);
        spawnNewBlock(2, 0);

        if (isTwoBlockMode) {
            spawnNewBlock(1, 1);
            spawnNewBlock(2, 1);
        }

        GameState snapReset = getSnapshot();
        listeners.forEach(l -> l.onReset(snapReset));
    }
}
