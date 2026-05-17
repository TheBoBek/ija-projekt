/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes
 *
 * Popis obsahu:
 * - Zdrojový soubor MoveCommand v balíku ija.command.
 */
package ija.command;

import ija.common.Position;
import ija.game.Game;

/**
 * Move a unit from one position to another.
 */
public final class MoveCommand implements GameCommand<Boolean> {
    private final Position from;
    private final Position to;

    public MoveCommand(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public CommandResult<Boolean> execute(Game game) {
        boolean moved = game.moveUnit(from, to);
        return new CommandResult<>(moved, moved);
    }

    @Override
    public String describe() {
        return "Move " + from + " -> " + to;
    }
}
