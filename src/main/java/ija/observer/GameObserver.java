/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-02)
 *   - 2026-05-02: Add task 2, implement Tile data handle and refactor TerrainType and TerrainDef.
 *
 * Popis obsahu:
 * - Zdrojový soubor GameObserver v balíku ija.observer.
 */
package ija.observer;

import ija.common.GameEvent;

/**
 * Observer callback used by Game.
 */
public interface GameObserver {
    /**
     * Called when Game emits new event.
     */
    void update(GameEvent event);
}
