package seda_project.control_alt_defeat.gamebox.network;

import java.io.Serializable;

record AdPayload(String name, int tcpPort, int level, GameMode gameMode) implements Serializable {}
