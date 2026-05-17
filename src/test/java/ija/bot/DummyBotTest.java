/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add bot + test
 *
 * Popis obsahu:
 * - Zdrojový soubor DummyBotTest v balíku ija.bot.
 */
package ija.bot;

import ija.common.Position;
import ija.game.Game;
import ija.game.GameFactory;
import ija.game.Turn;
import ija.game.Unit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dummy bot")
class DummyBotTest {

    @Test
    @DisplayName("Bot can execute full turn without UI")
    void testBotExecutesFullTurn() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        game.createUnit("Infantry", "P1", 0, 0);

        DummyBot bot = new DummyBot(42L);
        bot.playTurn(game);

        Assertions.assertEquals("P2", game.getTurn().getCurrentPlayer());

        Unit p1Unit = findFirstUnitOwnedBy(game, "P1");
        Assertions.assertNotNull(p1Unit);
        Assertions.assertTrue(p1Unit.hasActedThisTurn());
    }

    @Test
    @DisplayName("Bot purchases affordable unit in owned free factory")
    void testBotPurchasesInFactory() {
        Game game = GameFactory.createGame(new String[]{
            "T C",
            "P P"
        });
        game.getTileAt(0, 0).setOwner("P1");
        game.getTileAt(0, 1).setOwner("P1");

        game.endTurn();
        game.endTurn();
        Assertions.assertEquals("P1", game.getTurn().getCurrentPlayer());
        Assertions.assertEquals(1000, game.getPlayer("P1").getMoney());

        DummyBot bot = new DummyBot(1L);
        bot.playTurn(game);

        Unit purchased = game.getUnitAt(new Position(0, 0));
        Assertions.assertNotNull(purchased);
        Assertions.assertEquals("P1", purchased.getOwner());
        Assertions.assertEquals(0, game.getPlayer("P1").getMoney());
        Assertions.assertEquals("P2", game.getTurn().getCurrentPlayer());
    }

    @Test
    @DisplayName("Bot vs Bot loop progresses turn counter")
    void testBotVsBotProgressesTurns() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });
        game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P2", 1, 1);

        DummyBot bot = new DummyBot(7L);
        bot.playTurn(game);
        bot.playTurn(game);
        bot.playTurn(game);
        bot.playTurn(game);

        Turn turn = game.getTurn();
        Assertions.assertTrue(turn.getTurnNumber() >= 3);
    }

    private Unit findFirstUnitOwnedBy(Game game, String owner) {
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Unit unit = game.getUnitAt(new Position(row, col));
                if (unit != null && owner.equals(unit.getOwner())) {
                    return unit;
                }
            }
        }
        return null;
    }
}
