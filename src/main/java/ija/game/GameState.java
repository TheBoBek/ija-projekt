/** Author: xmarina00 */
package ija.game;

import ija.common.Position;

import java.util.List;

/**
 * Snapshot of full runtime game state.
 */
public record GameState(
    TileState[][] tiles,
    List<UnitState> units,
    List<PlayerState> players,
    TurnState turn,
    boolean gameOver,
    String winnerPlayerId
) {
    public record TileState(
        TerrainType terrainType,
        String owner,
        int capturePointsRemaining
    ) {
    }

    public record UnitState(
        UnitType type,
        String owner,
        Position position,
        int hp,
        boolean movedThisTurn,
        boolean actedThisTurn
    ) {
    }

    public record PlayerState(
        String playerId,
        int money
    ) {
    }

    public record TurnState(
        String currentPlayer,
        int turnNumber,
        Turn.Phase phase
    ) {
    }
}
