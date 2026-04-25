package seda_project.control_alt_defeat.gamebox.engine;

public record GameConfig(
        int k,
        int deckSize,
        String player1Name,
        String Player2Name,
        long randomSeed
        ) {}
