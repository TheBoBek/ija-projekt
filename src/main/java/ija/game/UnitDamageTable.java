/** Author: xmarina00, xbobkos00 */
package ija.game;

import java.util.EnumMap;

/**
 * Hardcoded base damage values between unit types.
 */
public final class UnitDamageTable {
    private final EnumMap<UnitType, EnumMap<UnitType, Integer>> damageByAttacker;

    public UnitDamageTable() {
        this.damageByAttacker = createDefaultTable();
    }

    public int getBaseDamage(UnitType attacker, UnitType defender) {
        if (attacker == null) {
            throw new IllegalArgumentException("Attacker type must not be null");
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender type must not be null");
        }

        EnumMap<UnitType, Integer> attackerRow = damageByAttacker.get(attacker);
        if (attackerRow == null || !attackerRow.containsKey(defender)) {
            throw new IllegalArgumentException(
                "Missing base damage for " + attacker + " attacking " + defender
            );
        }
        return attackerRow.get(defender);
    }

    private static EnumMap<UnitType, EnumMap<UnitType, Integer>> createDefaultTable() {
        EnumMap<UnitType, EnumMap<UnitType, Integer>> table = new EnumMap<>(UnitType.class);
        for (UnitType type : UnitType.values()) {
            table.put(type, new EnumMap<>(UnitType.class));
        }

        putDamage(table, UnitType.INFANTRY, UnitType.INFANTRY, 55);
        putDamage(table, UnitType.INFANTRY, UnitType.TANK, 5);
        putDamage(table, UnitType.INFANTRY, UnitType.ARTILLERY, 15);

        putDamage(table, UnitType.TANK, UnitType.INFANTRY, 75);
        putDamage(table, UnitType.TANK, UnitType.TANK, 55);
        putDamage(table, UnitType.TANK, UnitType.ARTILLERY, 70);

        putDamage(table, UnitType.ARTILLERY, UnitType.INFANTRY, 90);
        putDamage(table, UnitType.ARTILLERY, UnitType.TANK, 70);
        putDamage(table, UnitType.ARTILLERY, UnitType.ARTILLERY, 75);

        return table;
    }

    private static void putDamage(
        EnumMap<UnitType, EnumMap<UnitType, Integer>> table,
        UnitType attacker,
        UnitType defender,
        int damage
    ) {
        table.get(attacker).put(defender, damage);
    }
}
