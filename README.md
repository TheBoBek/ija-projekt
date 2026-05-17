# IJA 2025/26 Project – Tým xmarina00

Course project inspired by Advance Wars.

## Assignment Snapshot

- Build a turn-based strategy game on an `M x N` grid with terrain, units, economy, combat, capture, and replay.
- Keep the game engine separate from the user interface.
- Support at least one simple AI opponent and a `Bot vs. Bot` mode.
- Use git during the whole project.

## Repository Layout

- `src/main/java/ija/game/` contains the main game logic.
- `src/main/java/ija/ui/` contains the JavaFX desktop UI.
- `src/main/java/ija/command/` contains commands and command history.
- `src/main/java/ija/bot/` contains bot logic.
- `maps/` contains bundled scenarios.
- `assets/terrain/` contains terrain and unit sprites.

## UI Snapshot

- The project has a JavaFX desktop UI.
- The board is shown as a clickable grid.
- Bundled scenarios are loaded from `maps/`.
- You can select a unit, see movement range, and open action menus.
- Actions in the UI include `Attack`, `Capture`, `Wait`, and factory buying.
- The UI shows current turn, current funds, and an event log panel.
- The project includes `Dummy Bot`, `Bot vs. Bot`, and replay controls.

## Requirements

- `Java 21`
- `Maven`

## How to Run

- The project uses Maven and JavaFX.
- To start the game, run `mvn -q -DskipTests javafx:run`.
- The main JavaFX class is already set in Maven, so no extra class name is needed.
- The game loads bundled scenarios from `maps/` when it starts.

## How to Play

- Click your unit to see movement range.
- Click a tile to move.
- After move, choose `Attack`, `Capture`, or `Wait`.
- Click your factory to buy a unit.
- End turn to switch player.
- Try `Dummy Bot`, `Bot vs. Bot`, and replay buttons.

## Testing

- The project is checked with `mvn test`.
- Automated tests cover movement, combat, economy, replay state, bot behavior, and integration.
- Small manual gameplay smoke testing in the team focused on launching the JavaFX game, loading a bundled scenario, selecting units, checking movement highlights, trying move and action menus, ending turns, trying basic factory buying, and briefly checking bot play and `Bot vs. Bot`.

