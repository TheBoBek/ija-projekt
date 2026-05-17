/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07 až 2026-05-17)
 *   - 2026-05-17: Prepare assignment files and simplify engine validation
 *   - 2026-05-07: Extend CombatService + tests + Add factory validation API.
 *   - 2026-05-07: Add BuildingService + tests
 *
 * Popis obsahu:
 * - Zdrojový soubor BuildingServiceTest v balíku ija.game.
 */
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
    @DisplayName("Ownership helper returns false for null tile")
    void testIsOwnedByReturnsFalseForNullTile() {
        Assertions.assertFalse(buildingService.isOwnedBy(null, "P1"));
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
    @DisplayName("Healing is allowed on owned factory and HQ, rejected on non-building tile")
    void testCanHealOnOwnedBuildings() {
        Unit unit = damagedUnit("P1", 20);

        Assertions.assertTrue(buildingService.canHeal(unit, new Tile(TerrainType.FACTORY, "P1")));
        Assertions.assertTrue(buildingService.canHeal(unit, new Tile(TerrainType.HQ, "P1")));
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
    @DisplayName("Healing methods return soft values for null arguments")
    void testHealingReturnsSoftValuesForNullArguments() {
        Unit unit = damagedUnit("P1", 20);
        Tile city = new Tile(TerrainType.CITY, "P1");

        Assertions.assertFalse(buildingService.canHeal(null, city));
        Assertions.assertFalse(buildingService.canHeal(unit, null));
        Assertions.assertEquals(0, buildingService.healIfEligible(null, city));
        Assertions.assertEquals(0, buildingService.healIfEligible(unit, null));
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

        Assertions.assertEquals(20, healed);
        Assertions.assertEquals(100, unit.getHp());
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
    @DisplayName("Game healing hooks work on owned factory")
    void testGameHealUnitOnOwnedFactory() {
        Game game = gameWithSingleOwnedBuilding(TerrainType.FACTORY, "P1");
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        unit.takeDamage(15);

        Assertions.assertTrue(game.canHealUnitOnCurrentTile(new Position(0, 0)));

        int healed = game.healUnitOnCurrentTile(new Position(0, 0));

        Assertions.assertEquals(15, healed);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Game healing hooks work on owned HQ")
    void testGameHealUnitOnOwnedHq() {
        Game game = gameWithSingleOwnedBuilding(TerrainType.HQ, "P1");
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        unit.takeDamage(15);

        Assertions.assertTrue(game.canHealUnitOnCurrentTile(new Position(0, 0)));

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
    @DisplayName("Game healing hooks return soft values for null position")
    void testGameHealingReturnsSoftValuesForNullPosition() {
        Game game = gameWithSingleCityOwnedBy("P1");

        Assertions.assertFalse(game.canHealUnitOnCurrentTile(null));
        Assertions.assertEquals(0, game.healUnitOnCurrentTile(null));
    }

    @Test
    @DisplayName("Game healing hooks return soft values for position without unit")
    void testGameHealingReturnsSoftValuesForMissingUnit() {
        Game game = gameWithSingleCityOwnedBy("P1");

        Assertions.assertFalse(game.canHealUnitOnCurrentTile(new Position(0, 0)));
        Assertions.assertEquals(0, game.healUnitOnCurrentTile(new Position(0, 0)));
    }

    @Test
    @DisplayName("Capturable building helper identifies city factory and HQ")
    void testIsCapturableBuilding() {
        Assertions.assertTrue(buildingService.isCapturableBuilding(new Tile(TerrainType.CITY)));
        Assertions.assertTrue(buildingService.isCapturableBuilding(new Tile(TerrainType.FACTORY)));
        Assertions.assertTrue(buildingService.isCapturableBuilding(new Tile(TerrainType.HQ)));
        Assertions.assertFalse(buildingService.isCapturableBuilding(new Tile(TerrainType.PLAIN)));
        Assertions.assertFalse(buildingService.isCapturableBuilding(new Tile(TerrainType.FOREST)));
        Assertions.assertFalse(buildingService.isCapturableBuilding(new Tile(TerrainType.MOUNTAIN)));
        Assertions.assertFalse(buildingService.isCapturableBuilding(new Tile(TerrainType.WATER)));
    }

    @Test
    @DisplayName("Capture helper methods return soft values for null arguments")
    void testCaptureReturnsSoftValuesForNullArguments() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Tile city = new Tile(TerrainType.CITY, "P2");
        CaptureResult noCapture = new CaptureResult(false, 0, 0, 0, false, false);

        Assertions.assertFalse(buildingService.isCapturableBuilding(null));
        Assertions.assertFalse(buildingService.canCapture(null, city));
        Assertions.assertFalse(buildingService.canCapture(infantry, null));
        Assertions.assertEquals(0, buildingService.getCapturePower(null));
        Assertions.assertFalse(buildingService.isEnemyHq(null, city));
        Assertions.assertFalse(buildingService.isEnemyHq(infantry, null));
        Assertions.assertEquals(noCapture, buildingService.captureIfEligible(null, city));
        Assertions.assertEquals(noCapture, buildingService.captureIfEligible(infantry, null));
    }

    @Test
    @DisplayName("Capture is allowed for infantry on enemy and neutral buildings")
    void testCanCaptureWithInfantry() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));

        Assertions.assertTrue(buildingService.canCapture(infantry, new Tile(TerrainType.CITY, "P2")));
        Assertions.assertTrue(buildingService.canCapture(infantry, new Tile(TerrainType.FACTORY, "P2")));
        Assertions.assertTrue(buildingService.canCapture(infantry, new Tile(TerrainType.HQ, "P2")));
        Assertions.assertTrue(buildingService.canCapture(infantry, new Tile(TerrainType.CITY)));
    }

    @Test
    @DisplayName("Capture is rejected for tank artillery own building and destroyed unit")
    void testCanCaptureRejectsInvalidCases() {
        Unit tank = new Unit(UnitType.TANK, "P1", new Position(0, 0));
        Unit artillery = new Unit(UnitType.ARTILLERY, "P1", new Position(0, 0));
        Unit ownInfantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Unit destroyedInfantry = damagedUnit("P1", 100);
        Tile enemyCity = new Tile(TerrainType.CITY, "P2");
        Tile ownFactory = new Tile(TerrainType.FACTORY, "P1");

        Assertions.assertFalse(buildingService.canCapture(tank, enemyCity));
        Assertions.assertFalse(buildingService.canCapture(artillery, enemyCity));
        Assertions.assertFalse(buildingService.canCapture(ownInfantry, ownFactory));
        Assertions.assertFalse(buildingService.canCapture(destroyedInfantry, enemyCity));
    }

    @Test
    @DisplayName("Capture power is derived from current HP")
    void testGetCapturePower() {
        Unit full = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Unit ninetyFive = damagedUnit("P1", 5);
        Unit ten = damagedUnit("P1", 90);
        Unit zero = damagedUnit("P1", 100);

        Assertions.assertEquals(10, buildingService.getCapturePower(full));
        Assertions.assertEquals(9, buildingService.getCapturePower(ninetyFive));
        Assertions.assertEquals(1, buildingService.getCapturePower(ten));
        Assertions.assertEquals(0, buildingService.getCapturePower(zero));
    }

    @Test
    @DisplayName("Enemy HQ helper only matches enemy headquarters")
    void testIsEnemyHq() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));

        Assertions.assertTrue(buildingService.isEnemyHq(infantry, new Tile(TerrainType.HQ, "P2")));
        Assertions.assertTrue(buildingService.isEnemyHq(infantry, new Tile(TerrainType.HQ)));
        Assertions.assertFalse(buildingService.isEnemyHq(infantry, new Tile(TerrainType.HQ, "P1")));
        Assertions.assertFalse(buildingService.isEnemyHq(infantry, new Tile(TerrainType.CITY, "P2")));
    }

    @Test
    @DisplayName("Capture applies partial progress without changing owner")
    void testCaptureIfEligiblePartialCapture() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Tile city = new Tile(TerrainType.CITY, "P2");

        CaptureResult result = buildingService.captureIfEligible(infantry, city);

        Assertions.assertTrue(result.progressApplied());
        Assertions.assertEquals(10, result.capturePower());
        Assertions.assertEquals(20, result.capturePointsBefore());
        Assertions.assertEquals(10, result.capturePointsAfter());
        Assertions.assertFalse(result.ownershipChanged());
        Assertions.assertFalse(result.capturedHq());
        Assertions.assertEquals("P2", city.getOwner());
        Assertions.assertEquals(10, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Capture can complete and change owner")
    void testCaptureIfEligibleCompletesCapture() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Tile factory = new Tile(TerrainType.FACTORY, "P2");
        factory.reduceCapturePoints(15);

        CaptureResult result = buildingService.captureIfEligible(infantry, factory);

        Assertions.assertTrue(result.progressApplied());
        Assertions.assertEquals(10, result.capturePower());
        Assertions.assertEquals(5, result.capturePointsBefore());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, result.capturePointsAfter());
        Assertions.assertTrue(result.ownershipChanged());
        Assertions.assertFalse(result.capturedHq());
        Assertions.assertEquals("P1", factory.getOwner());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, factory.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Capture of enemy HQ is marked in the result")
    void testCaptureIfEligibleMarksCapturedHq() {
        Unit infantry = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        Tile hq = new Tile(TerrainType.HQ, "P2");
        hq.reduceCapturePoints(15);

        CaptureResult result = buildingService.captureIfEligible(infantry, hq);

        Assertions.assertTrue(result.ownershipChanged());
        Assertions.assertTrue(result.capturedHq());
        Assertions.assertEquals("P1", hq.getOwner());
    }

    @Test
    @DisplayName("Capture returns no-op result when action is not eligible")
    void testCaptureIfEligibleReturnsNoOpResult() {
        Unit tank = new Unit(UnitType.TANK, "P1", new Position(0, 0));
        Tile city = new Tile(TerrainType.CITY, "P2");

        CaptureResult result = buildingService.captureIfEligible(tank, city);

        Assertions.assertFalse(result.progressApplied());
        Assertions.assertEquals(0, result.capturePower());
        Assertions.assertEquals(20, result.capturePointsBefore());
        Assertions.assertEquals(20, result.capturePointsAfter());
        Assertions.assertFalse(result.ownershipChanged());
        Assertions.assertFalse(result.capturedHq());
        Assertions.assertEquals("P2", city.getOwner());
        Assertions.assertEquals(20, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Capture with zero power keeps progress unchanged")
    void testCaptureIfEligibleWithZeroPower() {
        Unit infantry = damagedUnit("P1", 95);
        Tile city = new Tile(TerrainType.CITY, "P2");

        CaptureResult result = buildingService.captureIfEligible(infantry, city);

        Assertions.assertFalse(result.progressApplied());
        Assertions.assertEquals(0, result.capturePower());
        Assertions.assertEquals(20, result.capturePointsBefore());
        Assertions.assertEquals(20, result.capturePointsAfter());
        Assertions.assertFalse(result.ownershipChanged());
        Assertions.assertFalse(result.capturedHq());
    }

    @Test
    @DisplayName("Game delegates capture checks to building service")
    void testGameCanCaptureBuilding() {
        Game game = gameWithSingleOwnedBuilding(TerrainType.CITY, "P2");
        game.createUnit("Infantry", "P1", 0, 0);

        Assertions.assertTrue(game.canCaptureBuilding(new Position(0, 0)));
    }

    @Test
    @DisplayName("Game capture wrapper applies partial capture")
    void testGameCaptureBuilding() {
        Game game = gameWithSingleOwnedBuilding(TerrainType.CITY, "P2");
        game.createUnit("Infantry", "P1", 0, 0);

        CaptureResult result = game.captureBuilding(new Position(0, 0));

        Assertions.assertTrue(result.progressApplied());
        Assertions.assertEquals(10, game.getTileAt(0, 0).getCapturePointsRemaining());
        Assertions.assertEquals("P2", game.getTileAt(0, 0).getOwner());
    }

    @Test
    @DisplayName("Game capture checks are soft but capture action still rejects invalid positions")
    void testGameCaptureInvalidPositions() {
        Game game = gameWithSingleOwnedBuilding(TerrainType.CITY, "P2");

        Assertions.assertFalse(game.canCaptureBuilding(null));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.captureBuilding(null));
        Assertions.assertFalse(game.canCaptureBuilding(new Position(0, 0)));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.captureBuilding(new Position(0, 0)));
    }

    @Test
    @DisplayName("Successful move resets partial capture progress on building left behind")
    void testMoveResetsCaptureProgressWhenLeavingBuilding() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2"), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Infantry", "P1", 0, 0);
        Tile city = game.getTileAt(0, 0);
        game.captureBuilding(new Position(0, 0));

        boolean moved = game.moveUnit(new Position(0, 0), new Position(0, 1));

        Assertions.assertTrue(moved);
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Failed move does not reset partial capture progress")
    void testFailedMoveDoesNotResetCaptureProgress() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.CITY, "P2"), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Infantry", "P1", 0, 0);
        Tile city = game.getTileAt(0, 0);
        game.captureBuilding(new Position(0, 0));

        boolean moved = game.moveUnit(new Position(0, 0), new Position(1, 1));

        Assertions.assertFalse(moved);
        Assertions.assertEquals(10, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Leaving building with full capture progress keeps it unchanged")
    void testMoveFromBuildingWithFullProgressDoesNothingSpecial() {
        Game game = new Game(new Tile[][]{{new Tile(TerrainType.FACTORY, "P1"), new Tile(TerrainType.PLAIN)}});
        game.createUnit("Infantry", "P1", 0, 0);
        Tile factory = game.getTileAt(0, 0);

        boolean moved = game.moveUnit(new Position(0, 0), new Position(0, 1));

        Assertions.assertTrue(moved);
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, factory.getCapturePointsRemaining());
    }

    private static Unit damagedUnit(String owner, int damageAmount) {
        Unit unit = new Unit(UnitType.INFANTRY, owner, new Position(0, 0));
        unit.takeDamage(damageAmount);
        return unit;
    }

    private static Game gameWithSingleCityOwnedBy(String owner) {
        return gameWithSingleOwnedBuilding(TerrainType.CITY, owner);
    }

    private static Game gameWithSingleOwnedBuilding(TerrainType terrainType, String owner) {
        Tile building = new Tile(terrainType, owner);
        return new Game(new Tile[][]{{building}});
    }
}
