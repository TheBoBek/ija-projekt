package ija.game;

/**
 * One concrete tile on the map.
 */
public final class Tile {
    private final TerrainType terrainType;
    private String owner;

    public Tile(TerrainType terrainType) {
        this(terrainType, null);
    }

    public Tile(TerrainType terrainType, String owner) {
        if (terrainType == null) {
            throw new IllegalArgumentException("Terrain type must not be null");
        }
        this.terrainType = terrainType;
        this.owner = validateOwner(owner);
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = validateOwner(owner);
    }

    public void clearOwner() {
        this.owner = null;
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
