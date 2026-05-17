/** Author: xmarina00 */
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
