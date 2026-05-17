/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07)
 *   - 2026-05-07: Extend CombatService + tests + Add factory validation API.
 *
 * Popis obsahu:
 * - Zdrojový soubor CaptureResultTest v balíku ija.
 */
package ija;

import ija.game.CaptureResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Capture result")
public class CaptureResultTest {

    @Test
    @DisplayName("Capture result stores raw capture outcome values")
    void testStoresCaptureOutcomeValues() {
        CaptureResult result = new CaptureResult(true, 7, 12, 5, false, false);

        Assertions.assertTrue(result.progressApplied());
        Assertions.assertEquals(7, result.capturePower());
        Assertions.assertEquals(12, result.capturePointsBefore());
        Assertions.assertEquals(5, result.capturePointsAfter());
        Assertions.assertFalse(result.ownershipChanged());
        Assertions.assertFalse(result.capturedHq());
    }
}
