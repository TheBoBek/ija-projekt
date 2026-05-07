package ija.game;

/**
 * Stateless service for building ownership checks and local building effects.
 */
public final class BuildingService {
    public static final int CITY_HEAL_AMOUNT = 20;

    public boolean isOwnedBy(Tile tile, String owner) {
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }
        if (owner == null || owner.isBlank()) {
            return false;
        }
        return owner.equals(tile.getOwner());
    }

    public boolean canHeal(Unit unit, Tile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }

        if (!tile.isCity()) {
            return false;
        }
        if (!isOwnedBy(tile, unit.getOwner())) {
            return false;
        }
        if (unit.isDestroyed()) {
            return false;
        }
        return unit.getHp() < 100;
    }

    public int healIfEligible(Unit unit, Tile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }
        if (!canHeal(unit, tile)) {
            return 0;
        }
        return unit.heal(CITY_HEAL_AMOUNT);
    }
}
