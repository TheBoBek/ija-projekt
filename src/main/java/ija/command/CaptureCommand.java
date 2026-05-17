/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add capture command
 *
 * Popis obsahu:
 * - Zdrojový soubor CaptureCommand v balíku ija.command.
 */
package ija.command;

import ija.common.Position;
import ija.game.CaptureResult;
import ija.game.Game;

/**
 * Perform capture action and consume the unit action (wait) if game continues.
 */
public final class CaptureCommand implements GameCommand<CaptureResult> {
    private final Position unitPosition;

    public CaptureCommand(Position unitPosition) {
        this.unitPosition = unitPosition;
    }

    @Override
    public CommandResult<CaptureResult> execute(Game game) {
        CaptureResult result = game.captureBuilding(unitPosition);
        boolean stateChanged = result.progressApplied();
        if (result.progressApplied() && !game.isGameOver()) {
            boolean waited = game.waitUnit(unitPosition);
            stateChanged = stateChanged || waited;
        }
        return new CommandResult<>(result, stateChanged);
    }

    @Override
    public String describe() {
        return "Capture " + unitPosition;
    }
}
