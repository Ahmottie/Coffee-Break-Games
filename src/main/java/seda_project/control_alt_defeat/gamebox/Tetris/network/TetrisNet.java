package seda_project.control_alt_defeat.gamebox.Tetris.network;

/**
 * Tetris LAN rollout switches.
 *
 * DUAL_ENGINE selects the netcode model for a LAN match:
 *  - false : the old host-authoritative model. The host owns the single engine
 *            and simulates both boards; the client is a thin terminal that sends
 *            key inputs and renders snapshots echoed back from the host. The
 *            client's own moves round-trip to the host, which is the source of
 *            the input lag.
 *  - true  : the dual-engine model. Each machine runs its own engine and
 *            simulates ONLY its own board, so the local player's moves are
 *            applied instantly with no round-trip. Each side ships its own board
 *            snapshot to the opponent for display. The opponent board is never
 *            simulated locally, so the unseeded RNG never needs to match.
 *
 * The flag is intentionally not final so the old path stays available as a
 * fallback while the dual-engine path is rolled out phase by phase.
 */
public final class TetrisNet {
    private TetrisNet() {}

    public static boolean DUAL_ENGINE = true;
}
