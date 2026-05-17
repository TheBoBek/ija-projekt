/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-16)
 *   - 2026-05-16: Add game event types
 * - TheBoBek (2026-05-11)
 *   - 2026-05-11: Add Turn, money and player logic
 *
 * Popis obsahu:
 * - Zdrojový soubor GameEventType v balíku ija.common.
 */
package ija.common;

/**
 * Supported game event categories emitted to observers.
 */
public enum GameEventType {
    MOVE,
    ATTACK,
    WAIT,
    CAPTURE,
    PURCHASE,
    INCOME,
    END_TURN
}
