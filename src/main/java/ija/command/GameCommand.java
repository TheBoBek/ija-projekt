/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes + test
 *
 * Popis obsahu:
 * - Zdrojový soubor GameCommand v balíku ija.command.
 */
package ija.command;

import ija.game.Game;

/**
 * One executable game action.
 */
public interface GameCommand<T> {
    CommandResult<T> execute(Game game);

    String describe();
}
