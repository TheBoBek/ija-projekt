/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes
 *
 * Popis obsahu:
 * - Zdrojový soubor WaitCommand v balíku ija.command.
 */
package ija.command;

import ija.common.Position;
import ija.game.Game;

/**
 * Mark one unit as waiting.
 */
public final class WaitCommand implements GameCommand<Boolean> {
    private final Position unitPosition;

    public WaitCommand(Position unitPosition) {
        this.unitPosition = unitPosition;
    }

    @Override
    public CommandResult<Boolean> execute(Game game) {
        boolean waited = game.waitUnit(unitPosition);
        return new CommandResult<>(waited, waited);
    }

    @Override
    public String describe() {
        return "Wait " + unitPosition;
    }
}
