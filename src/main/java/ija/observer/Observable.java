/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-02)
 *   - 2026-05-02: Add task 2, implement Tile data handle and refactor TerrainType and TerrainDef.
 *
 * Popis obsahu:
 * - Zdrojový soubor Observable v balíku ija.observer.
 */
package ija.observer;

/**
 * Basic observable API for the game model.
 */
public interface Observable {
    /**
     * Add observer to notification list.
     */
    void addObserver(GameObserver observer);

    /**
     * Remove observer from notification list.
     */
    void removeObserver(GameObserver observer);
}
