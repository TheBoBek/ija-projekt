package ija;

import ija.common.GameEvent;
import ija.common.GameEventType;
import ija.common.Position;
import ija.game.Game;
import ija.game.GameFactory;
import ija.game.TerrainType;
import ija.game.Tile;
import ija.game.Turn;
import ija.game.Unit;
import ija.game.UnitType;
import ija.observer.GameObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@DisplayName("Additional edge cases")
public class AdditionalEdgeCasesTest {

    @Test
    @Disabled("Legacy text loader kept only for compatibility")
    @DisplayName("Parser accepts CSV and compact row format")
    void testParserVariants() {
        Game game = GameFactory.createGame(new String[]{
            "P,F,M",
            "PPW"
        });

        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(unit.getPosition());
        Assertions.assertFalse(reachable.isEmpty(), "Map should be parsed and unit should have some reachable tiles");
    }

    @Test
    @Disabled("Legacy text loader kept only for compatibility")
    @DisplayName("Parser accepts city, factory, and HQ terrain")
    void testParserAcceptsBuildingTerrain() {
        Game game = GameFactory.createGame(new String[]{
            "C T H",
            "CITY FACTORY HQ"
        });

        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(unit.getPosition());
        Assertions.assertTrue(reachable.contains(new Position(0, 1)));
        Assertions.assertTrue(reachable.contains(new Position(1, 0)));
    }

    @Test
    @DisplayName("Position exposes compatible row and col helpers")
    void testPositionRowAndColHelpers() {
        Position position = new Position(3, 5);
        Assertions.assertEquals(position.x(), position.row());
        Assertions.assertEquals(position.y(), position.col());
    }

    @Test
    @DisplayName("TerrainType stores hardcoded terrain values")
    void testTerrainTypeValues() {
        Assertions.assertEquals(2, TerrainType.FOREST.getBonus());
        Assertions.assertEquals(-1, TerrainType.MOUNTAIN.getVehicleMoveCost());
        Assertions.assertEquals(1, TerrainType.CITY.getMoveCost(UnitType.INFANTRY));
        Assertions.assertEquals(2, TerrainType.FOREST.getMoveCost(UnitType.ARTILLERY));
        Assertions.assertEquals("Generuje prijem a leci jednotku", TerrainType.CITY.getMessage());
    }

    @Test
    @DisplayName("TerrainType helper methods identify building terrain")
    void testTerrainTypeHelpers() {
        Assertions.assertTrue(TerrainType.CITY.isBuilding());
        Assertions.assertTrue(TerrainType.FACTORY.isFactory());
        Assertions.assertTrue(TerrainType.HQ.isHq());
        Assertions.assertFalse(TerrainType.WATER.isPassable(UnitType.TANK));
        Assertions.assertFalse(TerrainType.PLAIN.isBuilding());
    }

    @Test
    @DisplayName("UnitType exposes artillery combat-ready metadata")
    void testUnitTypeArtilleryMetadata() {
        Assertions.assertEquals(UnitType.ARTILLERY, UnitType.fromName("Artillery"));
        Assertions.assertEquals(UnitType.ARTILLERY, UnitType.fromName("ARTILLERY"));
        Assertions.assertTrue(UnitType.ARTILLERY.isVehicle());
        Assertions.assertFalse(UnitType.ARTILLERY.isInfantry());
        Assertions.assertEquals(6000, UnitType.ARTILLERY.getCost());
        Assertions.assertEquals(5, UnitType.ARTILLERY.getMovePoints());
        Assertions.assertEquals(2, UnitType.ARTILLERY.getMinAttackRange());
        Assertions.assertEquals(3, UnitType.ARTILLERY.getMaxAttackRange());
        Assertions.assertTrue(UnitType.ARTILLERY.canAttackAtDistance(2));
        Assertions.assertTrue(UnitType.ARTILLERY.canAttackAtDistance(3));
        Assertions.assertFalse(UnitType.ARTILLERY.canAttackAtDistance(1));
        Assertions.assertFalse(UnitType.ARTILLERY.canAttackAtDistance(4));
        Assertions.assertFalse(UnitType.ARTILLERY.canCapture());
        Assertions.assertFalse(UnitType.ARTILLERY.canMoveAndShoot());
    }

    @Test
    @DisplayName("UnitType exposes infantry metadata from units.tsv")
    void testUnitTypeInfantryMetadata() {
        Assertions.assertEquals(UnitType.INFANTRY, UnitType.fromName("Infantry"));
        Assertions.assertEquals(UnitType.INFANTRY, UnitType.fromName("INFANTRY"));
        Assertions.assertFalse(UnitType.INFANTRY.isVehicle());
        Assertions.assertTrue(UnitType.INFANTRY.isInfantry());
        Assertions.assertEquals(1000, UnitType.INFANTRY.getCost());
        Assertions.assertEquals(3, UnitType.INFANTRY.getMovePoints());
        Assertions.assertEquals(1, UnitType.INFANTRY.getMinAttackRange());
        Assertions.assertEquals(1, UnitType.INFANTRY.getMaxAttackRange());
        Assertions.assertTrue(UnitType.INFANTRY.canAttackAtDistance(1));
        Assertions.assertFalse(UnitType.INFANTRY.canAttackAtDistance(2));
        Assertions.assertTrue(UnitType.INFANTRY.canCapture());
        Assertions.assertTrue(UnitType.INFANTRY.canMoveAndShoot());
    }

    @Test
    @DisplayName("UnitType exposes tank metadata from units.tsv")
    void testUnitTypeTankMetadata() {
        Assertions.assertEquals(UnitType.TANK, UnitType.fromName("Tank"));
        Assertions.assertTrue(UnitType.TANK.isVehicle());
        Assertions.assertFalse(UnitType.TANK.isInfantry());
        Assertions.assertEquals(7000, UnitType.TANK.getCost());
        Assertions.assertEquals(6, UnitType.TANK.getMovePoints());
        Assertions.assertEquals(1, UnitType.TANK.getMinAttackRange());
        Assertions.assertEquals(1, UnitType.TANK.getMaxAttackRange());
        Assertions.assertTrue(UnitType.TANK.canAttackAtDistance(1));
        Assertions.assertFalse(UnitType.TANK.canAttackAtDistance(2));
        Assertions.assertFalse(UnitType.TANK.canCapture());
        Assertions.assertTrue(UnitType.TANK.canMoveAndShoot());
    }

    @Test
    @DisplayName("Tile rejects null terrain type")
    void testTileRejectsNullTerrainType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Tile(null));
    }

    @Test
    @DisplayName("Tile without owner starts neutral")
    void testTileDefaultsToNoOwner() {
        Tile tile = new Tile(TerrainType.PLAIN);
        Assertions.assertNull(tile.getOwner());
    }

    @Test
    @DisplayName("Building tile accepts owner in constructor")
    void testTileAcceptsOwnerForBuilding() {
        Tile tile = new Tile(TerrainType.CITY, "P1");
        Assertions.assertEquals("P1", tile.getOwner());
    }

    @Test
    @DisplayName("Building tiles start with full capture points")
    void testBuildingTilesStartWithFullCapturePoints() {
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, new Tile(TerrainType.CITY).getCapturePointsRemaining());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, new Tile(TerrainType.FACTORY).getCapturePointsRemaining());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, new Tile(TerrainType.HQ).getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Non-building tile rejects owner")
    void testTileRejectsOwnerForPlainTile() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new Tile(TerrainType.PLAIN, "P1"));
    }

    @Test
    @DisplayName("Tile rejects blank owner")
    void testTileRejectsBlankOwner() {
        Tile tile = new Tile(TerrainType.CITY);
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> tile.setOwner("   "));
    }

    @Test
    @DisplayName("Building tile owner can be changed and cleared")
    void testTileOwnerMutation() {
        Tile tile = new Tile(TerrainType.FACTORY);
        tile.setOwner("P1");
        Assertions.assertEquals("P1", tile.getOwner());
        tile.clearOwner();
        Assertions.assertNull(tile.getOwner());
    }

    @Test
    @DisplayName("Capture API returns neutral values for non-building tiles")
    void testTileCaptureApiReturnsNeutralValuesForNonBuildingTiles() {
        Tile plain = new Tile(TerrainType.PLAIN);

        Assertions.assertEquals(0, plain.getCapturePointsRemaining());
        plain.resetCapturePoints();
        plain.reduceCapturePoints(1);
        Assertions.assertEquals(0, plain.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Building tile capture points can be reduced and reset")
    void testTileCapturePointsMutation() {
        Tile city = new Tile(TerrainType.CITY, "P1");

        city.reduceCapturePoints(5);
        Assertions.assertEquals(15, city.getCapturePointsRemaining());

        city.reduceCapturePoints(0);
        Assertions.assertEquals(15, city.getCapturePointsRemaining());

        city.reduceCapturePoints(999);
        Assertions.assertEquals(0, city.getCapturePointsRemaining());

        city.resetCapturePoints();
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Building tile rejects negative capture point reduction")
    void testTileRejectsNegativeCapturePointReduction() {
        Tile city = new Tile(TerrainType.CITY);

        Assertions.assertThrows(IllegalArgumentException.class, () -> city.reduceCapturePoints(-1));
    }

    @Test
    @DisplayName("Changing owner resets partial capture progress")
    void testTileOwnerChangeResetsCaptureProgress() {
        Tile city = new Tile(TerrainType.CITY, "P1");
        city.reduceCapturePoints(7);

        city.setOwner("P2");

        Assertions.assertEquals("P2", city.getOwner());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, city.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Clearing owner resets partial capture progress")
    void testTileClearOwnerResetsCaptureProgress() {
        Tile hq = new Tile(TerrainType.HQ, "P1");
        hq.reduceCapturePoints(11);

        hq.clearOwner();

        Assertions.assertNull(hq.getOwner());
        Assertions.assertEquals(Tile.DEFAULT_CAPTURE_POINTS, hq.getCapturePointsRemaining());
    }

    @Test
    @DisplayName("Tile delegates terrain properties")
    void testTileDelegatesTerrainProperties() {
        Tile city = new Tile(TerrainType.CITY);
        Tile water = new Tile(TerrainType.WATER);

        Assertions.assertEquals(TerrainType.CITY.getBonus(), city.getBonus());
        Assertions.assertEquals(TerrainType.CITY.getMessage(), city.getMessage());
        Assertions.assertEquals(TerrainType.WATER.getMoveCost(UnitType.TANK), water.getMoveCost(UnitType.TANK));
        Assertions.assertFalse(water.isPassable(UnitType.TANK));
        Assertions.assertTrue(city.isBuilding());
        Assertions.assertTrue(city.isCity());
    }

    @Test
    @DisplayName("Game exposes map dimensions")
    void testGameDimensions() {
        Game game = GameFactory.createGame(new String[]{
            "C T",
            "P H"
        });

        Assertions.assertEquals(2, game.getHeight());
        Assertions.assertEquals(2, game.getWidth());
    }

    @Test
    @DisplayName("Game returns tile by coordinates and position")
    void testGameReturnsTileAt() {
        Game game = GameFactory.createGame(new String[]{
            "C T",
            "P H"
        });

        Assertions.assertTrue(game.getTileAt(0, 0).isCity());
        Assertions.assertTrue(game.getTileAt(new Position(1, 1)).isHq());
    }

    @Test
    @DisplayName("Game returns unit at occupied position")
    void testGameReturnsUnitAtOccupiedPosition() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });

        Unit unit = game.createUnit("Infantry", "P1", 1, 0);

        Assertions.assertSame(unit, game.getUnitAt(new Position(1, 0)));
    }

    @Test
    @DisplayName("Game returns null when position has no unit")
    void testGameReturnsNullForEmptyPosition() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });

        game.createUnit("Infantry", "P1", 1, 0);

        Assertions.assertNull(game.getUnitAt(new Position(0, 1)));
    }

    @Test
    @DisplayName("Game returns null for unit lookup with null or outside position")
    void testGameReturnsNullForInvalidUnitLookup() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });

        Assertions.assertNull(game.getUnitAt(null));
        Assertions.assertNull(game.getUnitAt(new Position(-1, 0)));
        Assertions.assertNull(game.getUnitAt(new Position(0, 2)));
    }

    @Test
    @DisplayName("Game rejects tile lookup outside map")
    void testGameRejectsTileLookupOutsideMap() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.getTileAt(-1, 0));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.getTileAt(new Position(0, 2)));
    }

    @Test
    @DisplayName("GameFactory creates game from JSON map")
    void testCreateGameFromJson(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "intro-map", "width": 3, "height": 2 },
                  "grid": [
                    ["C", "T", "H"],
                    ["P", "P", "P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" },
                    { "row": 0, "col": 1, "owner": "P1" },
                    { "row": 0, "col": 2, "owner": "P2" }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P1", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Game game = GameFactory.createGameFromJson(jsonFile);

        Assertions.assertEquals(2, game.getHeight());
        Assertions.assertEquals(3, game.getWidth());
        Assertions.assertTrue(game.getTileAt(0, 0).isCity());
        Assertions.assertEquals("P1", game.getTileAt(0, 0).getOwner());
        Assertions.assertTrue(game.getTileAt(0, 1).isFactory());
        Assertions.assertEquals("P1", game.getTileAt(0, 1).getOwner());
        Assertions.assertTrue(game.getTileAt(new Position(0, 2)).isHq());
        Assertions.assertEquals("P2", game.getTileAt(0, 2).getOwner());
        Assertions.assertNotNull(game.getUnitAt(new Position(1, 0)));
        Assertions.assertEquals(UnitType.INFANTRY, game.getUnitAt(new Position(1, 0)).getType());
        Assertions.assertEquals("P1", game.getUnitAt(new Position(1, 0)).getOwner());
    }

    @Test
    @DisplayName("GameFactory creates independent games from JSON maps")
    void testCreateIndependentGamesFromJson(@TempDir Path tempDir) throws IOException {
        Path firstJson = writeJson(
            tempDir,
            "first-map.json",
            """
                {
                  "metadata": { "name": "first-map", "width": 1, "height": 1 },
                  "grid": [
                    ["C"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" }
                  ],
                  "units": []
                }
                """
        );
        Path secondJson = writeJson(
            tempDir,
            "second-map.json",
            """
                {
                  "metadata": { "name": "second-map", "width": 1, "height": 1 },
                  "grid": [
                    ["H"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P2" }
                  ],
                  "units": []
                }
                """
        );

        Game first = GameFactory.createGameFromJson(firstJson);
        Game second = GameFactory.createGameFromJson(secondJson);

        Assertions.assertNotSame(first, second);
        Assertions.assertTrue(first.getTileAt(0, 0).isCity());
        Assertions.assertEquals("P1", first.getTileAt(0, 0).getOwner());
        Assertions.assertTrue(second.getTileAt(0, 0).isHq());
        Assertions.assertEquals("P2", second.getTileAt(0, 0).getOwner());
    }

    @Test
    @DisplayName("GameFactory initializes runtime players and turn from JSON")
    void testCreateGameFromJsonInitializesRuntimeState(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "runtime-map", "width": 1, "height": 1 },
                  "grid": [
                    ["C"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" }
                  ],
                  "units": [],
                  "runtime": {
                    "players": [
                      { "id": "P1", "money": 5000 },
                      { "id": "P2", "money": 1200 }
                    ],
                    "turn": {
                      "currentPlayer": "P2",
                      "turnNumber": 4,
                      "phase": "INCOME"
                    }
                  }
                }
                """
        );

        Game game = GameFactory.createGameFromJson(jsonFile);

        Assertions.assertEquals(5000, game.getPlayer("P1").getMoney());
        Assertions.assertEquals(1200, game.getPlayer("P2").getMoney());
        Turn turn = game.getTurn();
        Assertions.assertEquals("P2", turn.getCurrentPlayer());
        Assertions.assertEquals(4, turn.getTurnNumber());
        Assertions.assertEquals(Turn.Phase.INCOME, turn.getPhase());
    }

    @Test
    @DisplayName("JSON loader rejects unsupported runtime turn phase")
    void testJsonRejectsUnsupportedRuntimeTurnPhase(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "runtime-map", "width": 1, "height": 1 },
                  "grid": [
                    ["C"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" }
                  ],
                  "units": [],
                  "runtime": {
                    "turn": {
                      "currentPlayer": "P1",
                      "turnNumber": 1,
                      "phase": "BAD_PHASE"
                    }
                  }
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects metadata dimension mismatch")
    void testJsonRejectsMetadataMismatch(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 2, "height": 2 },
                  "grid": [
                    ["C", "T", "H"],
                    ["P", "P", "P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" },
                    { "row": 0, "col": 1, "owner": "P1" },
                    { "row": 0, "col": 2, "owner": "P2" }
                  ],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unknown grid token")
    void testJsonRejectsUnknownGridToken(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 2, "height": 1 },
                  "grid": [
                    ["P", "X"]
                  ],
                  "buildings": [],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects duplicate building entry")
    void testJsonRejectsDuplicateBuildingEntry(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 2, "height": 1 },
                  "grid": [
                    ["C", "H"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" },
                    { "row": 0, "col": 0, "owner": "P2" }
                  ],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects building entry outside map")
    void testJsonRejectsBuildingOutsideMap(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 1 },
                  "grid": [
                    ["C"]
                  ],
                  "buildings": [
                    { "row": 1, "col": 0, "owner": "P1" }
                  ],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects building entry on non-building tile")
    void testJsonRejectsBuildingOnPlain(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 1 },
                  "grid": [
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" }
                  ],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects missing building entries")
    void testJsonRejectsMissingBuildingEntries(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 2, "height": 1 },
                  "grid": [
                    ["C", "H"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": "P1" }
                  ],
                  "units": []
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects missing units array")
    void testJsonRejectsMissingUnitsArray(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 1 },
                  "grid": [
                    ["C"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects null unit entry")
    void testJsonRejectsNullUnitEntry(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    null
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with missing type")
    void testJsonRejectsUnitMissingType(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "owner": "P1", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with missing owner")
    void testJsonRejectsUnitMissingOwner(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with blank owner")
    void testJsonRejectsUnitBlankOwner(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "   ", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with missing row")
    void testJsonRejectsUnitMissingRow(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P1", "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with missing col")
    void testJsonRejectsUnitMissingCol(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P1", "row": 1 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with unknown type")
    void testJsonRejectsUnknownUnitType(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "HELICOPTER", "owner": "P1", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit with unsupported owner")
    void testJsonRejectsUnsupportedUnitOwner(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P3", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects unit outside map")
    void testJsonRejectsUnitOutsideMap(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P1", "row": 2, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects duplicate unit position")
    void testJsonRejectsDuplicateUnitPosition(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 3 },
                  "grid": [
                    ["C"],
                    ["P"],
                    ["P"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "INFANTRY", "owner": "P1", "row": 1, "col": 0 },
                    { "type": "TANK", "owner": "P2", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects vehicle unit on mountain")
    void testJsonRejectsVehicleUnitOnMountain(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["M"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "TANK", "owner": "P1", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @DisplayName("JSON loader rejects vehicle unit on water")
    void testJsonRejectsVehicleUnitOnWater(@TempDir Path tempDir) throws IOException {
        Path jsonFile = writeJson(
            tempDir,
            """
                {
                  "metadata": { "name": "bad-map", "width": 1, "height": 2 },
                  "grid": [
                    ["C"],
                    ["W"]
                  ],
                  "buildings": [
                    { "row": 0, "col": 0, "owner": null }
                  ],
                  "units": [
                    { "type": "ARTILLERY", "owner": "P1", "row": 1, "col": 0 }
                  ]
                }
                """
        );

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGameFromJson(jsonFile));
    }

    @Test
    @Disabled("Legacy text loader kept only for compatibility")
    @DisplayName("Factory creates game from map file")
    void testCreateGameFromFile(@TempDir Path tempDir) throws IOException {
        Path mapFile = tempDir.resolve("map.txt");
        Files.writeString(mapFile, "P P P\nP F P\nP P P\n");

        Game game = GameFactory.createGame(mapFile);
        Unit unit = game.createUnit("Tank", "P1", 1, 1);
        Assertions.assertNotNull(unit);
    }

    @Test
    @Disabled("Legacy text loader kept only for compatibility")
    @DisplayName("Parser rejects non-rectangular map")
    void testParserRejectsNonRectangular() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGame(new String[]{"P P", "P"}));
    }

    @Test
    @Disabled("Legacy text loader kept only for compatibility")
    @DisplayName("Parser rejects unknown terrain token")
    void testParserRejectsUnknownToken() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> GameFactory.createGame(new String[]{"P X"}));
    }

    @Test
    @DisplayName("Unit creation rejects unknown unit type")
    void testCreateUnitRejectsUnknownType() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.createUnit("Helicopter", "P1", 0, 0));
    }

    @Test
    @DisplayName("Unit creation accepts artillery type")
    void testCreateUnitAcceptsArtilleryType() {
        Game game = GameFactory.createGame(new String[]{"P P"});

        Unit artillery = game.createUnit("Artillery", "P1", 0, 0);

        Assertions.assertEquals(UnitType.ARTILLERY, artillery.getType());
    }

    @Test
    @DisplayName("Unit creation rejects out-of-bounds position")
    void testCreateUnitRejectsOutOfBounds() {
        Game game = GameFactory.createGame(new String[]{"P"});
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.createUnit("Tank", "P1", -1, 0));
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.createUnit("Tank", "P1", 0, 1));
    }

    @Test
    @DisplayName("Unit creation rejects occupied tile")
    void testCreateUnitRejectsOccupiedTile() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        game.createUnit("Infantry", "P1", 0, 0);
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> game.createUnit("Tank", "P2", 0, 0));
    }

    @Test
    @DisplayName("Reachable tiles do not include origin")
    void testReachableDoesNotIncludeOrigin() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        List<Position> reachable = game.getReachableTiles(unit.getPosition());
        Assertions.assertFalse(reachable.contains(new Position(0, 0)));
    }

    @Test
    @DisplayName("Tank cannot reach tile if only path is too expensive")
    void testWeightedCostBlocksExpensiveOnlyPath() {
        Game game = GameFactory.createGame(new String[]{
            "P F F F P",
            "W W W W W"
        });
        Unit tank = game.createUnit("Tank", "P1", 0, 0);

        List<Position> reachable = game.getReachableTiles(tank.getPosition());
        Assertions.assertFalse(reachable.contains(new Position(0, 4)),
            "Only available path costs 7, tank move points are 6");
    }

    @Test
    @DisplayName("Artillery movement uses vehicle terrain rules and move range 5")
    void testArtilleryMovementRules() {
        Game game = GameFactory.createGame(new String[]{
            "P F F P M",
            "P P W P P"
        });
        Unit artillery = game.createUnit("Artillery", "P1", 0, 0);

        List<Position> reachable = game.getReachableTiles(artillery.getPosition());

        Assertions.assertTrue(reachable.contains(new Position(0, 3)),
            "Artillery should reach the plain tile at total movement cost 5");
        Assertions.assertFalse(reachable.contains(new Position(0, 4)),
            "Artillery is a vehicle and must not enter mountains");
        Assertions.assertFalse(reachable.contains(new Position(1, 2)),
            "Artillery is a vehicle and must not enter water");
    }

    @Test
    @DisplayName("Enemy occupied tile blocks pathfinding")
    void testOccupiedTileBlocksMovementPath() {
        Game game = GameFactory.createGame(new String[]{"P P P"});
        Unit left = game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P2", 0, 1);

        List<Position> reachable = game.getReachableTiles(left.getPosition());
        Assertions.assertFalse(reachable.contains(new Position(0, 1)));
        Assertions.assertFalse(reachable.contains(new Position(0, 2)));
    }

    @Test
    @DisplayName("Friendly occupied tile is pass-through but not a valid destination")
    void testFriendlyOccupiedTileIsPassThrough() {
        Game game = GameFactory.createGame(new String[]{"P P P P"});
        Unit left = game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P1", 0, 1);

        List<Position> reachable = game.getReachableTiles(left.getPosition());
        Assertions.assertFalse(reachable.contains(new Position(0, 1)));
        Assertions.assertTrue(reachable.contains(new Position(0, 2)));
        Assertions.assertTrue(reachable.contains(new Position(0, 3)));
    }

    @Test
    @DisplayName("Move fails when source has no unit")
    void testMoveFailsWithNoUnit() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        boolean moved = game.moveUnit(new Position(0, 0), new Position(0, 1));
        Assertions.assertFalse(moved);
    }

    @Test
    @DisplayName("Move fails when destination is occupied")
    void testMoveFailsToOccupiedDestination() {
        Game game = GameFactory.createGame(new String[]{"P P"});
        Unit first = game.createUnit("Infantry", "P1", 0, 0);
        game.createUnit("Infantry", "P2", 0, 1);

        boolean moved = game.moveUnit(first.getPosition(), new Position(0, 1));
        Assertions.assertFalse(moved);
    }

    @Test
    @DisplayName("Move by zero tiles is valid and keeps unit on place")
    void testMoveByZeroTiles() {
        class CountingObserver implements GameObserver {
            int count = 0;

            @Override
            public void update(GameEvent event) {
                count++;
            }
        }

        Game game = GameFactory.createGame(new String[]{"P"});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        Position origin = unit.getPosition();

        boolean moved = game.moveUnit(origin, origin);

        Assertions.assertTrue(moved);
        Assertions.assertEquals(origin, unit.getPosition());
        Assertions.assertFalse(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());
        Assertions.assertEquals(1, observer.count);
    }

    @Test
    @DisplayName("Observer not notified on invalid move")
    void testObserverNotNotifiedOnInvalidMove() {
        class CountingObserver implements GameObserver {
            int count = 0;

            @Override
            public void update(GameEvent event) {
                count++;
            }
        }

        Game game = GameFactory.createGame(new String[]{
            "P W",
            "P P"
        });
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);

        boolean moved = game.moveUnit(unit.getPosition(), new Position(0, 1));
        Assertions.assertFalse(moved);
        Assertions.assertEquals(0, observer.count);
    }

    @Test
    @DisplayName("Duplicate observer registration does not notify twice")
    void testDuplicateObserverRegistration() {
        class CountingObserver implements GameObserver {
            int count = 0;

            @Override
            public void update(GameEvent event) {
                count++;
            }
        }

        Game game = GameFactory.createGame(new String[]{"P P"});
        CountingObserver observer = new CountingObserver();
        game.addObserver(observer);
        game.addObserver(observer);

        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        boolean moved = game.moveUnit(unit.getPosition(), new Position(0, 1));

        Assertions.assertTrue(moved);
        Assertions.assertEquals(1, observer.count);
    }

    @Test
    @DisplayName("Move emits correct event payload")
    void testMoveEventPayload() {
        class CapturingObserver implements GameObserver {
            GameEvent lastEvent;

            @Override
            public void update(GameEvent event) {
                lastEvent = event;
            }
        }

        Game game = GameFactory.createGame(new String[]{"P P"});
        CapturingObserver observer = new CapturingObserver();
        game.addObserver(observer);
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);
        Position from = unit.getPosition();
        Position to = new Position(0, 1);

        boolean moved = game.moveUnit(from, to);

        Assertions.assertTrue(moved);
        Assertions.assertNotNull(observer.lastEvent);
        Assertions.assertEquals(GameEventType.MOVE, observer.lastEvent.type());
        Assertions.assertEquals(unit, observer.lastEvent.actor());
        Assertions.assertNull(observer.lastEvent.target());
        Assertions.assertEquals(from, observer.lastEvent.from());
        Assertions.assertEquals(to, observer.lastEvent.to());
        Assertions.assertNull(observer.lastEvent.combatResult());
    }

    @Test
    @DisplayName("Healing a full unit keeps hp at 100 and returns zero")
    void testHealFullUnitDoesNothing() {
        Unit unit = new Unit(UnitType.TANK, "P1", new Position(0, 0));

        int healed = unit.heal(20);

        Assertions.assertEquals(0, healed);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Healing a damaged unit returns the real amount restored")
    void testHealDamagedUnitReturnsRestoredHp() throws ReflectiveOperationException {
        Unit unit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        setUnitHp(unit, 70);

        int healed = unit.heal(20);

        Assertions.assertEquals(20, healed);
        Assertions.assertEquals(90, unit.getHp());
    }

    @Test
    @DisplayName("Healing clamps hp to 100 and returns only missing hp")
    void testHealClampsToMaximumHp() throws ReflectiveOperationException {
        Unit unit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        setUnitHp(unit, 85);

        int healed = unit.heal(200);

        Assertions.assertEquals(15, healed);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Healing rejects negative amount")
    void testHealRejectsNegativeAmount() {
        Unit unit = new Unit(UnitType.TANK, "P1", new Position(0, 0));

        Assertions.assertThrows(IllegalArgumentException.class, () -> unit.heal(-1));
    }

    @Test
    @DisplayName("Taking damage from a full unit returns the removed hp")
    void testTakeDamageReducesHp() {
        Unit unit = new Unit(UnitType.TANK, "P1", new Position(0, 0));

        int damage = unit.takeDamage(20);

        Assertions.assertEquals(20, damage);
        Assertions.assertEquals(80, unit.getHp());
        Assertions.assertFalse(unit.isDestroyed());
    }

    @Test
    @DisplayName("Taking zero damage keeps hp unchanged")
    void testTakeZeroDamageDoesNothing() {
        Unit unit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));

        int damage = unit.takeDamage(0);

        Assertions.assertEquals(0, damage);
        Assertions.assertEquals(100, unit.getHp());
    }

    @Test
    @DisplayName("Taking overkill damage clamps hp to zero")
    void testTakeDamageClampsToZero() throws ReflectiveOperationException {
        Unit unit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));
        setUnitHp(unit, 35);

        int damage = unit.takeDamage(200);

        Assertions.assertEquals(35, damage);
        Assertions.assertEquals(0, unit.getHp());
        Assertions.assertTrue(unit.isDestroyed());
    }

    @Test
    @DisplayName("Taking negative damage is rejected")
    void testTakeDamageRejectsNegativeAmount() {
        Unit unit = new Unit(UnitType.TANK, "P1", new Position(0, 0));

        Assertions.assertThrows(IllegalArgumentException.class, () -> unit.takeDamage(-1));
    }

    @Test
    @DisplayName("New unit starts with cleared turn flags")
    void testUnitStartsWithClearedTurnFlags() {
        Unit unit = new Unit(UnitType.INFANTRY, "P1", new Position(0, 0));

        Assertions.assertFalse(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());
    }

    @Test
    @DisplayName("Successful move marks unit as moved but not acted")
    void testSuccessfulMoveMarksMovedFlag() {
        Game game = GameFactory.createGame(new String[]{
            "P P",
            "P P"
        });
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);

        boolean moved = game.moveUnit(new Position(0, 0), new Position(0, 1));

        Assertions.assertTrue(moved);
        Assertions.assertTrue(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());
    }

    @Test
    @DisplayName("Unit cannot move twice in the same turn")
    void testSecondMoveInSameTurnIsRejected() {
        Game game = GameFactory.createGame(new String[]{
            "P P P",
            "P P P"
        });
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);

        boolean firstMove = game.moveUnit(new Position(0, 0), new Position(0, 1));
        boolean secondMove = game.moveUnit(new Position(0, 1), new Position(0, 2));

        Assertions.assertTrue(firstMove);
        Assertions.assertFalse(secondMove);
        Assertions.assertEquals(new Position(0, 1), unit.getPosition());
    }

    @Test
    @DisplayName("Failed move does not change turn flags")
    void testFailedMoveDoesNotChangeTurnFlags() {
        Game game = GameFactory.createGame(new String[]{
            "P W",
            "P P"
        });
        Unit unit = game.createUnit("Infantry", "P1", 0, 0);

        boolean moved = game.moveUnit(new Position(0, 0), new Position(0, 1));

        Assertions.assertFalse(moved);
        Assertions.assertFalse(unit.hasMovedThisTurn());
        Assertions.assertFalse(unit.hasActedThisTurn());
    }

    private Path writeJson(Path tempDir, String json) throws IOException {
        return writeJson(tempDir, "map.json", json);
    }

    private Path writeJson(Path tempDir, String fileName, String json) throws IOException {
        Path jsonFile = tempDir.resolve(fileName);
        Files.writeString(jsonFile, json);
        return jsonFile;
    }

    private void setUnitHp(Unit unit, int hp) throws ReflectiveOperationException {
        Field hpField = Unit.class.getDeclaredField("hp");
        hpField.setAccessible(true);
        hpField.setInt(unit, hp);
    }
}
