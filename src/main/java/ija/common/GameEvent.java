/** Author: xmarina00, xbobkos00 */
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
