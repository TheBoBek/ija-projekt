/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07 až 2026-05-17)
 *   - 2026-05-17: Prepare assignment files and simplify engine validation
 *   - 2026-05-07: Extend CombatService + tests + Add factory validation API.
 *   - 2026-05-07: Add CombatService + tests
 *
 * Popis obsahu:
 * - Zdrojový soubor CombatService v balíku ija.game.
 */
package ija.game;

/**
 * Central service for combat rules and resolution.
 */
public final class CombatService {
    private final UnitDamageTable damageTable;

    public CombatService(UnitDamageTable damageTable) {
        if (damageTable == null) {
            throw new IllegalArgumentException("Damage table must not be null");
        }
        this.damageTable = damageTable;
    }

    public boolean canAttack(Unit attacker, Unit defender) {
        if (attacker == null || defender == null) {
            return false;
        }
        if (attacker == defender) {
            return false;
        }
        if (attacker.isDestroyed()) {
            return false;
        }
        if (defender.isDestroyed()) {
            return false;
        }
        if (attacker.getOwner().equals(defender.getOwner())) {
            return false;
        }
        if (attacker.hasActedThisTurn()) {
            return false;
        }
        if (attacker.hasMovedThisTurn() && !attacker.getType().canMoveAndShoot()) {
            return false;
        }

        int distance = manhattanDistance(attacker, defender);
        return attacker.getType().canAttackAtDistance(distance);
    }

    public int calculateDamage(int baseDamage, int attackerHp, int defenderTerrainBonus) {
        return baseDamage * attackerHp * (10 - defenderTerrainBonus) / 1000;
    }

    public CombatResult resolveAttack(
        Unit attacker,
        Tile attackerTile,
        Unit defender,
        Tile defenderTile
    ) {
        if (attackerTile == null) {
            throw new IllegalArgumentException("Attacker tile must not be null");
        }
        if (defenderTile == null) {
            throw new IllegalArgumentException("Defender tile must not be null");
        }
        if (!canAttack(attacker, defender)) {
            throw new IllegalArgumentException("Attack is not allowed");
        }

        int baseDamage = damageTable.getBaseDamage(attacker.getType(), defender.getType());
        int rawDamageToDefender = calculateDamage(baseDamage, attacker.getHp(), defenderTile.getBonus());
        int appliedDamageToDefender = defender.takeDamage(rawDamageToDefender);
        attacker.markActedThisTurn();

        int appliedDamageToAttacker = 0;
        boolean counterattackPerformed = false;

        if (!defender.isDestroyed()) {
            int distance = manhattanDistance(defender, attacker);
            if (defender.getType().canAttackAtDistance(distance)) {
                counterattackPerformed = true;
                appliedDamageToAttacker = resolveCounterattack(defender, defenderTile, attacker, attackerTile);
            }
        }

        return new CombatResult(
            appliedDamageToDefender,
            appliedDamageToAttacker,
            defender.getHp(),
            attacker.getHp(),
            counterattackPerformed
        );
    }

    private int manhattanDistance(Unit attacker, Unit defender) {
        return Math.abs(attacker.getPosition().row() - defender.getPosition().row())
            + Math.abs(attacker.getPosition().col() - defender.getPosition().col());
    }

    private int resolveCounterattack(
        Unit counterAttacker,
        Tile counterAttackerTile,
        Unit originalAttacker,
        Tile originalAttackerTile
    ) {
        int baseDamage = damageTable.getBaseDamage(counterAttacker.getType(), originalAttacker.getType());
        int rawDamage = calculateDamage(baseDamage, counterAttacker.getHp(), originalAttackerTile.getBonus());
        return originalAttacker.takeDamage(rawDamage);
    }
}
