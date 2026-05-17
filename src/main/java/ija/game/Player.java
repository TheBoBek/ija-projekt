/**
 * Autoři a změny podle commit historie:
 * - TheBoBek (2026-05-11)
 *   - 2026-05-11: Add Turn, money and player logic
 *
 * Popis obsahu:
 * - Zdrojový soubor Player v balíku ija.game.
 */
package ija.game;

/**
 * Mutable runtime metadata for one player.
 */
public final class Player {
    private String playerId;
    private int money;

    public Player(String playerId, int money) {
        setPlayerId(playerId);
        setMoney(money);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id must not be blank");
        }
        this.playerId = playerId;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0) {
            throw new IllegalArgumentException("Player money must not be negative");
        }
        this.money = money;
    }
}
