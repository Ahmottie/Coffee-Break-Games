package seda_project.control_alt_defeat.gamebox.Tetris.Engine;

/**
 * A cross-board power-up effect that, in dual-engine LAN, is sent to the opponent
 * and applied by THEM to their own board (each machine simulates only its own
 * board, so an "opponent" effect can't be applied locally).
 *
 * The receiver applies the effect to its own player:
 *  - SPEED_UP       : opponent's board falls faster (a fast effect on self)
 *  - SPEED_DOWN     : opponent's board falls slower (a slow effect on self)
 *  - ROTATION_DELAY : opponent's rotations are delayed for a while
 */
public enum AttackType {
    SPEED_UP,
    SPEED_DOWN,
    ROTATION_DELAY
}
