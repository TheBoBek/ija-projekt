package ija;

import ija.game.CombatService;
import ija.game.UnitDamageTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Combat service")
public class CombatServiceTest {

    @Test
    @DisplayName("Combat service can be created with a damage table")
    void testCreatesWithDamageTable() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertNotNull(combatService);
    }

    @Test
    @DisplayName("Combat service rejects null damage table")
    void testRejectsNullDamageTable() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new CombatService(null)
        );
    }
}
