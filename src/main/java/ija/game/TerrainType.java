package ija.game;

/**
 * Terrain types used on the game map.
 */
public enum TerrainType {
    PLAIN(1, 1, 1, "Zakladni teren"),
    FOREST(2, 1, 2, "Poskytuje kryti, zpomaluje vozidla"),
    MOUNTAIN(4, 2, -1, "Vyborna obrana, pouze pro pechotu"),
    WATER(0, -1, -1, "Nepruchozi pro vsechny jednotky"),
    CITY(3, 1, 1, "Generuje prijem a leci jednotku"),
    FACTORY(3, 1, 1, "Umoznuje nakup jednotek"),
    HQ(4, 1, 1, "Dobytim nepratelskeho HQ vyhravas");

    private final int bonus;
    private final int infantryMoveCost;
    private final int vehicleMoveCost;
    private final String message;

    TerrainType(int bonus, int infantryMoveCost, int vehicleMoveCost, String message) {
        this.bonus = bonus;
        this.infantryMoveCost = infantryMoveCost;
        this.vehicleMoveCost = vehicleMoveCost;
        this.message = message;
    }

    public int getBonus() {
        return bonus;
    }

    public int getInfantryMoveCost() {
        return infantryMoveCost;
    }

    public int getVehicleMoveCost() {
        return vehicleMoveCost;
    }

    public String getMessage() {
        return message;
    }

    public int getMoveCost(UnitType unitType) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type must not be null");
        }
        if (unitType.isVehicle()) {
            return vehicleMoveCost;
        }
        return infantryMoveCost;
    }

    public boolean isPassable(UnitType unitType) {
        return getMoveCost(unitType) >= 0;
    }

    public boolean isBuilding() {
        return isCity() || isFactory() || isHq();
    }

    public boolean isCity() {
        return this == CITY;
    }

    public boolean isFactory() {
        return this == FACTORY;
    }

    public boolean isHq() {
        return this == HQ;
    }
}
