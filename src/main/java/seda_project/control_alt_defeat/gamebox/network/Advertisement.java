package seda_project.control_alt_defeat.gamebox.network;

import java.io.Serializable;

public record Advertisement(
        String name,
        int level,
        String ipAddress,
        int    tcpPort,
        long   lastSeenMs,
        GameMode gameMode,
        String boardState
) implements Serializable {}
