package ija.command;

import ija.game.Game;
import ija.game.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Command executor with undo/redo based on before/after snapshots.
 */
public final class GameCommandHistory {
    private final List<HistoryEntry> entries = new ArrayList<>();
    private int cursor = -1;

    public <T> CommandResult<T> execute(Game game, GameCommand<T> command) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command must not be null");
        }

        GameState before = game.snapshotState();
        CommandResult<T> result = command.execute(game);
        if (result != null && result.stateChanged()) {
            trimRedo();
            entries.add(new HistoryEntry(command.describe(), before, game.snapshotState()));
            cursor = entries.size() - 1;
        }
        return result;
    }

    public boolean canUndo() {
        return cursor >= 0;
    }

    public boolean canRedo() {
        return cursor + 1 < entries.size();
    }

    public Game undo() {
        if (!canUndo()) {
            return null;
        }
        HistoryEntry entry = entries.get(cursor);
        cursor--;
        return Game.fromState(entry.beforeState());
    }

    public Game redo() {
        if (!canRedo()) {
            return null;
        }
        cursor++;
        HistoryEntry entry = entries.get(cursor);
        return Game.fromState(entry.afterState());
    }

    public void clear() {
        entries.clear();
        cursor = -1;
    }

    public int size() {
        return entries.size();
    }

    private void trimRedo() {
        int keep = cursor + 1;
        while (entries.size() > keep) {
            entries.remove(entries.size() - 1);
        }
    }

    private record HistoryEntry(
        String description,
        GameState beforeState,
        GameState afterState
    ) {
    }
}
