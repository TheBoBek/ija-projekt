package ija.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game player")
class GamePlayerTest {

    @Test
    @DisplayName("New game starts with default players")
    void testNewGameHasDefaultPlayers() {
        Game game = GameFactory.createGame(new String[]{"P"});

        Player first = game.getPlayer("P1");
        Player second = game.getPlayer("P2");

        Assertions.assertEquals("P1", first.getPlayerId());
        Assertions.assertEquals(0, first.getMoney());
        Assertions.assertEquals("P2", second.getPlayerId());
        Assertions.assertEquals(0, second.getMoney());
    }

    @Test
    @DisplayName("Game returns player as a copy")
    void testGetPlayerReturnsCopy() {
        Game game = GameFactory.createGame(new String[]{"P"});

        Player returnedPlayer = game.getPlayer("P1");
        returnedPlayer.setPlayerId("P2");
        returnedPlayer.setMoney(5000);

        Player internalView = game.getPlayer("P1");

        Assertions.assertEquals("P1", internalView.getPlayerId());
        Assertions.assertEquals(0, internalView.getMoney());
    }

    @Test
    @DisplayName("Game rejects unknown and blank player id")
    void testGetPlayerRejectsInvalidId() {
        Game game = GameFactory.createGame(new String[]{"P"});

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.getPlayer(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.getPlayer("   "));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.getPlayer("P3"));
    }
}
