package ija.game;

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
}
