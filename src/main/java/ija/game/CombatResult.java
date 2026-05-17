/** Author: xmarina00, xbobkos00 */
package ija.game;

/**
 * Immutable result of a resolved combat exchange.
 */
public record CombatResult(
    int damageToDefender,
    int damageToAttacker,
    int defenderHpAfter,
    int attackerHpAfter,
    boolean counterattackPerformed
) {
    public boolean defenderDestroyed() {
        return defenderHpAfter == 0;
    }

    public boolean attackerDestroyed() {
        return attackerHpAfter == 0;
    }
}
