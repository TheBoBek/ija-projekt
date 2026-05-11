package ija.game;

import ija.common.GameEvent;
import ija.common.GameEventType;
import ija.common.Position;
import ija.observer.GameObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game combat integration")
class GameCombatIntegrationTest {

    private static final class CountingObserver implements GameObserver {
        private int count;
        private GameEvent lastEvent;

        @Override
        public void update(GameEvent event) {
            count++;
            lastEvent = event;
        }
    }

    @Test
    @DisplayName("canAttack returns true for a valid melee attack")
    void testCanAttackForValidMeleeAttack() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Tank", "P1", 0, 0);
        game.createUnit("Infantry", "P2", 0, 1);

        Assertions.assertTrue(game.canAttack(new Position(0, 0), new Position(0, 1)));
    }

    @Test
    @DisplayName("canAttack returns false for missing units and out of bounds")
    void testCanAttackRejectsMissingUnitsAndOutOfBounds() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Tank", "P1", 0, 0);

        Assertions.assertFalse(game.canAttack(new Position(0, 0), new Position(0, 1)));
        Assertions.assertFalse(game.canAttack(new Position(0, 1), new Position(0, 0)));
        Assertions.assertFalse(game.canAttack(new Position(0, 0), new Position(0, 2)));
        Assertions.assertFalse(game.canAttack(null, new Position(0, 0)));
    }

    @Test
    @DisplayName("canAttack returns false for same owner acted attacker and out of range target")
    void testCanAttackRejectsInvalidCombatStates() {
        Game sameOwnerGame = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        sameOwnerGame.createUnit("Infantry", "P1", 0, 0);
        sameOwnerGame.createUnit("Tank", "P1", 0, 1);
        Assertions.assertFalse(sameOwnerGame.canAttack(new Position(0, 0), new Position(0, 1)));

        Game actedGame = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        Unit actedAttacker = actedGame.createUnit("Infantry", "P1", 0, 0);
        actedGame.createUnit("Tank", "P2", 0, 1);
        actedAttacker.markActedThisTurn();
        Assertions.assertFalse(actedGame.canAttack(new Position(0, 0), new Position(0, 1)));

        Game rangeGame = new Game(new Tile[][]{{
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN)
        }});
        rangeGame.createUnit("Tank", "P1", 0, 0);
        rangeGame.createUnit("Infantry", "P2", 0, 2);
        Assertions.assertFalse(rangeGame.canAttack(new Position(0, 0), new Position(0, 2)));
    }

    @Test
    @DisplayName("canAttack returns false for artillery after movement")
    void testCanAttackRejectsArtilleryAfterMovement() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN)
        }});
        Unit attacker = game.createUnit("Artillery", "P1", 0, 0);
        game.createUnit("Tank", "P2", 0, 2);
        attacker.markMovedThisTurn();

        Assertions.assertFalse(game.canAttack(new Position(0, 0), new Position(0, 2)));
    }

    @Test
    @DisplayName("attack updates HP and marks attacker as acted when both units survive")
    void testAttackUpdatesCombatState() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        Unit attacker = game.createUnit("Tank", "P1", 0, 0);
        Unit defender = game.createUnit("Infantry", "P2", 0, 1);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 1));

        Assertions.assertEquals(result.defenderHpAfter(), defender.getHp());
        Assertions.assertEquals(result.attackerHpAfter(), attacker.getHp());
        Assertions.assertTrue(attacker.hasActedThisTurn());
        Assertions.assertSame(attacker, game.getUnitAt(new Position(0, 0)));
        Assertions.assertSame(defender, game.getUnitAt(new Position(0, 1)));
    }

    @Test
    @DisplayName("valid attack notifies observers with attack payload")
    void testAttackNotifiesObserversWithPayload() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        Unit attacker = game.createUnit("Tank", "P1", 0, 0);
        Unit defender = game.createUnit("Infantry", "P2", 0, 1);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 1));

        Assertions.assertEquals(1, observer.count);
        Assertions.assertNotNull(observer.lastEvent);
        Assertions.assertEquals(GameEventType.ATTACK, observer.lastEvent.type());
        Assertions.assertSame(attacker, observer.lastEvent.actor());
        Assertions.assertSame(defender, observer.lastEvent.target());
        Assertions.assertEquals(new Position(0, 0), observer.lastEvent.from());
        Assertions.assertEquals(new Position(0, 1), observer.lastEvent.to());
        Assertions.assertSame(result, observer.lastEvent.combatResult());
    }

    @Test
    @DisplayName("attack removes destroyed defender from board state")
    void testAttackRemovesDestroyedDefender() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN)
        }});
        game.createUnit("Artillery", "P1", 0, 0);
        Unit defender = game.createUnit("Infantry", "P2", 0, 2);
        defender.takeDamage(20);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 2));

        Assertions.assertTrue(result.defenderDestroyed());
        Assertions.assertNull(game.getUnitAt(new Position(0, 2)));
    }

    @Test
    @DisplayName("attack still notifies observers when defender is destroyed")
    void testAttackNotifiesObserversWhenDefenderIsDestroyed() {
        Game game = new Game(new Tile[][]{{
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN),
            new Tile(TerrainType.PLAIN)
        }});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        Unit attacker = game.createUnit("Artillery", "P1", 0, 0);
        Unit defender = game.createUnit("Infantry", "P2", 0, 2);
        defender.takeDamage(20);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 2));

        Assertions.assertTrue(result.defenderDestroyed());
        Assertions.assertEquals(1, observer.count);
        Assertions.assertEquals(GameEventType.ATTACK, observer.lastEvent.type());
        Assertions.assertSame(attacker, observer.lastEvent.actor());
        Assertions.assertSame(defender, observer.lastEvent.target());
        Assertions.assertSame(result, observer.lastEvent.combatResult());
    }

    @Test
    @DisplayName("attack removes destroyed attacker after counterattack")
    void testAttackRemovesDestroyedAttacker() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        Unit attacker = game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Tank", "P2", 0, 1);
        attacker.takeDamage(50);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 1));

        Assertions.assertTrue(result.attackerDestroyed());
        Assertions.assertNull(game.getUnitAt(new Position(0, 0)));
        Assertions.assertNotNull(game.getUnitAt(new Position(0, 1)));
    }

    @Test
    @DisplayName("attack still notifies observers when attacker is destroyed by counterattack")
    void testAttackNotifiesObserversWhenAttackerIsDestroyed() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        Unit attacker = game.createUnit("Infantry", "P1", 0, 0);
        Unit defender = game.createUnit("Tank", "P2", 0, 1);
        attacker.takeDamage(50);

        CombatResult result = game.attack(new Position(0, 0), new Position(0, 1));

        Assertions.assertTrue(result.attackerDestroyed());
        Assertions.assertEquals(1, observer.count);
        Assertions.assertEquals(GameEventType.ATTACK, observer.lastEvent.type());
        Assertions.assertSame(attacker, observer.lastEvent.actor());
        Assertions.assertSame(defender, observer.lastEvent.target());
        Assertions.assertSame(result, observer.lastEvent.combatResult());
    }

    @Test
    @DisplayName("attack throws when the attack is not allowed")
    void testAttackRejectsInvalidCombat() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P1", 0, 1);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> game.attack(new Position(0, 0), new Position(0, 1))
        );
    }

    @Test
    @DisplayName("invalid attack does not notify observers")
    void testInvalidAttackDoesNotNotifyObservers() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN), new Tile(TerrainType.PLAIN)}});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P1", 0, 1);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> game.attack(new Position(0, 0), new Position(0, 1))
        );
        Assertions.assertEquals(0, observer.count);
        Assertions.assertNull(observer.lastEvent);
    }
}
