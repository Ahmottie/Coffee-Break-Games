package seda_project.control_alt_defeat.gamebox.engine;

public record Card(
        int id,
        int symbolId,
        boolean faceUp,
        boolean removed
) {}
