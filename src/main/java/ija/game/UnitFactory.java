/** Author: xmarina00 */
package ija.game;

import ija.common.Position;

/**
 * Factory abstraction for creating unit instances.
 */
public interface UnitFactory {
    Unit createUnit(UnitType type, String owner, Position position);
}
