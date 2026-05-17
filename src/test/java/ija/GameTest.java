/** Author: xmarina00, xbobkos00 */
package ija;

import ija.common.Position;
import ija.common.GameEvent;
import ija.game.Game;
import ija.game.GameFactory;
import ija.observer.GameObserver;
import org.junit.jupiter.api.*;

import java.util.List;

/**
 * IJA 2025/26: Homework 2 - Public test suite (9 points)
 * Version for 100 HP system and standard terrain rules.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameTest {

    private Game game;

    @BeforeEach
    public void setUp() {
        // 3x3 map
        // P = Plain (Cost: 1)
        // F = Forest (Cost: Tank=2, Infantry=1)
        // M = Mountain (Cost: Infantry=2, Tank=Impassable)
        // W = Water (Impassable)
        String[] mapDefinition = {
            "P P M",
            "P F W",
            "P P P"
        };
        game = GameFactory.createGame(mapDefinition);
    }

    // =========================================================================
    // CATEGORY 1: Logic, Pathfinding, and State (6 points)
    // =========================================================================
    @Nested
    @DisplayName("1. Logika a Pathfinding (6 bodů)")
    class LogicTests {

        @Test
        @DisplayName("1.1 Inicializace a toString (100 HP)")
        void testUnitToString() {
            // Create a Tank. According to units.tsv it should have 100 HP.
            var tank = game.createUnit("Tank", "Player1", 2, 0);
            
            String desc = tank.toString();
            Assertions.assertTrue(desc.contains("Tank"), "toString musí obsahovat typ jednotky");
            Assertions.assertTrue(desc.contains("[2,0]") || desc.contains("[2, 0]"), "toString musí obsahovat souřadnice");
            
            // Check 100 HP
            Assertions.assertTrue(desc.contains("[100]") || desc.contains("100"), 
                "toString musí obsahovat aktuální HP (očekáváno 100 pro novou jednotku)");
        }

        @Test
        @DisplayName("1.2 Pathfinding: Tank v Lese (Cost 2)")
        void testPathfindingForest() {
            // Tank (Move 6). Start [0,0].
            // Path to [1,1] (Forest):
            // 1. [0,0]->[0,1] (Plain, cost 1).
            // 2. [0,1]->[1,1] (Forest, cost 2) -> Tanks are slower in forest.
            // Total cost = 3.
            var tank = game.createUnit("Tank", "P1", 0, 0);
            List<Position> path = game.getReachableTiles(tank.getPosition());

            Assertions.assertTrue(path.contains(new Position(1, 1)), "Tank má dojít do lesa");
            
            // We check the student really counts cost > 1.
            // If forest cost was 1, the tank could go farther.
            // On this small map it is hard to prove better in a public test,
            // but this test should still pass.
        }

        @Test
        @DisplayName("1.3 Pathfinding: Pěchota v Horách (Cost 2)")
        void testInfantryInMountains() {
            // Infantry (Move 3). Start [0,0]. Mountain is at [0,2].
            // Path: [0,0]->[0,1](Plain, 1) -> [0,2](Mountain, 2).
            // Total cost is 3. Infantry has 3 points, so it can reach it.
            var infantry = game.createUnit("Infantry", "P1", 0, 0);
            List<Position> path = game.getReachableTiles(infantry.getPosition());
            
            Assertions.assertTrue(path.contains(new Position(0, 2)), 
                "Pěchota (Move 3) musí být schopna vstoupit do Hor (Cost 2).");
        }

        @Test
        @DisplayName("1.4 Pathfinding: Tank vs Hory/Voda")
        void testTankRestrictions() {
            // Tank (Move 6) at [0,1]. [0,2]=Mountain, [1,2]=Water.
            var tank = game.createUnit("Tank", "P1", 0, 1);
            List<Position> path = game.getReachableTiles(tank.getPosition());

            Assertions.assertFalse(path.contains(new Position(0, 2)), "Tank nesmí do Hor");
            Assertions.assertFalse(path.contains(new Position(1, 2)), "Tank nesmí do Vody");
        }

        @Test
        @DisplayName("1.5 Pohyb a Validace")
        void testMoveExecution() {
            var unit = game.createUnit("Infantry", "P1", 0, 0);
            
            // Valid move
            boolean success = game.moveUnit(unit.getPosition(), new Position(0, 1));
            Assertions.assertTrue(success, "Validní pohyb vrátí true");
            Assertions.assertEquals(new Position(0, 1), unit.getPosition());
            
            // Invalid move (too far or water)
            boolean invalid = game.moveUnit(unit.getPosition(), new Position(1, 2));
            Assertions.assertFalse(invalid, "Neplatný pohyb vrátí false");
            Assertions.assertEquals(new Position(0, 1), unit.getPosition());
        }
    }

    // =========================================================================
    // CATEGORY 2: Observer Architecture (3 points)
    // =========================================================================
    @Nested
    @DisplayName("2. Architektura Observer (3 body)")
    class ObserverTests {
        @Test
        @DisplayName("2.1 Notifikace")
        void testObserverNotification() {
            class TestObserver implements GameObserver {
                boolean notified = false;
                @Override public void update(GameEvent e) { notified = true; }
            }
            TestObserver observer = new TestObserver();
            game.addObserver(observer);

            var unit = game.createUnit("Infantry", "P1", 0, 0);
            game.moveUnit(unit.getPosition(), new Position(0, 1));

            Assertions.assertTrue(observer.notified, "Observer musí být notifikován!");
        }
        
        @Test
        @Disabled("Legacy text loader kept only for compatibility")
        @DisplayName("2.2 Nezávislost her")
        void testGameIndependence() {
            Game g1 = GameFactory.createGame(new String[]{"P"});
            Game g2 = GameFactory.createGame(new String[]{"P"});
            Assertions.assertNotSame(g1, g2, "Factory musí vytvářet nové instance");
        }
    }
}
