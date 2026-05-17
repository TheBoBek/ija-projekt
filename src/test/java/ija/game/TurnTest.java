/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-11)
 *   - 2026-05-11: Add Turn, money and player logic
 *
 * Popis obsahu:
 * - Zdrojový soubor TurnTest v balíku ija.game.
 */
package ija.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Turn")
class TurnTest {

    @Test
    @DisplayName("Turn stores valid values")
    void testTurnStoresValidValues() {
        Turn turn = new Turn("P1", 1, Turn.Phase.ACTION);

        Assertions.assertEquals("P1", turn.getCurrentPlayer());
        Assertions.assertEquals(1, turn.getTurnNumber());
        Assertions.assertEquals(Turn.Phase.ACTION, turn.getPhase());
    }

    @Test
    @DisplayName("Turn rejects null and blank current player")
    void testTurnRejectsInvalidCurrentPlayer() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Turn(null, 1, Turn.Phase.ACTION));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Turn("   ", 1, Turn.Phase.ACTION));
    }

    @Test
    @DisplayName("Turn rejects non-positive turn number")
    void testTurnRejectsNonPositiveTurnNumber() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Turn("P1", 0, Turn.Phase.ACTION));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Turn("P1", -1, Turn.Phase.ACTION));
    }

    @Test
    @DisplayName("Turn rejects null phase")
    void testTurnRejectsNullPhase() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Turn("P1", 1, null));
    }
}
