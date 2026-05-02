package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit turn flags")
public class UnitTurnFlagsTest {

    @Test
    @DisplayName("Package-private turn helpers update flags as expected")
    void testTurnHelpers() {
        Unit unit = new Unit(UnitType.ARTILLERY, "P1", new Position(0, 0));

        unit.markMovedThisTurn();
        Assertions.assertTrue(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());

        unit.markActedThisTurn();
        Assertions.assertTrue(unit.hasMovedThisTurn());
        Assertions.assertTrue(unit.hasActedThisTurn());

        unit.resetTurnFlags();
        Assertions.assertFalse(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());
    }
}
