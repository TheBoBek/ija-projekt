package ija.bot;

import ija.game.Game;

/**
 * Player API for executing a full turn without UI clicks
 */
public interface Bot {
    void playTurn(Game game);
}
