package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Building service")
class BuildingServiceTest {
    private final BuildingService buildingService = new BuildingService();

    @Test
    @DisplayName("Ownership helper returns true for matching owner")
    void testIsOwnedByMatchingOwner() {
        Tile city = new Tile(TerrainType.CITY, "P1");

        Assertions.assertTrue(buildingService.isOwnedBy(city, "P1"));
    }

    @Test
    @DisplayName("Ownership helper returns false for null, blank, and different owner")
    void testIsOwnedByRejectsInvalidOwnerQueries() {
        Tile city = new Tile(TerrainType.CITY, "P1");

        Assertions.assertFalse(buildingService.isOwnedBy(city, null));
        Assertions.assertFalse(buildingService.isOwnedBy(city, "   "));
        Assertions.assertFalse(buildingService.isOwnedBy(city, "P2"));
    }

    @Test
    @DisplayName("Ownership helper rejects null tile")
    void testIsOwnedByRejectsNullTile() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> buildingService.isOwnedBy(null, "P1"));
    }

    @Test
    @DisplayName("Healing is allowed for damaged unit on owned city")
    void testCanHealOnOwnedCity() {
        Unit unit = damagedUnit("P1", 20);
        Tile city = new Tile(TerrainType.CITY, "P1");

        Assertions.assertTrue(buildingService.canHeal(unit, city));
    }

    @Test
    @DisplayName("Healing is rejected on enemy or neutral city")
    void testCanHealRejectsEnemyOrNeutralCity() {
        Unit unit = damagedUnit("P1", 20);

        Assertions.assertFalse(buildingService.canHeal(unit, new Tile(TerrainType.CITY, "P2")));
        Assertions.assertFalse(buildingService.canHeal(unit, new Tile(TerrainType.CITY)));
    }

    @Test
    @DisplayName("Healing is rejected on non-city tiles")
    void testCanHealRejectsNonCityTiles() {
        Unit unit = damagedUnit("P1", 20);

        Assertions.assertFalse(buildingService.canHeal(unit, new Tile(TerrainType.FACTORY, "P1")));
        Assertions.assertFalse(buildingService.canHeal(unit, new Tile(TerrainType.HQ, "P1")));
        Assertions.assertFalse(buildingService.canHeal(unit, new Tile(TerrainType.PLAIN)));
    }

    @Test
    @DisplayName("Healing is rejected for full hp or destroyed unit")
    void testCanHealRejectsFullOrDestroyedUnit() {
        Tile city = new Tile(TerrainType.CITY, "P1");
        Unit fullUnit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Unit destroyedUnit = damagedUnit("P1", 100);

        Assertions.assertFalse(buildingService.canHeal(fullUnit, city));
        Assertions.assertFalse(buildingService.canHeal(destroyedUnit, city));
    }

    @Test
    @DisplayName("Healing methods reject null arguments")
    void testHealingRejectsNullArguments() {
        Unit unit = damagedUnit("P1", 20);
        Tile city = new Tile(TerrainType.CITY, "P1");

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> buildingService.canHeal(null, city));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> buildingService.canHeal(unit, null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> buildingService.healIfEligible(null, city));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> buildingService.healIfEligible(unit, null));
    }

    @Test
    @DisplayName("Healing returns real restored hp")
    void testHealIfEligibleReturnsRealAmount() {
        Unit unit = damagedUnit("P1", 15);
        Tile city = new Tile(TerrainType.CITY, "P1");

        int healed = buildingService.healIfEligible(unit, city);

        Assertions.assertEquals(15, healed);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Healing returns zero when not eligible")
    void testHealIfEligibleReturnsZeroWhenNotEligible() {
        Unit unit = damagedUnit("P1", 20);
        Tile factory = new Tile(TerrainType.FACTORY, "P1");

        int healed = buildingService.healIfEligible(unit, factory);

        Assertions.assertEquals(0, healed);
        Assertions.assertEquals(80, unit.getHp());
    }

    @Test
    @DisplayName("Game delegates healing checks to building service")
    void testGameCanHealUnitOnCurrentTile() {
        Game game = gameWithSingleCityOwnedBy("P1");
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        unit.takeDamage(20);

        Assertions.assertTrue(game.canHealUnitOnCurrentTile(new Position(0, 0)));
    }

    @Test
    @DisplayName("Game healing hook updates unit hp")
    void testGameHealUnitOnCurrentTile() {
        Game game = gameWithSingleCityOwnedBy("P1");
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        unit.takeDamage(15);

        int healed = game.healUnitOnCurrentTile(new Position(0, 0));

        Assertions.assertEquals(15, healed);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Game healing hook returns zero when tile is not healable")
    void testGameHealUnitReturnsZeroWhenNotHealable() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.PLAIN)}});
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        unit.takeDamage(20);

        int healed = game.healUnitOnCurrentTile(new Position(0, 0));

        Assertions.assertEquals(0, healed);
        Assertions.assertEquals(80, unit.getHp());
    }

    @Test
    @DisplayName("Game healing hooks reject null position")
    void testGameHealingRejectsNullPosition() {
        Game game = gameWithSingleCityOwnedBy("P1");

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.canHealUnitOnCurrentTile(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.healUnitOnCurrentTile(null));
    }

    @Test
    @DisplayName("Game healing hooks reject position without unit")
    void testGameHealingRejectsMissingUnit() {
        Game game = gameWithSingleCityOwnedBy("P1");

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.canHealUnitOnCurrentTile(new Position(0, 0)));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.healUnitOnCurrentTile(new Position(0, 0)));
    }

    private static Unit damagedUnit(String owner, int damageAmount) {
        Unit unit = new Unit(UnitType.INFANTRY, owner, new Position(0, 0));
        unit.takeDamage(damageAmount);
        return unit;
    }

    private static Game gameWithSingleCityOwnedBy(String owner) {
        Tile city = new Tile(TerrainType.CITY, owner);
        return new Game(new Tile[][]{{city}});
    }
}
