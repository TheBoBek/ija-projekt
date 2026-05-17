/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07)
 *   - 2026-05-07: Add CombatResult + tests
 *
 * Popis obsahu:
 * - Zdrojový soubor CombatResultTest v balíku ija.
 */
package ija;

import ija.game.CombatResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Combat result")
public class CombatResultTest {

    @Test
    @DisplayName("Combat result stores raw outcome values")
    void testStoresOutcomeValues() {
        CombatResult result = new CombatResult(35, 10, 65, 90, true);

        Assertions.assertEquals(35, result.damageToDefender());
        Assertions.assertEquals(10, result.damageToAttacker());
        Assertions.assertEquals(65, result.defenderHpAfter());
        Assertions.assertEquals(90, result.attackerHpAfter());
        Assertions.assertTrue(result.counterattackPerformed());
    }

    @Test
    @DisplayName("Destroyed helpers are derived from hp after values")
    void testDestroyedHelpers() {
        CombatResult defenderDestroyed = new CombatResult(100, 0, 0, 80, false);
        CombatResult attackerDestroyed = new CombatResult(20, 70, 40, 0, true);
        CombatResult nobodyDestroyed = new CombatResult(15, 10, 85, 90, true);

        Assertions.assertTrue(defenderDestroyed.defenderDestroyed());
        Assertions.assertFalse(defenderDestroyed.attackerDestroyed());

        Assertions.assertFalse(attackerDestroyed.defenderDestroyed());
        Assertions.assertTrue(attackerDestroyed.attackerDestroyed());

        Assertions.assertFalse(nobodyDestroyed.defenderDestroyed());
        Assertions.assertFalse(nobodyDestroyed.attackerDestroyed());
    }
}
