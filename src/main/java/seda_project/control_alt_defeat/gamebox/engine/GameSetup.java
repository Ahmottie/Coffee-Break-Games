package seda_project.control_alt_defeat.gamebox.engine;

import seda_project.control_alt_defeat.gamebox.Memory.engine.Card;

import java.util.List;

public record GameSetup(
        List<Card> initialDeck,
        String firstPlayer
) implements java.io.Serializable {}