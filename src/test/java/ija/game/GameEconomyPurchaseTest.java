/** Author: xmarina00 */
package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game economy and factory purchase")
class GameEconomyPurchaseTest {

    @Test
    @DisplayName("Income is awarded to the player whose turn starts")
    void testIncomeAwardedOnTurnStart() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2")}});

        Assertions.assertEquals(0, game.getPlayer("P1").getMoney());
        Assertions.assertEquals(0, game.getPlayer("P2").getMoney());

        game.endTurn();
        Assertions.assertEquals("P2", game.getTurn().getCurrentPlayer());
        Assertions.assertEquals(1000, game.getPlayer("P2").getMoney());

        game.endTurn();
        Assertions.assertEquals("P1", game.getTurn().getCurrentPlayer());
        Assertions.assertEquals(0, game.getPlayer("P1").getMoney());
    }

    @Test
    @DisplayName("Factory purchase spends money and creates exhausted unit")
    void testPurchaseFlowOnOwnedFactory() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.FACTORY, "P1"),
            new Tile(TerrainType.CITY, "P1")
        }});
        Position factory = new Position(0, 0);

        Assertions.assertFalse(game.canPurchaseUnit("Infantry", factory));

        game.endTurn();
        game.endTurn();

        Assertions.assertEquals(1000, game.getPlayer("P1").getMoney());
        Assertions.assertTrue(game.canPurchaseUnit("Infantry", factory));
        Assertions.assertFalse(game.canPurchaseUnit("Tank", factory));

        Unit created = game.purchaseUnit("Infantry", factory);
        Assertions.assertEquals("P1", created.getOwner());
        Assertions.assertEquals(UnitType.INFANTRY, created.getType());
        Assertions.assertEquals(factory, created.getPosition());
        Assertions.assertTrue(created.hasMovedThisTurn());
        Assertions.assertTrue(created.hasActedThisTurn());
        Assertions.assertEquals(0, game.getPlayer("P1").getMoney());
        Assertions.assertFalse(game.canPurchaseUnit("Infantry", factory));
    }

    @Test
    @DisplayName("Cannot purchase on enemy factory even with enough money")
    void testCannotPurchaseOnEnemyFactory() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.FACTORY, "P2"),
            new Tile(TerrainType.CITY, "P1")
        }});
        Position enemyFactory = new Position(0, 0);

        game.endTurn();
        game.endTurn();

        Assertions.assertEquals(1000, game.getPlayer("P1").getMoney());
        Assertions.assertFalse(game.canPurchaseUnit("Infantry", enemyFactory));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.purchaseUnit("Infantry", enemyFactory));
    }

    @Test
    @DisplayName("Income helper counts cities only")
    void testIncomeCountsCitiesOnly() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.CITY, "P1"),
            new Tile(TerrainType.FACTORY, "P1"),
            new Tile(TerrainType.HQ, "P1")
        }});

        Assertions.assertEquals(1000, game.getIncomeForPlayer("P1"));
        Assertions.assertEquals(0, game.getIncomeForPlayer("P2"));
    }

    @Test
    @DisplayName("Turn start auto-repair heals owned damaged unit and deducts repair cost")
    void testTurnStartAutoRepairWithCost() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2")}});
        Unit unit = game.createUnit("Infantry", "P2", 0, 0);
        unit.takeDamage(20);

        game.endTurn();

        Assertions.assertEquals("P2", game.getTurn().getCurrentPlayer());
        Assertions.assertEquals(100, unit.getHp());
        Assertions.assertEquals(800, game.getPlayer("P2").getMoney());
    }

    @Test
    @DisplayName("Turn start auto-repair does not happen when player cannot afford it")
    void testTurnStartAutoRepairSkippedWhenInsufficientFunds() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.FACTORY, "P2")}});
        Unit unit = game.createUnit("Tank", "P2", 0, 0);
        unit.takeDamage(10);
        game.setPlayerMoney("P2", 100);

        game.endTurn();

        Assertions.assertEquals("P2", game.getTurn().getCurrentPlayer());
        Assertions.assertEquals(90, unit.getHp());
        Assertions.assertEquals(100, game.getPlayer("P2").getMoney());
    }

    @Test
    @DisplayName("Turn start auto-repair charges proportionally for partial missing HP")
    void testTurnStartAutoRepairPartialHpCost() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2")}});
        Unit unit = game.createUnit("Infantry", "P2", 0, 0);
        unit.takeDamage(15);

        game.endTurn();

        Assertions.assertEquals(100, unit.getHp());
        Assertions.assertEquals(850, game.getPlayer("P2").getMoney());
    }
}
