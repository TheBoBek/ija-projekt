/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add attack command
 *
 * Popis obsahu:
 * - Zdrojový soubor AttackCommand v balíku ija.command.
 */
package ija.command;

import ija.common.Position;
import ija.game.CombatResult;
import ija.game.Game;

/**
 * Perform attack action.
 */
public final class AttackCommand implements GameCommand<CombatResult> {
    private final Position attacker;
    private final Position defender;

    public AttackCommand(Position attacker, Position defender) {
        this.attacker = attacker;
        this.defender = defender;
    }

    @Override
    public CommandResult<CombatResult> execute(Game game) {
        CombatResult result = game.attack(attacker, defender);
        return new CommandResult<>(result, true);
    }

    @Override
    public String describe() {
        return "Attack " + attacker + " -> " + defender;
    }
}
