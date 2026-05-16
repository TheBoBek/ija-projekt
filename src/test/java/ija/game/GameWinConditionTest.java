package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game HQ capture win condition")
class GameWinConditionTest {

    @Test
    @DisplayName("Capturing enemy HQ ends game and stores winner")
    void testHqCaptureEndsGame() {
        Tile hq = new Tile(TerrainType.HQ, "P2");
        hq.reduceCapturePoints(10);
        Game game = new Game(new Tile[][]{{hq}});
        game.createUnit("Infantry", "P1", 0, 0);

        CaptureResult result = game.captureBuilding(new Position(0, 0));

        Assertions.assertTrue(result.capturedHq());
        Assertions.assertTrue(game.isGameOver());
        Assertions.assertEquals("P1", game.getWinnerPlayerId());
    }

    @Test
    @DisplayName("After HQ capture no further actions can progress game")
    void testGameBlocksActionsAfterHqCapture() {
        Tile hq = new Tile(TerrainType.HQ, "P2");
        hq.reduceCapturePoints(10);
        Game game = new Game(new Tile[][]{
            {hq, new Tile(TerrainType.PLAIN)},
            {new Tile(TerrainType.FACTORY, "P1"), new Tile(TerrainType.PLAIN)}
        });
        game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P1", 1, 1);

        game.captureBuilding(new Position(0, 0));
        Turn turnBefore = game.getTurn();

        Assertions.assertFalse(game.moveUnit(new Position(1, 1), new Position(1, 0)));
        Assertions.assertFalse(game.waitUnit(new Position(1, 1)));
        Assertions.assertFalse(game.canAttack(new Position(1, 1), new Position(0, 0)));
        Assertions.assertFalse(game.canCaptureBuilding(new Position(1, 1)));
        Assertions.assertFalse(game.canPurchaseUnit("Infantry", new Position(1, 0)));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.purchaseUnit("Infantry", new Position(1, 0)));

        game.endTurn();
        Turn turnAfter = game.getTurn();
        Assertions.assertEquals(turnBefore.getCurrentPlayer(), turnAfter.getCurrentPlayer());
        Assertions.assertEquals(turnBefore.getTurnNumber(), turnAfter.getTurnNumber());
    }
}
