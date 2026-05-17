/** Author: xmarina00, xbobkos00 */
package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game turn")
class GameTurnTest {

    @Test
    @DisplayName("New game starts with default turn")
    void testNewGameHasDefaultTurn() {
        Game game = GameFactory.createGame(new String[]{"P"});

        Turn turn = game.getTurn();

        Assertions.assertEquals("P1", turn.getCurrentPlayer());
        Assertions.assertEquals(1, turn.getTurnNumber());
        Assertions.assertEquals(Turn.Phase.ACTION, turn.getPhase());
    }

    @Test
    @DisplayName("Game returns turn as a copy")
    void testGetTurnReturnsCopy() {
        Game game = GameFactory.createGame(new String[]{"P"});

        Turn returnedTurn = game.getTurn();
        returnedTurn.setCurrentPlayer("P2");
        returnedTurn.setTurnNumber(5);
        returnedTurn.setPhase(Turn.Phase.INCOME);

        Turn internalView = game.getTurn();

        Assertions.assertEquals("P1", internalView.getCurrentPlayer());
        Assertions.assertEquals(1, internalView.getTurnNumber());
        Assertions.assertEquals(Turn.Phase.ACTION, internalView.getPhase());
    }

    @Test
    @DisplayName("endTurn switches player and increments turn after both players act")
    void testEndTurnSwitchesPlayerAndTurnNumber() {
        Game game = GameFactory.createGame(new String[]{"P"});

        game.endTurn();
        Turn afterFirstEnd = game.getTurn();
        Assertions.assertEquals("P2", afterFirstEnd.getCurrentPlayer());
        Assertions.assertEquals(1, afterFirstEnd.getTurnNumber());

        game.endTurn();
        Turn afterSecondEnd = game.getTurn();
        Assertions.assertEquals("P1", afterSecondEnd.getCurrentPlayer());
        Assertions.assertEquals(2, afterSecondEnd.getTurnNumber());
    }

    @Test
    @DisplayName("endTurn resets turn flags for units of the new current player")
    void testEndTurnResetsFlagsForNextPlayerUnits() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        Unit p1 = game.createUnit("Infantry", "P1", 0, 0);
        Unit p2 = game.createUnit("Infantry", "P2", 0, 1);

        p1.markMovedThisTurn();
        p1.markActedThisTurn();
        p2.markMovedThisTurn();
        p2.markActedThisTurn();

        game.endTurn();
        Assertions.assertTrue(p1.hasMovedThisTurn());
        Assertions.assertTrue(p1.hasActedThisTurn());
        Assertions.assertFalse(p2.hasMovedThisTurn());
        Assertions.assertFalse(p2.hasActedThisTurn());
    }

    @Test
    @DisplayName("waitUnit marks a unit as acted and rejects repeated waits")
    void testWaitUnit() {
        Game game = GameFactory.createGame(new String[]{"P"});
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        Position unitPosition = new Position(0, 0);

        Assertions.assertTrue(game.waitUnit(unitPosition));
        Assertions.assertTrue(unit.hasActedThisTurn());
        Assertions.assertFalse(game.waitUnit(unitPosition));
        Assertions.assertFalse(game.waitUnit(null));
        Assertions.assertFalse(game.waitUnit(new Position(0, 1)));
    }
}
