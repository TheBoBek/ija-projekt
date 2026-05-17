/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-02 až 2026-05-17)
 *   - 2026-05-17: Prepare assignment files and simplify engine validation
 *   - 2026-05-11: Add Turn, money and player logic
 *   - 2026-05-02: Add unit load from JSON, Artillery, UnitDamageTable and created tests
 *   - 2026-05-02: Add task 2, implement Tile data handle and refactor TerrainType and TerrainDef.
 * - alegzoo (2026-05-16)
 *   - 2026-05-16: Implement json map loader
 *
 * Popis obsahu:
 * - Zdrojový soubor GameFactory v balíku ija.game.
 */
package ija.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class that creates Game instances from map input.
 */
public final class GameFactory {
    private GameFactory() {
    }

    /**
     * Legacy text-based factory path kept only for compatibility and old fixtures.
     */
    @Deprecated(forRemoval = false)
    public static Game createGame(String[] mapDefinition) {
        return new Game(parseMap(mapDefinition));
    }

    private static String[] splitCompactRow(String row) {
        // Example: "PPMW" -> "P","P","M","W"
        String[] tokens = new String[row.length()];
        for (int i = 0; i < row.length(); i++) {
            tokens[i] = String.valueOf(row.charAt(i));
        }
        return tokens;
    }

    /**
     * Legacy text-file factory path kept only for compatibility and old fixtures.
     */
    @Deprecated(forRemoval = false)
    public static Game createGame(Path mapFile) {
        if (mapFile == null) {
            throw new IllegalArgumentException("Map file path must not be null");
        }

        try {
            // Read every line from map file and parse it like text input.
            List<String> lines = java.nio.file.Files.readAllLines(mapFile);
            return new Game(parseMap(lines.toArray(new String[0])));
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read map file: " + mapFile, e);
        }
    }

    /**
     * Main project factory path for JSON-defined scenarios.
     */

    // Note: In future might be deleted.
    public static Game createGameFromJson(Path jsonFile) {
        JsonMapLoader.LoadedMapData loadedMap = JsonMapLoader.load(jsonFile);
        return createGameFromLoadedMap(loadedMap);
    }

    private static Game createGameFromLoadedMap(JsonMapLoader.LoadedMapData loadedMap) {
        Game game = createGameFromTiles(loadedMap.tiles());
        initializeUnits(game, loadedMap);
        initializePlayerState(game, loadedMap);
        initializeTurn(game, loadedMap);
        return game;
    }

    private static Game createGameFromTiles(Tile[][] tiles) {
        return new Game(tiles);
    }

    private static void initializeUnits(Game game, JsonMapLoader.LoadedMapData loadedMap) {
        for (JsonMapLoader.ScenarioUnitData unit : loadedMap.units()) {
            game.createUnit(unit.type(), unit.owner(), unit.position().row(), unit.position().col());
        }
    }

    private static void initializePlayerState(Game game, JsonMapLoader.LoadedMapData loadedMap) {
        for (JsonMapLoader.ScenarioPlayerState playerState : loadedMap.players()) {
            game.setPlayerMoney(playerState.id(), playerState.money());
        }
    }

    private static void initializeTurn(Game game, JsonMapLoader.LoadedMapData loadedMap) {
        JsonMapLoader.ScenarioTurnState turn = loadedMap.turn();
        if (turn == null) {
            return;
        }
        game.setTurnState(turn.currentPlayer(), turn.turnNumber(), turn.phase());
    }

    /**
     * Legacy text parser kept only for compatibility and old fixtures.
     */
    static Tile[][] parseMap(String[] mapDefinition) {
        if (mapDefinition == null || mapDefinition.length == 0) {
            throw new IllegalArgumentException("Map definition must not be empty");
        }

        List<Tile[]> rows = new ArrayList<>();
        int width = -1;

        for (String rawRow : mapDefinition) {
            // Ignore blank lines to be more tolerant.
            if (rawRow == null || rawRow.trim().isEmpty()) {
                continue;
            }

            // Support spaces, commas, and semicolons as separators.
            String normalizedRow = rawRow.replace(',', ' ').replace(';', ' ').trim();
            String[] tokens = normalizedRow.split("\\s+");
            // Also support compact rows like "PPMW".
            if (tokens.length == 1 && tokens[0].matches("[PpFfMmWwCcTtHh]+")) {
                tokens = splitCompactRow(tokens[0]);
            }
            Tile[] parsedRow = new Tile[tokens.length];

            for (int i = 0; i < tokens.length; i++) {
                parsedRow[i] = new Tile(parseTerrain(tokens[i]));
            }

            if (width == -1) {
                width = parsedRow.length;
            } else if (width != parsedRow.length) {
                throw new IllegalArgumentException("Map must be rectangular");
            }

            rows.add(parsedRow);
        }

        if (rows.isEmpty() || width <= 0) {
            throw new IllegalArgumentException("Map definition must contain at least one row and one column");
        }

        // Convert dynamic list to fixed 2D array.
        Tile[][] map = new Tile[rows.size()][width];
        for (int row = 0; row < rows.size(); row++) {
            System.arraycopy(rows.get(row), 0, map[row], 0, width);
        }

        return map;
    }

    private static TerrainType parseTerrain(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Terrain token must not be null");
        }

        String cleaned = token.replace("\uFEFF", "").trim().toUpperCase();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Empty terrain token");
        }

        return switch (cleaned) {
            case "P", "PLAIN" -> TerrainType.PLAIN;
            case "F", "FOREST" -> TerrainType.FOREST;
            case "M", "MOUNTAIN" -> TerrainType.MOUNTAIN;
            case "W", "WATER" -> TerrainType.WATER;
            case "C", "CITY" -> TerrainType.CITY;
            case "T", "FACTORY" -> TerrainType.FACTORY;
            case "H", "HQ" -> TerrainType.HQ;
            default -> throw new IllegalArgumentException("Unknown terrain token: " + token);
        };
    }
}
