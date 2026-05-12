package seda_project.control_alt_defeat.gamebox.Tetris.Enginge;

/*
Based on the functional requirements (FR-FLOW-08 to FR-FLOW-19)
the engine needs to handle the game
player inputs
detect collisions
clear lines
update score
determine the loser
*/

public class TetrisEngine {
    private final Board p1Board;
    private final Board p2Board;

    private Block p1ActiveBlock;
    private Block p2ActiveBlock;

    private int p1Score;
    private int p2Score;

    private final String p1Name;
    private final String p2Name;

    private boolean p1Lost;
    private boolean p2Lost;
    private boolean isGameOver;

    public TetrisEngine(String p1Name, String p2Name) {
        this.p1Name = p1Name;
        this.p2Name = p2Name;

        // Player 1 is standard, Player 2 is inverted (FR-OUTPUT-02)
        this.p1Board = new Board(false);
        this.p2Board = new Board(true);

        this.p1Score = 0;
        this.p2Score = 0;

        this.p1Lost = false;
        this.p2Lost = false;
        this.isGameOver = false;

        spawnNewBlock(1);
        spawnNewBlock(2);
    }

    /**
     * Called by a timer in the UI or LAN loop to apply gravity.
     */
    public void tick() {
        if (isGameOver) return;

        if (!p1Lost) {
            applyGravity(1, p1ActiveBlock, p1Board);
        }
        if (!p2Lost) {
            applyGravity(2, p2ActiveBlock, p2Board);
        }

        checkWinCondition();
    }

    private void applyGravity(int playerNum, Block activeBlock, Board board) {
        // Move block in the direction of gravity
        if (board.isInverted()) {
            activeBlock.moveUp();
        } else {
            activeBlock.moveDown();
        }

        // If the move was invalid (hit a wall or locked block), revert it and lock
        if (!board.isValidPosition(activeBlock)) {
            if (board.isInverted()) {
                activeBlock.moveDown(); // revert up
            } else {
                activeBlock.moveUp(); // revert down
            }

            board.lockBlock(activeBlock);

            // Clear lines and update score (FR-FLOW-13, FR-FLOW-14)
            int linesCleared = board.clearLines();
            if (playerNum == 1) {
                p1Score += calculateScore(linesCleared);
            } else {
                p2Score += calculateScore(linesCleared);
            }

            spawnNewBlock(playerNum);
        }
    }

    private void spawnNewBlock(int playerNum) {
        Block newBlock = BlockFactory.generateRandomStandardBlock();

        // If inverted, spawn at the bottom of the board
        if (playerNum == 2) {
            // Need a custom spawn Y for the bottom of the board
            while(newBlock.getY() < 19) {
                newBlock.moveDown();
            }
        }

        if (playerNum == 1) {
            p1ActiveBlock = newBlock;
            if (!p1Board.isValidPosition(p1ActiveBlock)) {
                p1Lost = true; // Board overflow (FR-FLOW-15)
            }
        } else {
            p2ActiveBlock = newBlock;
            if (!p2Board.isValidPosition(p2ActiveBlock)) {
                p2Lost = true; // Board overflow
            }
        }
    }

    /**
     * Standard scoring system based on lines cleared at once.
     */
    private int calculateScore(int linesCleared) {
        return switch (linesCleared) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800; // Tetris!
            default -> 0;
        };
    }

    /**
     * Process player inputs (Left, Right, Rotate, Soft Drop).
     * Call this from your UI Key Event handler.
     */
    public void processInput(int playerNum, String action) {
        if (isGameOver) return;
        if (playerNum == 1 && p1Lost) return;
        if (playerNum == 2 && p2Lost) return;

        Block block = (playerNum == 1) ? p1ActiveBlock : p2ActiveBlock;
        Board board = (playerNum == 1) ? p1Board : p2Board;

        switch (action) {
            case "LEFT":
                block.moveLeft();
                if (!board.isValidPosition(block)) block.moveRight();
                break;
            case "RIGHT":
                block.moveRight();
                if (!board.isValidPosition(block)) block.moveLeft();
                break;
            case "ROTATE":
                block.rotateClockwise();
                if (!board.isValidPosition(block)) block.rotateCounterClockwise();
                break;
            case "DROP":
                // Soft drop (manual gravity)
                applyGravity(playerNum, block, board);
                break;
        }
    }

    private void checkWinCondition() {
        if (p1Lost && p2Lost) {
            isGameOver = true; // Both lost simultaneously
        } else if (p1Lost || p2Lost) {
            // Allow the other player to continue playing (FR-FLOW-18)
            // If you want the game to end immediately when one loses, set isGameOver = true here.
        }
    }

    /**
     * Generates an immutable snapshot of the game for rendering and LAN transfer.
     */
    public GameState getSnapshot() {
        return new GameState(
                p1Board.getGrid(),
                p2Board.getGrid(),
                p1ActiveBlock,
                p2ActiveBlock,
                p1Score,
                p2Score,
                p1Name,
                p2Name,
                p1Lost,
                p2Lost,
                isGameOver
        );
    }
}