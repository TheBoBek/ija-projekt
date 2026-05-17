/** Author: xmarina00, xbobkos00 */
package ija.game;

import ija.common.Position;

/**
 * One unit on the board with owner, type, position, and HP.
 */
public final class Unit {
    private static final int DEFAULT_HP = 100;

    private final UnitType type;
    private final String owner;
    private Position position;
    private int hp;
    private boolean movedThisTurn;
    private boolean actedThisTurn;

    public Unit(UnitType type, String owner, Position position) {
        this.type = type;
        this.owner = owner;
        this.position = position;
        this.hp = DEFAULT_HP;
    }

    public UnitType getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public Position getPosition() {
        return position;
    }

    public int getHp() {
        return hp;
    }

    public boolean isDestroyed() {
        return hp == 0;
    }

    public boolean hasMovedThisTurn() {
        return movedThisTurn;
    }

    public boolean hasActedThisTurn() {
        return actedThisTurn;
    }

    public int heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount must not be negative");
        }

        int before = hp;
        hp = Math.min(DEFAULT_HP, hp + amount);
        return hp - before;
    }

    public int takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount must not be negative");
        }

        int before = hp;
        hp = Math.max(0, hp - amount);
        return before - hp;
    }

    void markMovedThisTurn() {
        movedThisTurn = true;
    }

    void markActedThisTurn() {
        actedThisTurn = true;
    }

    void resetTurnFlags() {
        movedThisTurn = false;
        actedThisTurn = false;
    }

    void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "{" + type.getDisplayName() + "[" + position.x() + "," + position.y() + "][" + hp + "]}";
    }
}
