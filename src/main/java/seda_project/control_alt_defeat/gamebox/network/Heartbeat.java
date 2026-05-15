package seda_project.control_alt_defeat.gamebox.network;

public record Heartbeat(long sentAt) implements HeartbeatMessage {}
