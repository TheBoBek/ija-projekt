package ija.common;

import ija.game.Unit;

/**
 * Event object sent to observers after game actions.
 */
public record GameEvent(String type, Unit unit, Position from, Position to) {
}
