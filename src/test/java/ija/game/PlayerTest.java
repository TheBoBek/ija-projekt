/** Author: xmarina00, xbobkos00 */
package ija.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Player")
class PlayerTest {

    @Test
    @DisplayName("Player stores valid values")
    void testPlayerStoresValidValues() {
        Player player = new Player("P1", 0);

        Assertions.assertEquals("P1", player.getPlayerId());
        Assertions.assertEquals(0, player.getMoney());
    }

    @Test
    @DisplayName("Player rejects null and blank player id")
    void testPlayerRejectsInvalidPlayerId() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Player(null, 0));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Player("   ", 0));
    }

    @Test
    @DisplayName("Player rejects negative money")
    void testPlayerRejectsNegativeMoney() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Player("P1", -1));
    }
}
