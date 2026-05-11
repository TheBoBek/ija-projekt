package ija.game;

/**
 * Mutable runtime metadata for the current turn.
 */
public final class Turn {
    public enum Phase {
        INCOME,
        ACTION
    }

    private String currentPlayer;
    private int turnNumber;
    private Phase phase;

    public Turn(String currentPlayer, int turnNumber, Phase phase) {
        setCurrentPlayer(currentPlayer);
        setTurnNumber(turnNumber);
        setPhase(phase);
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        if (currentPlayer == null || currentPlayer.isBlank()) {
            throw new IllegalArgumentException("Current player must not be blank");
        }
        this.currentPlayer = currentPlayer;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        if (turnNumber <= 0) {
            throw new IllegalArgumentException("Turn number must be positive");
        }
        this.turnNumber = turnNumber;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        if (phase == null) {
            throw new IllegalArgumentException("Turn phase must not be null");
        }
        this.phase = phase;
    }
}
