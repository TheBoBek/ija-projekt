/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-16)
 *   - 2026-05-16: Add game event types
 * - TheBoBek (2026-05-02 až 2026-05-11)
 *   - 2026-05-11: Add Turn, money and player logic
 *   - 2026-05-02: Add task 2, implement Tile data handle and refactor TerrainType and TerrainDef.
 *
 * Popis obsahu:
 * - Zdrojový soubor GameEvent v balíku ija.common.
 */
package ija.common;

import ija.game.CombatResult;
import ija.game.Unit;

/**
 * Event object sent to observers after game actions.
 */
public record GameEvent(
    GameEventType type,
    Unit actor,
    Unit target,
    Position from,
    Position to,
    CombatResult combatResult
) {
    public static GameEvent move(Unit actor, Position from, Position to) {
        return new GameEvent(GameEventType.MOVE, actor, null, from, to, null);
    }

    public static GameEvent attack(Unit actor, Unit target, Position from, Position to, CombatResult combatResult) {
        return new GameEvent(GameEventType.ATTACK, actor, target, from, to, combatResult);
    }

    public static GameEvent wait(Unit actor, Position at) {
        return new GameEvent(GameEventType.WAIT, actor, null, at, at, null);
    }

    public static GameEvent capture(Unit actor, Position at) {
        return new GameEvent(GameEventType.CAPTURE, actor, null, at, at, null);
    }

    public static GameEvent purchase(Unit actor, Position at) {
        return new GameEvent(GameEventType.PURCHASE, actor, null, at, at, null);
    }

    public static GameEvent income() {
        return new GameEvent(GameEventType.INCOME, null, null, null, null, null);
    }

    public static GameEvent endTurn() {
        return new GameEvent(GameEventType.END_TURN, null, null, null, null, null);
    }
}
