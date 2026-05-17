/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-02)
 *   - 2026-05-02: Add unit load from JSON, Artillery, UnitDamageTable and created tests
 *
 * Popis obsahu:
 * - Zdrojový soubor UnitDamageTableTest v balíku ija.
 */
package ija;

import ija.game.UnitDamageTable;
import ija.game.UnitType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit damage table")
public class UnitDamageTableTest {

    @Test
    @DisplayName("Damage table exposes all infantry attacker values")
    void testInfantryAttackerValues() {
        UnitDamageTable damageTable = new UnitDamageTable();

        Assertions.assertEquals(55, damageTable.getBaseDamage(UnitType.INFANTRY, UnitType.INFANTRY));
        Assertions.assertEquals(5, damageTable.getBaseDamage(UnitType.INFANTRY, UnitType.TANK));
        Assertions.assertEquals(15, damageTable.getBaseDamage(UnitType.INFANTRY, UnitType.ARTILLERY));
    }

    @Test
    @DisplayName("Damage table exposes all tank attacker values")
    void testTankAttackerValues() {
        UnitDamageTable damageTable = new UnitDamageTable();

        Assertions.assertEquals(75, damageTable.getBaseDamage(UnitType.TANK, UnitType.INFANTRY));
        Assertions.assertEquals(55, damageTable.getBaseDamage(UnitType.TANK, UnitType.TANK));
        Assertions.assertEquals(70, damageTable.getBaseDamage(UnitType.TANK, UnitType.ARTILLERY));
    }

    @Test
    @DisplayName("Damage table exposes all artillery attacker values")
    void testArtilleryAttackerValues() {
        UnitDamageTable damageTable = new UnitDamageTable();

        Assertions.assertEquals(90, damageTable.getBaseDamage(UnitType.ARTILLERY, UnitType.INFANTRY));
        Assertions.assertEquals(70, damageTable.getBaseDamage(UnitType.ARTILLERY, UnitType.TANK));
        Assertions.assertEquals(75, damageTable.getBaseDamage(UnitType.ARTILLERY, UnitType.ARTILLERY));
    }

    @Test
    @DisplayName("Damage table rejects null attacker")
    void testRejectsNullAttacker() {
        UnitDamageTable damageTable = new UnitDamageTable();

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> damageTable.getBaseDamage(null, UnitType.TANK)
        );
    }

    @Test
    @DisplayName("Damage table rejects null defender")
    void testRejectsNullDefender() {
        UnitDamageTable damageTable = new UnitDamageTable();

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> damageTable.getBaseDamage(UnitType.TANK, null)
        );
    }
}
