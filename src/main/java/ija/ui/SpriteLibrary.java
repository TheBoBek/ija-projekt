/** Author: xmarina00 */
package ija.ui;

import ija.game.TerrainType;
import ija.game.Tile;
import ija.game.Unit;
import ija.game.UnitType;
import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Lazy image loader for terrain and unit sprites.
 */
final class SpriteLibrary {
    private final Path terrainDirectory = resolveTerrainDirectory();
    private final Map<String, Image> cache = new HashMap<>();

    private static Path resolveTerrainDirectory() {
        Path preferred = Path.of("lib", "terrain");
        if (Files.exists(preferred)) {
            return preferred;
        }
        return Path.of("assets", "terrain");
    }

    Image terrain(Tile tile) {
        if (tile == null) {
            return null;
        }
        String fileName = terrainFileName(tile);
        return load(fileName);
    }

    Image unit(Unit unit) {
        if (unit == null) {
            return null;
        }
        String fileName = unitFileName(unit);
        return load(fileName);
    }

    private Image load(String fileName) {
        if (fileName == null) {
            return null;
        }
        return cache.computeIfAbsent(fileName, this::tryLoadImage);
    }

    private Image tryLoadImage(String fileName) {
        Path fullPath = terrainDirectory.resolve(fileName);
        if (!Files.exists(fullPath)) {
            return null;
        }
        return new Image(fullPath.toUri().toString(), true);
    }

    private String terrainFileName(Tile tile) {
        TerrainType terrain = tile.getTerrainType();
        return switch (terrain) {
            case PLAIN -> "grass.png";
            case FOREST -> "forest.png";
            case MOUNTAIN -> "mountain.png";
            case WATER -> "water.png";
            case CITY -> {
                if ("P1".equals(tile.getOwner())) {
                    yield "city-red.png";
                }
                if ("P2".equals(tile.getOwner())) {
                    yield "city-blue.png";
                }
                yield "city-neutral.png";
            }
            case FACTORY -> {
                if ("P1".equals(tile.getOwner())) {
                    yield "factory-red.png";
                }
                if ("P2".equals(tile.getOwner())) {
                    yield "factory-blue.png";
                }
                yield "factory-neutral.png";
            }
            case HQ -> {
                if ("P1".equals(tile.getOwner())) {
                    yield "hq-red.png";
                }
                if ("P2".equals(tile.getOwner())) {
                    yield "hq-blue.png";
                }
                yield "hq-neutral.png";
            }
        };
    }

    private String unitFileName(Unit unit) {
        String color = "P2".equals(unit.getOwner()) ? "blue" : "red";
        UnitType type = unit.getType();
        return switch (type) {
            case INFANTRY -> "soldier-" + color + ".png";
            case TANK -> "tank-" + color + ".png";
            case ARTILLERY -> "artillery-" + color + ".png";
        };
    }
}
