package ija.game;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import ija.common.Position;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads full project map definitions from JSON.
 */
public final class JsonMapLoader {
    private static final Gson GSON = new Gson();
    private static final Set<String> ALLOWED_OWNERS = Set.of("P1", "P2");

    private JsonMapLoader() {
    }

    public static LoadedMapData load(Path jsonFile) {
        if (jsonFile == null) {
            throw new IllegalArgumentException("JSON map path must not be null");
        }

        try (Reader reader = Files.newBufferedReader(jsonFile)) {
            JsonMapDefinition definition = GSON.fromJson(reader, JsonMapDefinition.class);
            if (definition == null) {
                throw new IllegalArgumentException("JSON map definition must not be empty");
            }

            validateMetadata(definition.metadata);
            Tile[][] tiles = parseGrid(definition.grid, definition.metadata);
            applyBuildings(tiles, definition.buildings);
            List<ScenarioUnitData> units = parseUnits(definition.units, tiles);

            return new LoadedMapData(
                definition.metadata.name,
                definition.metadata.width,
                definition.metadata.height,
                tiles,
                units
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read JSON map: " + jsonFile, e);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Invalid JSON map: " + jsonFile, e);
        }
    }

    private static void validateMetadata(MapMetadata metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Map metadata must be present");
        }
        if (metadata.name == null || metadata.name.isBlank()) {
            throw new IllegalArgumentException("Map metadata name must not be blank");
        }
        if (metadata.width == null || metadata.width <= 0) {
            throw new IllegalArgumentException("Map metadata width must be positive");
        }
        if (metadata.height == null || metadata.height <= 0) {
            throw new IllegalArgumentException("Map metadata height must be positive");
        }
    }

    private static Tile[][] parseGrid(List<List<String>> grid, MapMetadata metadata) {
        if (grid == null || grid.isEmpty()) {
            throw new IllegalArgumentException("Grid must not be empty");
        }

        List<Tile[]> rows = new ArrayList<>();
        int width = -1;

        for (int row = 0; row < grid.size(); row++) {
            List<String> jsonRow = grid.get(row);
            if (jsonRow == null || jsonRow.isEmpty()) {
                throw new IllegalArgumentException("Grid row " + row + " must not be empty");
            }

            if (width == -1) {
                width = jsonRow.size();
            } else if (width != jsonRow.size()) {
                throw new IllegalArgumentException("Grid must be rectangular");
            }

            Tile[] parsedRow = new Tile[jsonRow.size()];
            for (int col = 0; col < jsonRow.size(); col++) {
                parsedRow[col] = new Tile(parseGridToken(jsonRow.get(col)));
            }
            rows.add(parsedRow);
        }

        int height = rows.size();
        if (metadata.width != width || metadata.height != height) {
            throw new IllegalArgumentException(
                "Metadata dimensions do not match grid dimensions"
            );
        }

        Tile[][] tiles = new Tile[height][width];
        for (int row = 0; row < height; row++) {
            System.arraycopy(rows.get(row), 0, tiles[row], 0, width);
        }
        return tiles;
    }

    private static TerrainType parseGridToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Grid token must not be null");
        }

        String cleaned = token.trim().toUpperCase();
        if (cleaned.length() != 1) {
            throw new IllegalArgumentException("Grid token must use short notation: " + token);
        }

        return switch (cleaned) {
            case "P" -> TerrainType.PLAIN;
            case "F" -> TerrainType.FOREST;
            case "M" -> TerrainType.MOUNTAIN;
            case "W" -> TerrainType.WATER;
            case "C" -> TerrainType.CITY;
            case "T" -> TerrainType.FACTORY;
            case "H" -> TerrainType.HQ;
            default -> throw new IllegalArgumentException("Unknown grid token: " + token);
        };
    }

    private static void applyBuildings(Tile[][] tiles, List<BuildingDefinition> buildings) {
        if (buildings == null) {
            throw new IllegalArgumentException("Buildings array must be present");
        }

        Set<Position> expectedBuildings = new HashSet<>();
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                if (tiles[row][col].isBuilding()) {
                    expectedBuildings.add(new Position(row, col));
                }
            }
        }

        Set<Position> seenBuildings = new HashSet<>();
        for (BuildingDefinition building : buildings) {
            if (building == null) {
                throw new IllegalArgumentException("Building entry must not be null");
            }
            if (building.row == null || building.col == null) {
                throw new IllegalArgumentException("Building row/col must be present");
            }

            Position position = new Position(building.row, building.col);
            if (!isInside(tiles, position)) {
                throw new IllegalArgumentException("Building is outside map: " + position);
            }

            Tile tile = tiles[position.row()][position.col()];
            if (!tile.isBuilding()) {
                throw new IllegalArgumentException("Building entry must point to CITY, FACTORY, or HQ: " + position);
            }
            if (!seenBuildings.add(position)) {
                throw new IllegalArgumentException("Duplicate building entry at: " + position);
            }

            validateOwner(building.owner);
            if (building.owner == null) {
                tile.clearOwner();
            } else {
                tile.setOwner(building.owner);
            }
        }

        if (!seenBuildings.equals(expectedBuildings)) {
            Set<Position> missing = new HashSet<>(expectedBuildings);
            missing.removeAll(seenBuildings);
            throw new IllegalArgumentException("Missing building entries for: " + missing);
        }
    }

    private static void validateOwner(String owner) {
        if (owner == null) {
            return;
        }
        if (!ALLOWED_OWNERS.contains(owner)) {
            throw new IllegalArgumentException("Unsupported building owner: " + owner);
        }
    }

    private static List<ScenarioUnitData> parseUnits(List<UnitDefinition> units, Tile[][] tiles) {
        if (units == null) {
            throw new IllegalArgumentException("Units array must be present");
        }

        List<ScenarioUnitData> parsedUnits = new ArrayList<>();
        Set<Position> occupiedPositions = new HashSet<>();

        for (UnitDefinition unit : units) {
            if (unit == null) {
                throw new IllegalArgumentException("Unit entry must not be null");
            }
            if (unit.type == null || unit.type.isBlank()) {
                throw new IllegalArgumentException("Unit type must be present");
            }
            if (unit.owner == null) {
                throw new IllegalArgumentException("Unit owner must be present");
            }
            if (unit.owner.isBlank()) {
                throw new IllegalArgumentException("Unit owner must not be blank");
            }
            if (unit.row == null || unit.col == null) {
                throw new IllegalArgumentException("Unit row/col must be present");
            }
            if (!ALLOWED_OWNERS.contains(unit.owner)) {
                throw new IllegalArgumentException("Unsupported unit owner: " + unit.owner);
            }

            UnitType unitType = UnitType.fromName(unit.type);
            if (unitType == null) {
                throw new IllegalArgumentException("Unknown unit type: " + unit.type);
            }

            Position position = new Position(unit.row, unit.col);
            if (!isInside(tiles, position)) {
                throw new IllegalArgumentException("Unit is outside map: " + position);
            }
            if (!occupiedPositions.add(position)) {
                throw new IllegalArgumentException("Duplicate unit position: " + position);
            }
            if (!tiles[position.row()][position.col()].isPassable(unitType)) {
                throw new IllegalArgumentException(
                    "Unit cannot be placed on impassable terrain: " + position
                );
            }

            parsedUnits.add(new ScenarioUnitData(unit.type, unit.owner, position));
        }

        return parsedUnits;
    }

    private static boolean isInside(Tile[][] tiles, Position position) {
        int row = position.row();
        int col = position.col();
        return row >= 0 && row < tiles.length && col >= 0 && col < tiles[0].length;
    }

    public record LoadedMapData(
        String name,
        int width,
        int height,
        Tile[][] tiles,
        List<ScenarioUnitData> units
    ) {
    }

    public record ScenarioUnitData(String type, String owner, Position position) {
    }

    private static final class JsonMapDefinition {
        private MapMetadata metadata;
        private List<List<String>> grid;
        private List<BuildingDefinition> buildings;
        private List<UnitDefinition> units;
    }

    private static final class MapMetadata {
        private String name;
        private Integer width;
        private Integer height;
    }

    private static final class BuildingDefinition {
        private Integer row;
        private Integer col;
        private String owner;
    }

    private static final class UnitDefinition {
        private String type;
        private String owner;
        private Integer row;
        private Integer col;
    }
}
