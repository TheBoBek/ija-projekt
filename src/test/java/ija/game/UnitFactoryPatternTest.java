package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Unit factory pattern")
class UnitFactoryPatternTest {

    @Test
    @DisplayName("Game delegates unit creation to provided unit factory")
    void testGameDelegatesUnitCreationToFactory() {
        TrackingUnitFactory factory = new TrackingUnitFactory();
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN)}}, factory);

        Unit created = game.createUnit("Infantry", "P1", 0, 0);

        Assertions.assertEquals(1, factory.calls);
        Assertions.assertEquals(UnitType.INFANTRY, factory.lastType);
        Assertions.assertEquals("P1", factory.lastOwner);
        Assertions.assertEquals(new Position(0, 0), factory.lastPosition);
        Assertions.assertSame(created, game.getUnitAt(new Position(0, 0)));
    }

    @Test
    @DisplayName("Game rejects null unit returned by factory")
    void testGameRejectsNullUnitFromFactory() {
        UnitFactory factory = (type, owner, position) -> null;
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN)}}, factory);

        IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> game.createUnit("Infantry", "P1", 0, 0)
        );

        Assertions.assertEquals("Unit factory returned null unit", exception.getMessage());
    }

    private static final class TrackingUnitFactory implements UnitFactory {
        private int calls;
        private UnitType lastType;
        private String lastOwner;
        private Position lastPosition;

        @Override
        public Unit createUnit(UnitType type, String owner, Position position) {
            calls++;
            lastType = type;
            lastOwner = owner;
            lastPosition = position;
            return new Unit(type, owner, position);
        }
    }
}
