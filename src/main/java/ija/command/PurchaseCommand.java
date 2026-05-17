/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes
 *
 * Popis obsahu:
 * - Zdrojový soubor PurchaseCommand v balíku ija.command.
 */
package ija.command;

import ija.common.Position;
import ija.game.Game;
import ija.game.Unit;

/**
 * Buy one unit on factory tile.
 */
public final class PurchaseCommand implements GameCommand<Unit> {
    private final String unitTypeName;
    private final Position factoryPosition;

    public PurchaseCommand(String unitTypeName, Position factoryPosition) {
        this.unitTypeName = unitTypeName;
        this.factoryPosition = factoryPosition;
    }

    @Override
    public CommandResult<Unit> execute(Game game) {
        Unit unit = game.purchaseUnit(unitTypeName, factoryPosition);
        return new CommandResult<>(unit, true);
    }

    @Override
    public String describe() {
        return "Purchase " + unitTypeName + " at " + factoryPosition;
    }
}
