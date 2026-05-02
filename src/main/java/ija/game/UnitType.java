package ija.game;

/**
 * Unit type with display name and move points.
 */
public enum UnitType {
    TANK("Tank", 6, true, 1, 1, false, true),
    INFANTRY("Infantry", 3, false, 1, 1, true, true),
    ARTILLERY("Artillery", 5, true, 2, 3, false, false);

    private final String displayName;
    private final int movePoints;
    private final boolean vehicle;
    private final int minAttackRange;
    private final int maxAttackRange;
    private final boolean canCapture;
    private final boolean canMoveAndShoot;

    UnitType(
        String displayName,
        int movePoints,
        boolean vehicle,
        int minAttackRange,
        int maxAttackRange,
        boolean canCapture,
        boolean canMoveAndShoot
    ) {
        this.displayName = displayName;
        this.movePoints = movePoints;
        this.vehicle = vehicle;
        this.minAttackRange = minAttackRange;
        this.maxAttackRange = maxAttackRange;
        this.canCapture = canCapture;
        this.canMoveAndShoot = canMoveAndShoot;
    }

    public static UnitType fromName(String name) {
        if (name == null) {
            return null;
        }

        // Accept values like "Tank", "TANK", or "tank".
        String normalized = normalize(name);
        for (UnitType type : values()) {
            if (normalize(type.name()).equals(normalized)) {
                return type;
            }
            if (normalize(type.displayName).equals(normalized)) {
                return type;
            }
        }

        return null;
    }

    public int getMovePoints() {
        return movePoints;
    }

    public boolean isVehicle() {
        return vehicle;
    }

    public boolean isInfantry() {
        return !vehicle;
    }

    public int getMinAttackRange() {
        return minAttackRange;
    }

    public int getMaxAttackRange() {
        return maxAttackRange;
    }

    public boolean canCapture() {
        return canCapture;
    }

    public boolean canMoveAndShoot() {
        return canMoveAndShoot;
    }

    private static String normalize(String value) {
        // Remove spaces/symbols, then compare lower-case text.
        return value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    public String getDisplayName() {
        return displayName;
    }
}
