/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-07 až 2026-05-17)
 *   - 2026-05-17: Prepare assignment files and simplify engine validation
 *   - 2026-05-07: Extend CombatService + tests + Add factory validation API.
 *
 * Popis obsahu:
 * - Zdrojový soubor CombatServiceTest v balíku ija.game.
 */
package ija.game;

import ija.common.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Combat service")
class CombatServiceTest {

    @Test
    @DisplayName("Combat service can be created with a damage table")
    void testCreatesWithDamageTable() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertNotNull(combatService);
    }

    @Test
    @DisplayName("Combat service rejects null damage table")
    void testRejectsNullDamageTable() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new CombatService(null)
        );
    }

    @Test
    @DisplayName("canAttack rejects null attacker")
    void testCanAttackRejectsNullAttacker() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);

        Assertions.assertFalse(combatService.canAttack(null, defender));
    }

    @Test
    @DisplayName("canAttack rejects null defender")
    void testCanAttackRejectsNullDefender() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);

        Assertions.assertFalse(combatService.canAttack(attacker, null));
    }

    @Test
    @DisplayName("canAttack rejects same object")
    void testCanAttackRejectsSameObject() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);

        Assertions.assertFalse(combatService.canAttack(attacker, attacker));
    }

    @Test
    @DisplayName("canAttack rejects destroyed attacker")
    void testCanAttackRejectsDestroyedAttacker() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        attacker.takeDamage(100);

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack rejects destroyed defender")
    void testCanAttackRejectsDestroyedDefender() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        defender.takeDamage(100);

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack rejects same owner")
    void testCanAttackRejectsSameOwner() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P1", 0, 1);

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack rejects attacker that already acted")
    void testCanAttackRejectsAttackerThatAlreadyActed() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        attacker.markActedThisTurn();

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack rejects artillery after movement")
    void testCanAttackRejectsArtilleryAfterMovement() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 2);
        attacker.markMovedThisTurn();

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack allows infantry after movement when target is in range")
    void testCanAttackAllowsInfantryAfterMovement() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        attacker.markMovedThisTurn();

        Assertions.assertTrue(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack allows tank in melee range")
    void testCanAttackAllowsTankAtRangeOne() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.TANK, "P1", 0, 0);
        Unit defender = unit(UnitType.INFANTRY, "P2", 0, 1);

        Assertions.assertTrue(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack rejects tank out of range")
    void testCanAttackRejectsTankAtRangeTwo() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.TANK, "P1", 0, 0);
        Unit defender = unit(UnitType.INFANTRY, "P2", 0, 2);

        Assertions.assertFalse(combatService.canAttack(attacker, defender));
    }

    @Test
    @DisplayName("canAttack allows artillery at range two and three")
    void testCanAttackAllowsArtilleryAtRangeTwoAndThree() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);

        Assertions.assertTrue(combatService.canAttack(attacker, unit(UnitType.TANK, "P2", 0, 2)));
        Assertions.assertTrue(combatService.canAttack(attacker, unit(UnitType.TANK, "P2", 0, 3)));
    }

    @Test
    @DisplayName("canAttack rejects artillery at range one and four")
    void testCanAttackRejectsArtilleryAtInvalidRanges() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);

        Assertions.assertFalse(combatService.canAttack(attacker, unit(UnitType.TANK, "P2", 0, 1)));
        Assertions.assertFalse(combatService.canAttack(attacker, unit(UnitType.TANK, "P2", 0, 4)));
    }

    @Test
    @DisplayName("calculateDamage returns zero when attacker hp is zero")
    void testCalculateDamageReturnsZeroForZeroHpAttacker() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(0, combatService.calculateDamage(75, 0, 0));
    }

    @Test
    @DisplayName("calculateDamage returns zero when base damage is zero")
    void testCalculateDamageReturnsZeroForZeroBaseDamage() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(0, combatService.calculateDamage(0, 100, 0));
    }

    @Test
    @DisplayName("calculateDamage returns base damage when attacker is full hp and defender has no bonus")
    void testCalculateDamageWithoutDefenseBonus() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(75, combatService.calculateDamage(75, 100, 0));
    }

    @Test
    @DisplayName("calculateDamage applies defender terrain bonus")
    void testCalculateDamageWithTerrainBonus() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(52, combatService.calculateDamage(75, 100, 3));
    }

    @Test
    @DisplayName("calculateDamage scales with attacker current hp")
    void testCalculateDamageWithReducedAttackerHp() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(30, combatService.calculateDamage(75, 40, 0));
    }

    @Test
    @DisplayName("calculateDamage matches combined formula example")
    void testCalculateDamageCombinedExample() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Assertions.assertEquals(30, combatService.calculateDamage(70, 55, 2));
    }

    @Test
    @DisplayName("resolveAttack rejects null attacker")
    void testResolveAttackRejectsNullAttacker() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        Tile tile = new Tile(TerrainType.PLAIN);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(null, tile, defender, tile)
        );
    }

    @Test
    @DisplayName("resolveAttack rejects null attacker tile")
    void testResolveAttackRejectsNullAttackerTile() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        Tile defenderTile = new Tile(TerrainType.PLAIN);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(attacker, null, defender, defenderTile)
        );
    }

    @Test
    @DisplayName("resolveAttack rejects null defender")
    void testResolveAttackRejectsNullDefender() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Tile attackerTile = new Tile(TerrainType.PLAIN);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(attacker, attackerTile, null, attackerTile)
        );
    }

    @Test
    @DisplayName("resolveAttack rejects null defender tile")
    void testResolveAttackRejectsNullDefenderTile() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        Tile attackerTile = new Tile(TerrainType.PLAIN);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(attacker, attackerTile, defender, null)
        );
    }

    @Test
    @DisplayName("resolveAttack rejects attack that canAttack would refuse")
    void testResolveAttackRejectsInvalidAttack() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P1", 0, 1);
        Tile tile = new Tile(TerrainType.PLAIN);

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(attacker, tile, defender, tile)
        );
    }

    @Test
    @DisplayName("resolveAttack performs melee exchange and marks attacker as acted")
    void testResolveAttackMeleeExchange() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.TANK, "P1", 0, 0);
        Unit defender = unit(UnitType.INFANTRY, "P2", 0, 1);
        Tile attackerTile = new Tile(TerrainType.PLAIN);
        Tile defenderTile = new Tile(TerrainType.PLAIN);

        CombatResult result = combatService.resolveAttack(attacker, attackerTile, defender, defenderTile);

        Assertions.assertEquals(67, result.damageToDefender());
        Assertions.assertEquals(1, result.damageToAttacker());
        Assertions.assertEquals(33, result.defenderHpAfter());
        Assertions.assertEquals(99, result.attackerHpAfter());
        Assertions.assertTrue(result.counterattackPerformed());
        Assertions.assertTrue(attacker.hasActedThisTurn());
        Assertions.assertFalse(defender.hasActedThisTurn());
        Assertions.assertEquals(33, defender.getHp());
        Assertions.assertEquals(99, attacker.getHp());
    }

    @Test
    @DisplayName("resolveAttack stops after defender is destroyed")
    void testResolveAttackStopsWhenDefenderDies() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);
        Unit defender = unit(UnitType.INFANTRY, "P2", 0, 2);
        Tile attackerTile = new Tile(TerrainType.PLAIN);
        Tile defenderTile = new Tile(TerrainType.PLAIN);
        defender.takeDamage(20);

        CombatResult result = combatService.resolveAttack(attacker, attackerTile, defender, defenderTile);

        Assertions.assertEquals(80, result.damageToDefender());
        Assertions.assertEquals(0, result.damageToAttacker());
        Assertions.assertTrue(result.defenderDestroyed());
        Assertions.assertFalse(result.counterattackPerformed());
        Assertions.assertEquals(0, defender.getHp());
        Assertions.assertEquals(100, attacker.getHp());
    }

    @Test
    @DisplayName("resolveAttack counterattack uses attacker tile bonus")
    void testResolveAttackCounterattackUsesAttackerTileBonus() {
        CombatService combatService = new CombatService(new UnitDamageTable());

        Unit plainAttacker = unit(UnitType.TANK, "P1", 0, 0);
        Unit plainDefender = unit(UnitType.INFANTRY, "P2", 0, 1);
        CombatResult plainResult = combatService.resolveAttack(
            plainAttacker,
            new Tile(TerrainType.PLAIN),
            plainDefender,
            new Tile(TerrainType.PLAIN)
        );

        Unit hqAttacker = unit(UnitType.TANK, "P1", 0, 0);
        Unit hqDefender = unit(UnitType.INFANTRY, "P2", 0, 1);
        CombatResult hqResult = combatService.resolveAttack(
            hqAttacker,
            new Tile(TerrainType.HQ),
            hqDefender,
            new Tile(TerrainType.PLAIN)
        );

        Assertions.assertEquals(1, plainResult.damageToAttacker());
        Assertions.assertEquals(0, hqResult.damageToAttacker());
        Assertions.assertTrue(plainResult.counterattackPerformed());
        Assertions.assertTrue(hqResult.counterattackPerformed());
    }

    @Test
    @DisplayName("resolveAttack artillery can fire at range without melee counterattack")
    void testResolveAttackArtilleryAtRange() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 2);
        Tile attackerTile = new Tile(TerrainType.PLAIN);
        Tile defenderTile = new Tile(TerrainType.PLAIN);

        CombatResult result = combatService.resolveAttack(attacker, attackerTile, defender, defenderTile);

        Assertions.assertEquals(63, result.damageToDefender());
        Assertions.assertEquals(0, result.damageToAttacker());
        Assertions.assertEquals(37, result.defenderHpAfter());
        Assertions.assertEquals(100, result.attackerHpAfter());
        Assertions.assertFalse(result.counterattackPerformed());
        Assertions.assertTrue(attacker.hasActedThisTurn());
    }

    @Test
    @DisplayName("resolveAttack rejects artillery after movement")
    void testResolveAttackRejectsArtilleryAfterMovement() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.ARTILLERY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 2);
        Tile tile = new Tile(TerrainType.PLAIN);
        attacker.markMovedThisTurn();

        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> combatService.resolveAttack(attacker, tile, defender, tile)
        );
    }

    @Test
    @DisplayName("resolveAttack still consumes action when primary damage is zero")
    void testResolveAttackConsumesActionWhenPrimaryDamageIsZero() {
        CombatService combatService = new CombatService(new UnitDamageTable());
        Unit attacker = unit(UnitType.INFANTRY, "P1", 0, 0);
        Unit defender = unit(UnitType.TANK, "P2", 0, 1);
        Tile attackerTile = new Tile(TerrainType.PLAIN);
        Tile defenderTile = new Tile(TerrainType.HQ);
        attacker.takeDamage(90);

        CombatResult result = combatService.resolveAttack(attacker, attackerTile, defender, defenderTile);

        Assertions.assertEquals(0, result.damageToDefender());
        Assertions.assertTrue(attacker.hasActedThisTurn());
    }

    private static Unit unit(UnitType type, String owner, int row, int col) {
        return new Unit(type, owner, new Position(row, col));
    }
}
