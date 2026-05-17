package ija.command;

import ija.common.Position;
import ija.game.Game;
import ija.game.GameFactory;
import ija.game.Unit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Game command history")
class GameCommandHistoryTest {

    @Test
    @DisplayName("Move command can be undone and redone")
    void testMoveUndoRedo() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        game.createUnit("Infantry", "P1", 0, 0);
        GameCommandHistory history = new GameCommandHistory();

        CommandResult<Boolean> move = history.execute(game, new MoveCommand(new Position(0, 0), new Position(0, 1)));

        Assertions.assertTrue(move.value());
        Assertions.assertTrue(history.canUndo());
        Assertions.assertNotNull(game.getUnitAt(new Position(0, 1)));

        Game undone = history.undo();
        Assertions.assertNotNull(undone);
        Assertions.assertNotNull(undone.getUnitAt(new Position(0, 0)));
        Assertions.assertNull(undone.getUnitAt(new Position(0, 1)));

        Game redone = history.redo();
        Assertions.assertNotNull(redone);
        Assertions.assertNull(redone.getUnitAt(new Position(0, 0)));
        Assertions.assertNotNull(redone.getUnitAt(new Position(0, 1)));
    }

    @Test
    @DisplayName("No-op command result is not stored in history")
    void testNoOpNotRecorded() {
        Game game = GameFactory.createGame(new String[]{"P"});
        GameCommandHistory history = new GameCommandHistory();

        CommandResult<Boolean> waited = history.execute(game, new WaitCommand(new Position(0, 0)));

        Assertions.assertFalse(waited.value());
        Assertions.assertEquals(0, history.size());
        Assertions.assertFalse(history.canUndo());
    }

    @Test
    @DisplayName("Executing command after undo clears redo branch")
    void testBranchingClearsRedo() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        game.createUnit("Infantry", "P1", 0, 0);
        GameCommandHistory history = new GameCommandHistory();

        history.execute(game, new MoveCommand(new Position(0, 0), new Position(0, 1)));
        Game undone = history.undo();
        Assertions.assertNotNull(undone);
        history.execute(undone, new WaitCommand(new Position(0, 0)));

        Assertions.assertFalse(history.canRedo());
        Assertions.assertEquals(1, history.size());
    }

    @Test
    @DisplayName("Capture command consumes unit action when game continues")
    void testCaptureCommandConsumesAction() {
        Game game = GameFactory.createGame(new String[]{"C"});
        game.getTileAt(new Position(0, 0)).setOwner("P2");
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        GameCommandHistory history = new GameCommandHistory();

        CommandResult<ija.game.CaptureResult> result = history.execute(game, new CaptureCommand(new Position(0, 0)));

        Assertions.assertTrue(result.value().progressApplied());
        Assertions.assertTrue(unit.hasActedThisTurn());
        Assertions.assertEquals(1, history.size());
    }
}
