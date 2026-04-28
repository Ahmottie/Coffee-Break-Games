package seda_project.control_alt_defeat.gamebox.engine;

import java.util.List;

public record FlipResult(
        TurnResult result,
        List<Integer> matchedIds,
        String newActivePlayer,
        int scoreAwarded
) implements java.io.Serializable {}