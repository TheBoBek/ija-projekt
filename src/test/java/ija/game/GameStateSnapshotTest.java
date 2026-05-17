/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add game state snapshot model
 *
 * Popis obsahu:
 * - Zdrojový soubor GameStateSnapshotTest v balíku ija.game.
 */
package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game state snapshot")
class GameStateSnapshotTest {

    @Test
    @DisplayName("Roundtrip snapshot restore keeps map units turn and economy")
    void testSnapshotRoundtripRestoresCompleteState() {
        Game game = new Game(new Tile[][]{
            {new Tile(TerrainType.CITY, "P2"), new Tile(TerrainType.FACTORY, "P1")},
            {new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}
        });

        Unit infantry = game.createUnit("Infantry", "P1", 0, 0);
        Unit tank = game.createUnit("Tank", "P1", 1, 1);
        tank.takeDamage(25);
        tank.markMovedThisTurn();
        tank.markActedThisTurn();

        game.captureBuilding(new Position(0, 0));
        game.waitUnit(new Position(0, 0));
        game.endTurn();

        GameState snapshot = game.snapshotState();
        Game restored = Game.fromState(snapshot);

        Tile restoredCity = restored.getTileAt(0, 0);
        Assertions.assertEquals(TerrainType.CITY, restoredCity.getTerrainType());
        Assertions.assertEquals("P2", restoredCity.getOwner());
        Assertions.assertEquals(10, restoredCity.getCapturePointsRemaining());

        Unit restoredInfantry = restored.getUnitAt(new Position(0, 0));
        Assertions.assertNotNull(restoredInfantry);
        Assertions.assertEquals(UnitType.INFANTRY, restoredInfantry.getType());
        Assertions.assertEquals(100, restoredInfantry.getHp());
        Assertions.assertFalse(restoredInfantry.hasMovedThisTurn());
        Assertions.assertTrue(restoredInfantry.hasActedThisTurn());

        Unit restoredTank = restored.getUnitAt(new Position(1, 1));
        Assertions.assertNotNull(restoredTank);
        Assertions.assertEquals(UnitType.TANK, restoredTank.getType());
        Assertions.assertEquals(75, restoredTank.getHp());
        Assertions.assertTrue(restoredTank.hasMovedThisTurn());
        Assertions.assertTrue(restoredTank.hasActedThisTurn());

        Assertions.assertEquals(1000, restored.getPlayer("P2").getMoney());
        Assertions.assertEquals(0, restored.getPlayer("P1").getMoney());
        Assertions.assertEquals(game.getTurn().getCurrentPlayer(), restored.getTurn().getCurrentPlayer());
        Assertions.assertEquals(game.getTurn().getTurnNumber(), restored.getTurn().getTurnNumber());
        Assertions.assertEquals(game.getTurn().getPhase(), restored.getTurn().getPhase());
    }

    @Test
    @DisplayName("Restore rejects invalid capture points")
    void testRestoreRejectsInvalidCapturePoints() {
        GameState.TileState[][] tileStates = new GameState.TileState[][]{
            {new GameState.TileState(TerrainType.CITY, "P1", 99)}
        };
        GameState badState = new GameState(
            tileStates,
            java.util.List.of(),
            java.util.List.of(
                new GameState.PlayerState("P1", 0),
                new GameState.PlayerState("P2", 0)
            ),
            new GameState.TurnState("P1", 1, Turn.Phase.ACTION),
            false,
            null
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> Game.fromState(badState));
    }

    @Test
    @DisplayName("Roundtrip snapshot keeps HQ-capture game-over state")
    void testSnapshotRoundtripKeepsGameOverState() {
        Tile hq = new Tile(TerrainType.HQ, "P2");
        hq.reduceCapturePoints(10);
        Game game = new Game(new Tile[][]{{hq}});
        game.createUnit("Infantry", "P1", 0, 0);

        CaptureResult capture = game.captureBuilding(new Position(0, 0));
        Assertions.assertTrue(capture.capturedHq());
        Assertions.assertTrue(game.isGameOver());
        Assertions.assertEquals("P1", game.getWinnerPlayerId());

        Game restored = Game.fromState(game.snapshotState());
        Assertions.assertTrue(restored.isGameOver());
        Assertions.assertEquals("P1", restored.getWinnerPlayerId());
    }
}
