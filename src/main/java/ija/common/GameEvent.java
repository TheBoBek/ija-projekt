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
}
