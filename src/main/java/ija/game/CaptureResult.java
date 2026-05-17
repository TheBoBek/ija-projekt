/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07)
 *   - 2026-05-07: Extend CombatService + tests + Add factory validation API.
 *
 * Popis obsahu:
 * - Zdrojový soubor CaptureResult v balíku ija.game.
 */
package ija.game;

/**
 * Immutable result of a single capture action attempt.
 */
public record CaptureResult(
    boolean progressApplied,
    int capturePower,
    int capturePointsBefore,
    int capturePointsAfter,
    boolean ownershipChanged,
    boolean capturedHq
) {
}
