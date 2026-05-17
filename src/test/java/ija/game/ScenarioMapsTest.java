/** Author: xmarina00 */
package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.EnumSet;

@DisplayName("Bundled playable scenarios")
class ScenarioMapsTest {

    @Test
    @DisplayName("Scenario alpha loads and contains required terrain/buildings")
    void testScenarioAlphaRequirements() {
        assertScenarioMeetsMinimalRequirements(Path.of("data", "maps", "scenario-alpha.json"));
    }

    @Test
    @DisplayName("Scenario beta loads and contains required terrain/buildings")
    void testScenarioBetaRequirements() {
        assertScenarioMeetsMinimalRequirements(Path.of("data", "maps", "scenario-beta.json"));
    }

    private void assertScenarioMeetsMinimalRequirements(Path mapPath) {
        Game game = GameFactory.createGameFromJson(mapPath);
        Assertions.assertTrue(game.getWidth() > 0);
        Assertions.assertTrue(game.getHeight() > 0);

        EnumSet<TerrainType> terrainSeen = EnumSet.noneOf(TerrainType.class);
        boolean p1Hq = false;
        boolean p2Hq = false;
        boolean cityFound = false;

        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                Tile tile = game.getTileAt(position);
                terrainSeen.add(tile.getTerrainType());

                if (tile.isCity()) {
                    cityFound = true;
                }
                if (tile.isHq()) {
                    if ("P1".equals(tile.getOwner())) {
                        p1Hq = true;
                    }
                    if ("P2".equals(tile.getOwner())) {
                        p2Hq = true;
                    }
                }
            }
        }

        Assertions.assertEquals(EnumSet.allOf(TerrainType.class), terrainSeen);
        Assertions.assertTrue(cityFound);
        Assertions.assertTrue(p1Hq);
        Assertions.assertTrue(p2Hq);
    }
}
