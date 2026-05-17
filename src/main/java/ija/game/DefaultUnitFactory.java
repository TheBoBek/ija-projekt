/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add unit factory classes
 *
 * Popis obsahu:
 * - Zdrojový soubor DefaultUnitFactory v balíku ija.game.
 */
package ija.game;

import ija.common.Position;

/**
 * Default unit factory used by the game engine.
 */
public final class DefaultUnitFactory implements UnitFactory {
    @Override
    public Unit createUnit(UnitType type, String owner, Position position) {
        return new Unit(type, owner, position);
    }
}
