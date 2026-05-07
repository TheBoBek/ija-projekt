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
}
