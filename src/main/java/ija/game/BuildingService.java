package ija.game;

/**
 * Stateless service for building ownership checks and local building effects.
 */
public final class BuildingService {
    public static final int BUILDING_HEAL_AMOUNT = 20;

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

        if (!tile.isBuilding()) {
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
        return unit.heal(BUILDING_HEAL_AMOUNT);
    }

    public boolean isCapturableBuilding(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }
        return tile.isCity() || tile.isFactory() || tile.isHq();
    }

    public boolean canCapture(Unit unit, Tile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }
        if (!isCapturableBuilding(tile)) {
            return false;
        }
        if (!unit.getType().canCapture()) {
            return false;
        }
        if (unit.isDestroyed()) {
            return false;
        }
        return !isOwnedBy(tile, unit.getOwner());
    }

    public int getCapturePower(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        return unit.getHp() / 10;
    }

    public boolean isEnemyHq(Unit unit, Tile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }
        return tile.isHq() && !isOwnedBy(tile, unit.getOwner());
    }

    public CaptureResult captureIfEligible(Unit unit, Tile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit must not be null");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile must not be null");
        }

        int before = isCapturableBuilding(tile) ? tile.getCapturePointsRemaining() : 0;
        if (!canCapture(unit, tile)) {
            return new CaptureResult(false, 0, before, before, false, false);
        }

        int capturePower = getCapturePower(unit);
        boolean enemyHq = isEnemyHq(unit, tile);
        if (capturePower <= 0) {
            return new CaptureResult(false, 0, before, before, false, false);
        }

        tile.reduceCapturePoints(capturePower);
        int after = tile.getCapturePointsRemaining();
        boolean ownershipChanged = false;
        boolean capturedHq = false;

        if (after == 0) {
            tile.setOwner(unit.getOwner());
            after = tile.getCapturePointsRemaining();
            ownershipChanged = true;
            capturedHq = enemyHq;
        }

        return new CaptureResult(true, capturePower, before, after, ownershipChanged, capturedHq);
    }
}
