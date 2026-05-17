/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-02)
 *   - 2026-05-02: Add task 2, implement Tile data handle and refactor TerrainType and TerrainDef.
 *
 * Popis obsahu:
 * - Zdrojový soubor Position v balíku ija.common.
 */
package ija.common;

/**
 * Simple coordinate on the map.
 */
public record Position(int x, int y) {
    public int row() {
        return x;
    }

    public int col() {
        return y;
    }
}
