/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07)
 *   - 2026-05-07: Add CombatResult + tests
 *
 * Popis obsahu:
 * - Zdrojový soubor CombatResult v balíku ija.game.
 */
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
