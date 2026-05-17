/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes
 *
 * Popis obsahu:
 * - Zdrojový soubor EndTurnCommand v balíku ija.command.
 */
package ija.command;

import ija.game.Game;
import ija.game.Turn;

/**
 * End current player's turn.
 */
public final class EndTurnCommand implements GameCommand<Void> {
    @Override
    public CommandResult<Void> execute(Game game) {
        Turn before = game.getTurn();
        game.endTurn();
        Turn after = game.getTurn();

        boolean changed = !before.getCurrentPlayer().equals(after.getCurrentPlayer())
            || before.getTurnNumber() != after.getTurnNumber()
            || before.getPhase() != after.getPhase();
        return new CommandResult<>(null, changed);
    }

    @Override
    public String describe() {
        return "End Turn";
    }
}
