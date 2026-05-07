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
