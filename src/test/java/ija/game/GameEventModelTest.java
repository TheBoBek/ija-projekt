package ija.game;

import ija.common.GameEvent;
import ija.common.GameEventType;
import ija.common.Position;
import ija.observer.GameObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@DisplayName("Game event model")
class GameEventModelTest {

    private static final class CapturingObserver implements GameObserver {
        private final List<GameEvent> events = new ArrayList<>();

        @Override
        public void update(GameEvent event) {
            events.add(event);
        }
    }

    @Test
    @DisplayName("wait emits WAIT event")
    void testWaitEmitsEvent() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN)}});
        CapturingObserver observer = new CapturingObserver();
        game.addObserver(observer);
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);

        boolean waited = game.waitUnit(unit.getPosition());

        Assertions.assertTrue(waited);
        Assertions.assertEquals(1, observer.events.size());
        GameEvent event = observer.events.get(0);
        Assertions.assertEquals(GameEventType.WAIT, event.type());
        Assertions.assertSame(unit, event.actor());
        Assertions.assertEquals(unit.getPosition(), event.from());
        Assertions.assertEquals(unit.getPosition(), event.to());
    }

    @Test
    @DisplayName("capture emits CAPTURE event when progress is applied")
    void testCaptureEmitsEventOnProgress() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2")}});
        CapturingObserver observer = new CapturingObserver();
        game.addObserver(observer);
        game.createUnit("Infantry", "P1", 0, 0);

        CaptureResult result = game.captureBuilding(new Position(0, 0));

        Assertions.assertTrue(result.progressApplied());
        Assertions.assertEquals(1, observer.events.size());
        Assertions.assertEquals(GameEventType.CAPTURE, observer.events.get(0).type());
    }

    @Test
    @DisplayName("purchase emits PURCHASE event with created unit actor")
    void testPurchaseEmitsEvent() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.FACTORY, "P1")}});
        CapturingObserver observer = new CapturingObserver();
        game.addObserver(observer);
        game.setPlayerMoney("P1", 1000);

        Unit created = game.purchaseUnit("Infantry", new Position(0, 0));

        Assertions.assertEquals(1, observer.events.size());
        GameEvent event = observer.events.get(0);
        Assertions.assertEquals(GameEventType.PURCHASE, event.type());
        Assertions.assertSame(created, event.actor());
        Assertions.assertEquals(new Position(0, 0), event.from());
        Assertions.assertEquals(new Position(0, 0), event.to());
    }

    @Test
    @DisplayName("endTurn emits END_TURN and INCOME events")
    void testEndTurnEmitsEvents() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2")}});
        CapturingObserver observer = new CapturingObserver();
        game.addObserver(observer);

        game.endTurn();

        Assertions.assertEquals(2, observer.events.size());
        Assertions.assertEquals(GameEventType.END_TURN, observer.events.get(0).type());
        Assertions.assertEquals(GameEventType.INCOME, observer.events.get(1).type());
    }
}
