package ija.game;

/**
 * One concrete tile on the map.
 */
public final class Tile {
    public static final int DEFAULT_CAPTURE_POINTS = 20;

    private final TerrainType terrainType;
    private String owner;
    private int capturePointsRemaining;

    public Tile(TerrainType terrainType) {
        this(terrainType, null);
    }

    public Tile(TerrainType terrainType, String owner) {
        if (terrainType == null) {
            throw new IllegalArgumentException("Terrain type must not be null");
        }
        this.terrainType = terrainType;
        this.owner = validateOwner(owner);
        this.capturePointsRemaining = initialCapturePoints();
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = validateOwner(owner);
        syncCaptureStateAfterOwnershipChange();
    }

    public void clearOwner() {
        this.owner = null;
        syncCaptureStateAfterOwnershipChange();
    }

    public int getCapturePointsRemaining() {
        requireBuildingTileForCapture();
        return capturePointsRemaining;
    }

    public void resetCapturePoints() {
        requireBuildingTileForCapture();
        capturePointsRemaining = DEFAULT_CAPTURE_POINTS;
    }

    public void reduceCapturePoints(int amount) {
        requireBuildingTileForCapture();
        if (amount < 0) {
            throw new IllegalArgumentException("Capture reduction amount must not be negative");
        }
        capturePointsRemaining = Math.max(0, capturePointsRemaining - amount);
    }

    public int getBonus() {
        return terrainType.getBonus();
    }

    public String getMessage() {
        return terrainType.getMessage();
    }

    public int getMoveCost(UnitType unitType) {
        return terrainType.getMoveCost(unitType);
    }

    public boolean isPassable(UnitType unitType) {
        return terrainType.isPassable(unitType);
    }

    public boolean isBuilding() {
        return terrainType.isBuilding();
    }

    public boolean isCity() {
        return terrainType.isCity();
    }

    public boolean isFactory() {
        return terrainType.isFactory();
    }

    public boolean isHq() {
        return terrainType.isHq();
    }

    private int initialCapturePoints() {
        if (terrainType.isBuilding()) {
            return DEFAULT_CAPTURE_POINTS;
        }
        return 0;
    }

    private void syncCaptureStateAfterOwnershipChange() {
        if (terrainType.isBuilding()) {
            capturePointsRemaining = DEFAULT_CAPTURE_POINTS;
        } else {
            capturePointsRemaining = 0;
        }
    }

    private void requireBuildingTileForCapture() {
        if (!terrainType.isBuilding()) {
            throw new IllegalArgumentException("Capture state exists only for building tiles");
        }
    }

    private String validateOwner(String owner) {
        if (owner == null) {
            return null;
        }
        if (owner.isBlank()) {
            throw new IllegalArgumentException("Owner must not be blank");
        }
        if (!terrainType.isBuilding()) {
            throw new IllegalArgumentException("Only building tiles can have owner");
        }
        return owner;
    }
}
