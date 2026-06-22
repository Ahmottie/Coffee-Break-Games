# Coffee Break Games — Memory Game

A two-player memory card game built as our SEDA Masters project. Players take
turns flipping cards on a shared board and try to find matching K-tuples
(K cards sharing the same symbol). The player with the most points when the
board is empty wins.

The game supports two modes:

- **Local play** — two players share one machine and one keyboard/mouse.
- **LAN play** — two players on the same Wi-Fi or LAN, one hosts and the
  other joins by typing the host's IP address.

The rules for this game are the same in Local and Lan mode.

## Team

| Person   | Area                                              |
|----------| ------------------------------------------------- |
| Amir     | Game engine — match logic, scoring, deck setup    |
| Erik     | UI / FXML controllers, animations, local play     |
| Suleyman | LAN networking — sockets, lobby sync, disconnect  |

## Requirements

- **Java 24 or newer** (the project uses Java 24 language features and a
  module-info.java).
- **Maven 3.9+** (or use IntelliJ's bundled Maven — see below).
- For LAN play: both machines on the same local network, with TCP port
  **8765** reachable.

## Build

From the repo root:

```sh
mvn clean package
```

This compiles the project, runs the unit tests, and writes the build outputs
into `target/`. You should see two JAR files there:

- `GameBox.jar` — the plain artifact (depends on JavaFX being on the module
  path at runtime).
- `GameBox-jar-with-dependencies.jar` — fat JAR with dependencies bundled,
  intended for direct execution with `java -jar`.

## Run

The simplest way during development is the JavaFX Maven plugin:

```sh
mvn javafx:run
```

Or run the fat JAR directly:

```sh
java -jar target/GameBox-jar-with-dependencies.jar
```

If you launch from IntelliJ instead, use the green ▶ next to `Launcher.java`
under `src/main/java/seda_project/control_alt_defeat/gamebox/`.

## How to play

### Local game

1. Start the application.
2. From the main menu choose **Local Game**.
3. Enter K (1–45). The system computes up to three valid deck sizes (the
   three largest multiples of K that do not exceed 45) and lets you pick one.
4. Optionally enter both player names. If left blank they default to
   "Player 1" and "Player 2".
5. Click **Start**. The board appears and the first player (chosen randomly)
   begins.

### LAN game

To play across two machines you need one host and one joining player.

#### Hosting

1. From the main menu choose **LAN Game → Host LAN Game**.
2. Enter your name, choose K and a deck size, click **Host**.
3. The lobby screen will show your **local IP address**. Tell that IP to the
   other player.
4. macOS / Windows may show a firewall prompt the first time the JVM listens
   on a port. **Allow incoming connections** — otherwise the joining player
   won't be able to connect.

#### Joining

1. From the main menu choose **LAN Game → Join LAN Game**.
2. Enter the host's IP address, your player name, and click **Connect**.
3. Both players land on the lobby screen and see each other's names plus the
   chosen K and deck size. When both players click **Ready**, a 3-second
   countdown starts and then the game begins on both machines simultaneously.

#### During the game

- Only the active player can flip cards. The inactive client's clicks are
  ignored until the turn changes.
- Every flip is broadcast to the other machine within ~500 ms; both boards
  stay identical the whole time.
- A mismatch keeps the wrong cards visible for 1.5 seconds (the
  `MISMATCH_DELAY_MS` constant in the engine) before flipping back, so both
  players have time to memorise them.

#### Disconnections

If the connection drops mid-game (someone closes the app, the Wi-Fi cuts
out, etc.), the other player sees an alert within at most 5 seconds and is
returned to the main menu. There is no automatic reconnect.

### After the game

When the board is empty the result screen shows both names, both final
scores, and the outcome (win / lose / draw). From there you can:

- **New Game** — keeps the same K, deck size, and player names. Starts a
  fresh game without going back through the configuration screen. In LAN
  mode the host's button initiates this; the other side just follows along.
- **Return to Menu** — quits the current session and goes back to the main
  menu.

## Project layout

```
src/main/java/seda_project/control_alt_defeat/gamebox/
├── GameBox.java             JavaFX Application entry
├── Launcher.java            main()
├── Memory/
│   ├── Configuration.java   helper for deck-size computation, scene loading
│   ├── ViewStack.java       FXML scene navigation stack
│   ├── Controller/          FXML controllers (one per screen)
│   └── engine/              Amir's package: GameEngine + records, Decks,
│                            GameEngineImpl, GameEventListener
├── network/                 our LAN networking layer
│   ├── NetworkLayer.java    public interface for either side of a session
│   ├── NetworkListener.java callback interface
│   ├── GameMessage.java     sealed protocol (Hello, LobbyConfig, Ready,
│                            StartCountdown, Flip, Heartbeat, Disconnect,
│                            NewGame)
│   ├── LanSession.java      socket reader + heartbeat + disconnect
│   ├── LanHost.java
│   ├── LanClient.java
│   ├── Lan.java             port + IP utilities
│   └── Session.java         cross-screen state holder
└── ui/                      misc UI helpers (MCard, etc.)

src/main/resources/Views/Memory/    FXML layouts
src/test/java/                       unit + integration tests
docs/                                requirements.pdf
```

## Tests

We use JUnit 5. Run them with:

```sh
mvn test
```

The test classes currently in the repo:

- `gamebox.network.LanSessionTest` — verifies the socket layer and the
  5-second peer-disconnect detection over loopback.
- `gamebox.ExampleTest` — placeholder smoke test from the template.

## Known limitations / non-goals

These are intentionally out of scope (see `docs/requirements.pdf`, section
2.2):

- No single-player or AI opponent.
- No internet play, only LAN.
- No automatic host discovery — the joining player has to type the IP.
- No persistent player accounts or cross-session leaderboard.
- No mobile or web client.

## Troubleshooting

**"Cannot resolve symbol" on JavaFX classes after pulling.** Run
`mvn clean compile` once. IntelliJ sometimes loses the JavaFX module path
after a Maven dependency change.

**"Connection refused" when joining.** Either the host hasn't reached the
lobby screen yet (the server socket only opens once they click Host), or
the firewall is blocking inbound TCP on port 8765, or the two machines are
not actually on the same network.

**Lobby labels show "Label" forever on the joining side.** Means the host
didn't receive the join's `Hello` or didn't send back `LobbyConfig`. Check
the firewall first; if both machines saw each other's TCP handshake, then
look at the console on the host for exceptions.

**Disconnect alert fires too quickly.** That's actually correct — when a
peer closes the socket cleanly, the OS sends a TCP FIN immediately and the
remaining side detects it right away. The 5-second timeout is the fallback
for ungraceful disconnects (cable pulled, process force-killed).

## Chess
Pieces from: https://greenchess.net/info.php?item=downloads
Mug from: https://www.cgtrader.com/items/3536740/download-page
3d pieces from: https://free3d.com/3d-model/chess-19650.html

## Images
* Application Icon: https://www.svgrepo.com/svg/211920/coffee-grain-seed?edit=true

## Sounds
* Chess Pieces: https://pixabay.com/sound-effects/film-special-effects-chess-pieces-60890/
* Chess Background: https://pixabay.com/music/video-games-music-for-puzzle-game-146738/

* Tetris Background: https://pixabay.com/music/classical-string-quartet-tetris-theme-korobeiniki-rearranged-arr-for-strings-185592/
* Tetris Lineclear: https://pixabay.com/sound-effects/film-special-effects-clear-notification-355970/
* Tetris Piecesnap: https://pixabay.com/sound-effects/film-special-effects-finger-snap-179180/
* Tetris Levelup: https://pixabay.com/sound-effects/film-special-effects-level-up-05-326133/
* Tetris Radialbomb: https://pixabay.com/sound-effects/bomb-explosion-469038/
* Tetris Columnbomb: https://pixabay.com/sound-effects/film-special-effects-drill-spinning-in-open-air-33575/

* Memory Background: https://pixabay.com/music/solo-piano-retro-arcade-game-music-491487/
* Memory Match: https://pixabay.com/sound-effects/film-special-effects-sample-confirm-success03-kofi-by-miraclei-360151/
* Memory Missmatch: https://pixabay.com/sound-effects/film-special-effects-fail-234710/


* Button press: https://pixabay.com/sound-effects/film-special-effects-button-press-382713/
* Lobby Background: https://pixabay.com/music/ambient-coffee-morning-coffee-shop-music-499186/

* Konami EasterEgg: https://www.vgmpf.com/Wiki/index.php?title=Nemesis_(ARC)

