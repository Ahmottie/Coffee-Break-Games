# AI Usage Disclosure — Suleyman's Contributions

This file documents — section by section — every part of my contributions to
the project where I used an LLM. It is intended to satisfy the project page's
disclosure requirement: *"Parts that are not your own work should be clearly
marked in a separate file and properly referenced. Every group member must be
able to understand and explain the code on request."*

---

## 1. Why an LLM was used

I came into this project with no prior Java experience (my background is PHP,
JavaScript, TypeScript, and a little Go). The team needed a LAN networking
implementation in roughly 5 working days alongside other coursework. I used
the LLM as a tutor + pair-programmer:

- to translate familiar concepts (TypeScript interfaces, Go goroutines) into
  Java equivalents (interfaces, threads + `Platform.runLater`);
- to suggest an architecture for the network layer that I could then type
  out, inspect, and modify;
- to debug runtime issues (one bug I would not have found without it: an
  empty `addListener` body in the engine that broke all UI updates in LAN
  mode);
- to coordinate git workflow (branch, rebase, conflict resolution).

I did **not** use the LLM to write project documents/reports beyond this
file and the project README, and I did not paste in code I had not read.

---

## 2. Files I authored with AI assistance

The boundary between "AI-suggested" and "mine" varies per file. The
following table is the headline; details follow below.

| File | Created from scratch with AI help? | What I changed / understood myself |
| --- | --- | --- |
| `network/NetworkListener.java`  | Yes | Trivial interface; concepts (default methods) explained to me |
| `network/NetworkLayer.java`     | Yes | Public surface for the LAN connection; `AutoCloseable` extension |
| `network/Lan.java`              | Yes | IP discovery utility; I fixed a missing-`return` bug myself |
| `network/GameMessage.java`      | Yes | Sealed interface protocol; I rewrote comments in my own words |
| `network/Session.java`          | Yes | Cross-screen state holder (singleton-like) |
| `network/LanSession.java`       | Yes — most complex file | Read-loop + heartbeat + disconnect logic |
| `network/LanHost.java`          | Yes | Server-socket accept wrapper |
| `network/LanClient.java`        | Yes | Client-socket connect wrapper |
| `module-info.java` (additions)  | AI-suggested edits | One-line `exports` additions; I resolved a merge conflict on this file myself |
| `Memory/Controller/HostLan.java` (changes) | AI-suggested edits | Inserted ~10 lines to populate `Session` and start `LanHost.hostAsync` |
| `Memory/Controller/JoinLan.java` (changes) | AI-suggested edits | Replaced empty `connectToHost` stub with `LanClient.join`; reorganised `try/catch` |
| `Memory/Controller/WaitForOpponent.java`   | Rewritten with AI | Hello / LobbyConfig handshake, Ready toggle, countdown, navigation |
| `Memory/Controller/GameScreen.java` (refactor) | AI-suggested edits | Replaced `LocalGame` with `GameEngineImpl`; added engine listener; LAN broadcast/receive with echo guard; active-player gate |
| `Memory/Controller/ResultScreen.java` (changes) | AI-suggested edits | LAN-aware "Play Again" using a new `NewGame` protocol message; disconnect handling |
| `src/test/java/.../network/LanSessionTest.java` | AI-drafted | Two integration tests (roundtrip Hello, disconnect detection) |
| `README.md` (full rewrite) | AI-drafted, I revised | Replaced template README with project README |

The two engine test files I drafted for Amir (`GameEngineImplTest.java` and
`DecksTest.java`) are also AI-assisted; they are not in the repository at
submission time — they were handed to Amir to integrate as his own
contribution. He should disclose them under his own AI-usage policy if any.

---

## 3. Detailed breakdown by file

### 3.1 `network/NetworkListener.java`

- **AI**: suggested the four-callback shape (`onConnected`,
  `onMessage`, `onDisconnected`, `onError`) with `default {}` bodies so
  implementers override only what they need.
- **Concepts explained to me**: Java `interface` vs `class`, the `default`
  keyword (Java 8+), `Throwable` as the root error type. I had previously
  confused interfaces with classes and got a compile error on my first
  attempt; the assistant identified the typo (`class` instead of
  `interface`).
- **Mine**: I typed the file in IntelliJ, fixed the class/interface mistake
  after the explanation, and I can explain the rationale for default
  methods.

### 3.2 `network/NetworkLayer.java`

- **AI**: suggested `extends AutoCloseable` so the layer participates in
  try-with-resources, and the `@Override void close()` re-declaration to
  narrow the throws clause.
- **Concepts explained**: abstract methods (semicolon vs `{}` body),
  `extends`/`implements`, `@Override` annotation usefulness.
- **Mine**: file authored in IntelliJ; I can explain why `AutoCloseable`
  matters (deterministic resource release) and how it differs from the
  Go `defer` pattern I knew before.

### 3.3 `network/Lan.java`

- **AI**: drafted `localIp()` (network-interface enumeration filtered to
  IPv4 non-loopback non-virtual) and `isValidIp()`.
- **Bug I caught and fixed myself**: the assistant's first version returned
  `"unknown"` *inside* the catch block, leaving no return on the success
  path. The compiler error "Missing return statement" was mine to
  diagnose and the relocation of the `return` to outside the `try` was a
  correction I applied.
- **Concepts explained**: `static`, `final`, the private-constructor
  pattern for utility classes, checked exceptions, `instanceof` pattern
  matching.

### 3.4 `network/GameMessage.java`

- **AI**: introduced **sealed interfaces** (Java 17+) and **records** as
  the Java equivalent of a TypeScript discriminated union, suggested the
  seven message records (`Hello`, `LobbyConfig`, `Ready`, `StartCountdown`,
  `Flip`, `Heartbeat`, `Disconnect`) and one added later (`NewGame`).
- **One conceptual correction I needed**: I wrote in an early comment that
  `sealed` "allows the interface to be implemented anywhere" — the
  opposite of what it does. The assistant flagged this and I rewrote the
  comment in my own words to reflect that `sealed` *restricts* which
  classes may implement.
- **Mine**: file typed in IntelliJ, comments rewritten in my own words.
  I can defend the rationale: closed protocol surface, deterministic
  switch handling, `Serializable` for the wire format.

### 3.5 `network/Session.java`

- **AI**: suggested the lazy singleton pattern (`current()` + `clear()`)
  and a list of public fields to hold cross-screen state.
- **Mine**: I added an informal comment ("makes the network connection
  go bum bum" — which I would explain in a viva as a casual note that the
  `clear()` method tears down the socket).
- **Concepts explained**: `static` fields, lazy initialisation, why we
  used direct public fields rather than getters/setters for this
  intentionally-small-scope holder.

### 3.6 `network/LanSession.java`  ← largest single file by AI involvement

This file is the engine room of the network package: socket I/O, two
background threads (reader + heartbeat), disconnect detection,
listener fan-out. **Most of the structure was AI-suggested** because the
patterns (`CopyOnWriteArrayList`, `AtomicBoolean.compareAndSet`,
`ObjectOutputStream` construction ordering, `SocketTimeoutException`
handling) are not patterns I had used before.

Key things I now understand and can explain after working through it:

- **The OOS/OIS ordering deadlock.** Both ends must build the
  `ObjectOutputStream` first and flush, then build the
  `ObjectInputStream`. Reversing this order deadlocks because
  `ObjectInputStream`'s constructor blocks until it reads a stream
  header. The assistant explained this before I wrote the constructor.
- **`out.reset()` after every send.** Without this, `ObjectOutputStream`
  caches references it has already serialised and sends a "you've seen
  this" back-reference for repeated `Flip(cardId)` instances. I would
  not have found this on my own.
- **`compareAndSet(false, true)` for one-shot disconnect.** Why
  `AtomicBoolean.compareAndSet` instead of a plain check + set: ensures
  the cleanup block runs exactly once even if `disconnect(...)` is
  called concurrently from the read loop and a peer's graceful-close.
- **Daemon threads.** So the JVM can exit even if the read loop is
  blocked on `readObject()`.
- **`SO_TIMEOUT` = 5000 ms.** Bounds the worst-case disconnect detection
  to 5 seconds even when the peer disappears without a TCP FIN (e.g.
  `kill -9`).

I typed every line of this file. The structure is the assistant's; the
diagnostic comments are mostly mine.

### 3.7 `network/LanHost.java` and `network/LanClient.java`

- **AI**: suggested the synchronous + async pair pattern
  (`host(port)` / `hostAsync(port, onReady, onError)` and equivalent for
  the client). The async wrappers spawn a daemon thread so we don't
  block the JavaFX thread on `accept()` / `connect()`.
- **Mine**: I typed both files. I can explain why the constructor of
  `Socket` without a `connect(addr, timeout)` would hang ~30 s on
  unreachable IPs vs. our 3-second timeout. Also why a `ServerSocket`
  is only needed for the initial handshake (it's closed by
  try-with-resources after `accept()`).

### 3.8 `src/test/java/.../network/LanSessionTest.java`

- **AI**: drafted two JUnit 5 tests:
  - `roundtripHello` — boots a host on a background thread, connects a
    client, exchanges `Hello`, asserts both sides receive.
  - `disconnectFiresWhenPeerCloses` — verifies the 5-second timeout path.
- **Concepts explained**: `@Test`, `CountDownLatch`, `AtomicReference`,
  anonymous classes for one-off listener implementations, static
  imports for assertion methods.
- **Mine**: ran the tests in IntelliJ, verified both pass repeatedly. I
  can run and explain them under examination.

---

### 3.9 Controller integrations (`HostLan`, `JoinLan`, `WaitForOpponent`)

For each of these the assistant proposed concrete diffs against existing
files. I applied them in IntelliJ, ran the app, and iterated when the
behaviour wasn't right. The key changes by file:

**`HostLan.onSearchAction()`** — added 4 imports and ~10 new lines after
the existing parsing to:

- build a `GameConfig`,
- call `Decks.prepare(config)` to generate the deck and choose the first
  player,
- stash everything in `Session.current()` so the lobby and game screens
  can read it,
- start `LanHost.hostAsync(...)` on a background thread.

The navigation to `WaitForOpponent` already existed (Eric's code); I only
inserted the data-staging block.

**`JoinLan.onConnectAction()`** — replaced an empty `connectToHost(...)`
stub Eric had left with a real call to `LanClient.join(...)`. Restructured
the surrounding `try` block so the navigation only happens on successful
connect (Eric's original navigated unconditionally, which was a bug we
identified together).

**`WaitForOpponent` (rewrite)** — this file was largely rewritten because
the original was a UI shell with hardcoded test stubs (e.g. an opponent
named "Peter" appearing after a few clicks). The new version:

- shows host IP via `Lan.localIp()` (FR-LAN-02),
- registers a `NetworkListener` to handle `Hello → LobbyConfig` on the
  host side and `LobbyConfig` on the client side,
- toggles ready state via `Ready` messages,
- starts a 3-second countdown via `StartCountdown` when both sides are
  ready,
- navigates to `GameScreen` after the countdown via `Platform.runLater`
  and a `PauseTransition`.

I drove the design decisions: e.g. choosing a *relative* `delayMs`
(rather than an absolute timestamp) for the countdown because the two
machines have independent clocks. The assistant flagged this as a
gotcha; I now own that reasoning.

### 3.10 `GameScreen.java` refactor

The single biggest single-file change in my work. The original used a
parallel implementation called `LocalGame` for the local mode and did not
use Amir's `GameEngineImpl` at all. To enable LAN play I refactored the
controller to drive the UI from engine events.

**AI**: suggested the structure — a `GameEventListener` implementation
that drives the existing animations (`turnCardsBack`, `removeMatch`,
`awardPoints`, `gameEnd`) in response to engine callbacks
(`onMatch`, `onMismatch`, `onTurnChanged`).

**My role**: I applied the refactor incrementally, hit two bugs that I
debugged with the assistant's help:

1. The `flippedCards.clear()` call was placed in the listener body
   *before* the 1.5 s pause used by `turnCardsBack`. By the time the
   pause fired, the list was empty and no cards flipped back. We
   removed the early clear because `turnCardsBack` already clears
   internally after its pause.
2. **The empty `addListener` bug in Amir's `GameEngineImpl`.** I noticed
   the listener never fired in LAN mode and added diagnostic println
   statements. The console showed `engine.flip(...)` executing
   (active-player switched) but no listener events. We then read
   `GameEngineImpl.java` and found `addListener` had a body of `{}`
   — i.e. listeners were registered into thin air. I added the
   missing two-line body. This was a meaningful debugging contribution
   I can describe in detail.

**LAN bridging** (added after the listener refactor):

- An `applyingRemote` echo guard initially implemented as a boolean; the
  assistant pointed out it would reset before the animation finished and
  caused remote flips to be re-broadcast. The replacement is a
  `ConcurrentHashMap.newKeySet()` of card-ids being applied remotely;
  each `Flip` consumes its marker exactly once.
- Active-player gating in the click handler: `if
  (Session.current().network != null
  && !engine.getActivePlayer().equals(myName)) return;` — only in LAN
  mode; local mode still lets both players share the keyboard.

### 3.11 `ResultScreen.java` changes

Eric had built local-mode "Play Again" already, with a fall-through bug on
the draw case (`switch` `case 0` missing `break`). The LAN mode needed a
new protocol message `NewGame(config, setup)` that the host generates and
broadcasts. The client receives it and follows.

The fall-through bug was identified by reading the existing code together.
The LAN protocol message was the assistant's suggestion; I wrote the
controller wiring and the network listener attachment.

### 3.12 `README.md`

Largely AI-drafted (project description, build/run instructions, LAN
setup, project layout, troubleshooting). I revised the tone and added a
few corrections, including the team table.

---

## 4. Bug fixes and design decisions

A few moments worth highlighting because they go beyond "AI typed code,
I pasted it":

1. **Engine package move conflict.** Eric had moved `engine/` to
   `Memory/engine/` on a stale branch that was missing three of Amir's
   newest files. The assistant flagged the missing files in his diff
   before merge; I coordinated with the team about the package location
   and resolved the resulting `module-info.java` conflict during a
   rebase.

2. **`GameConfig.Player2Name` → `player2Name`.** A typo Amir had left;
   we discussed and fixed before any serialisation depended on it.

3. **Selective disclosure conversation.** When initially considering a
   partial disclosure of AI usage, the assistant pushed back and
   recommended this complete-disclosure approach instead. This document
   is the result.

4. **Architectural decision on the local-vs-LAN engine split.** Both
   modes now use Amir's `GameEngineImpl`. The decision to refactor
   rather than maintain two parallel engines was the team's; the
   assistant laid out the alternatives and I made the call.

---

## 5. Workflow / tooling assistance

These weren't code per se but I used the assistant for:

- IntelliJ Maven workflow (where to find the Maven tool window, how to
  run `clean package` without a system `mvn`).
- Git workflow: creating feature branches, merging, resolving the merge
  conflict on `module-info.java`, dealing with a divergent `main`/local
  state after committing to the wrong branch.
- Diagnosing the missing `maven-shade-plugin` configuration when
  attempting to produce a runnable fat JAR for submission.

These are operational/learning items rather than code, but listed here
for completeness.

---

## 6. What I learned and can explain

Areas I am now confident defending in a viva:

- The structure and behaviour of the LAN protocol I implemented (the
  seven `GameMessage` records and the host/client message flow).
- Threading model: JavaFX Application Thread vs. background reader vs.
  heartbeat thread; why `Platform.runLater(...)` is required when a
  background thread touches the engine or UI.
- Why `ObjectOutputStream.reset()` is critical after every send.
- The 5-second disconnect detection mechanism (heartbeat interval +
  `SO_TIMEOUT`) and why it satisfies QR-RELY-04 in both graceful and
  ungraceful disconnect cases.
- The `applyingRemote` / echo-prevention pattern in the LAN bridge.
- The deterministic-replay property that makes LAN sync work without
  shipping board state in every message: host runs `Decks.prepare`
  once, both engines start from the same `GameSetup`, identical flip
  sequences produce identical state.

Areas where I was actively guided by the AI and which I would describe
honestly as AI-supported under questioning:

- Specific Java syntax for sealed interfaces and records, including the
  `permits` clause.
- The `CopyOnWriteArrayList` and `AtomicBoolean.compareAndSet` choices
  in `LanSession`.
- The `maven-shade-plugin` configuration (added locally to produce the
  submission JAR; not committed).

---

## 7. Tetris LAN — dual-engine optimisation (later work)

A separate, later task on the `feature/suleyman-lan-optimization` branch.
The Tetris LAN mode was **host-authoritative**: the host owned the only
engine and simulated *both* boards, while the client was a thin terminal
that sent key inputs and rendered snapshots echoed back from the host.
Every client move round-tripped to the host before the client saw it,
which caused noticeable input lag on the client side.

**What I changed (AI-assisted, phased):** I reworked it into a
**dual-engine** model — each machine runs its own `TetrisEngine` and
simulates **only its own board**, so the local player's input is applied
instantly with no round-trip. The opponent's board is display-only, drawn
from snapshots the peer sends. Because each board has exactly one
authoritative simulator, the unseeded RNG never has to match between
machines.

Because the two boards interact (power-ups, bombs, board-size pressure),
every cross-board effect became an explicit network message instead of a
direct field write. New `TetrisMessage` types: `BoardState`, `PlayerLost`,
`Attack`, `SettingsSync`, `PortalBlock`, `BoardShrink`, and the
`SwapActive*` / `SwapBoards*` request/response handshakes. I rolled it out
in phases and tested each over two windows before moving on:

1. **Core dual-engine** — both sides build an engine, each ticks/inputs
   only its own player; game-over reached by a `PlayerLost` consensus
   (the original "both must top out" rule preserved).
2. **Networked power-ups** — self effects apply locally; opponent effects
   (speed up/down, rotation delay) are sent as `Attack`s the peer applies
   to its own board. Also added a host→client `SettingsSync` so the
   client builds its engine from the **host's** advanced settings (it was
   using local defaults, so nothing spawned client-side).
3. **PORTAL** — one-way block transfer into the peer's spawn queue.
4. **Bombs** — re-enabled as-is (self-contained: a bomb only clears the
   bomber's own board).
5. **Board-change** — clearing lines grows my board and sends a
   `BoardShrink` so the peer shrinks theirs.
6. **Swap active blocks / swap boards** — two-way handshakes that exchange
   blocks / full grids (with the 180° rotation the original used).

**Bugs I found and fixed during this work** (the kind I can explain):

- A `NullPointerException` on game-over: the shared `onStopped`/disconnect
  cleanup called `p2EngineTicker.stop()`, but in dual mode each machine
  only creates *one* ticker, so the other was `null`. The host crashed
  before navigating to the result screen while the client didn't —
  diagnosed from the stack trace, fixed by null-checking each ticker.
- A disconnect crash on **Exit** from the result screen: closing the
  socket fired our *own* `onDisconnected`, which then ran `changeScene`
  after we'd already navigated away (detached scene → null window). Fixed
  by suppressing the self-disconnect on intentional exit and skipping the
  navigation when already detached.

**Files (Tetris-only):** `Tetris/Engine/TetrisEngine.java`,
`TetrisEventListener.java`, `TetrisAdvancedSettings.java`,
`KeyHandler.java`, `Tetris/network/TetrisMessage.java`, and the three
`Tetris/Controller` screens (`GameScreen`, `WaitForOpponent`,
`ResultScreen`); plus two new files `Tetris/network/TetrisNet.java`
(a `DUAL_ENGINE` rollout flag) and `Tetris/Engine/AttackType.java`. The
shared `network/` package, `Session`, and the Memory/HexChess games were
**not** touched (verified by diff + a full clean rebuild).

