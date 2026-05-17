/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add unit factory classes
 *
 * Popis obsahu:
 * - Zdrojový soubor UnitFactory v balíku ija.game.
 */
package ija.game;

import ija.common.Position;

/**
 * Factory abstraction for creating unit instances.
 */
public interface UnitFactory {
    Unit createUnit(UnitType type, String owner, Position position);
}
