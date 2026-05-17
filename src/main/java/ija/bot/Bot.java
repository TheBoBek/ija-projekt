/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add bot + test
 *
 * Popis obsahu:
 * - Zdrojový soubor Bot v balíku ija.bot.
 */
package ija.bot;

import ija.game.Game;

/**
 * Player API for executing a full turn without UI clicks
 */
public interface Bot {
    void playTurn(Game game);
}
