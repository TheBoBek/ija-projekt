# Assignment Summary

This project is a turn-based strategy game inspired by Advance Wars. The assignment requires both functional gameplay and a clean software design.

## Core Gameplay Requirements

- The map is an `M x N` grid with terrain types from `terrain.tsv`: plain, forest, mountain, water, city, factory, and HQ.
- Mountains block vehicles, water blocks all units, cities generate `+1000` money per turn and heal units, factories buy units, and capturing an enemy HQ wins the game.
- Units come from `units.tsv`: infantry, tank, and artillery.
- Infantry can capture cities and HQ. Tanks and artillery cannot capture.
- Artillery attacks at range `2-3` and cannot move and fire in the same turn.

## Combat Rules

- Combat is deterministic.
- Base damage comes from `units-damage.tsv`.
- Damage depends on the current attacker HP and the defender terrain bonus.
- Counter-attacks happen automatically only if the defender survives and the attacker is inside defender range.
- A counter-attack uses the defender's reduced HP after taking damage.

## Turn Flow

Each player turn contains:

1. Income and repair phase.
2. Action phase for movement, combat, capture, and unit purchase.

Unit action flow:

1. Select a unit.
2. Compute valid movement using 4-neighbour pathfinding and terrain costs.
3. Move the unit, including zero-tile movement.
4. Offer context actions: `Attack`, `Capture`, `Wait`.
5. Mark the unit as spent for the turn.

## Movement and Occupancy Rules

- Use 4-direction adjacency only.
- Path cost is the sum of entered tiles, excluding the starting tile.
- Friendly units are pass-through but cannot be the final destination.
- Enemy units block movement completely.
- At most one unit may occupy a tile after movement.
- A factory tile cannot produce a unit if any unit already stands on it.

## Capture and Repair Rules

- Neutral or enemy cities/HQ need `20` capture points.
- Capture reduces those points by `floor(current_infantry_hp / 10)` each turn.
- Leaving the tile resets capture progress.
- At the start of the owner's turn, damaged units on owned city/factory/HQ heal by `+20 HP`, capped at `100`.
- Repairs cost money: `10%` of unit purchase cost per restored `10 HP`.

## Architecture and Technical Requirements

- The engine must be decoupled from the GUI using MVC.
- Logging is mandatory for turns, combat, and purchases.
- The game must support replay forward and backward from the log.
- The user must be able to switch from replay mode back into live game mode from any replayed state.
- Required design patterns explicitly called out in the assignment:
  - MVC
  - Command pattern for reversible actions / step back
  - Factory pattern for unit creation

## Bot Requirement

- Implement at least one simple `Dummy Bot`.
- The engine must be able to request a move from the bot without simulating mouse clicks.
- You must be able to run `Bot vs. Bot`.

## Minimum Passing Scope

- At least two playable maps.
- Working movement/pathfinding.
- Working economy, purchases, combat, and capture logic.
- Functional action logging and replay.
- Dummy bot support.
- Real git history showing contribution from all team members.

## AI Usage Requirement

If you use GenAI during the project, you must submit `ai_audit.md` documenting:

- which parts AI helped generate or refactor
- the prompts used
- how the output was verified and adjusted
