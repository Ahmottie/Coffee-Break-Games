package seda_project.control_alt_defeat.gamebox.Memory.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Decks {

    private Decks() {}

    public static GameSetup prepare(GameConfig config) {
        // Calculate unique symbols
        int numSymbols = config.deckSize() / config.k();

        // Generate symbols
        List<Integer> symbolPool = new ArrayList<>();
        for (int symbolId = 0; symbolId < numSymbols; symbolId++) {
            for (int i = 0; i < config.k(); i++) {
                symbolPool.add(symbolId);
            }
        }

        // Shuffle symbols
        Collections.shuffle(symbolPool);

        // Build cards
        List<Card> initialDeck = new ArrayList<>();
        for (int i = 0; i < symbolPool.size(); i++) {
            initialDeck.add(new Card(i, symbolPool.get(i), false, false));
        }

        // Pick random player
        Random rand = new Random();
        String firstPlayer = rand.nextBoolean() ? config.player1Name() : config.player2Name();

        return new GameSetup(initialDeck, firstPlayer);
    }
}