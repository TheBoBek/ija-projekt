package ija.command;

import ija.game.Game;

/**
 * One executable game action.
 */
public interface GameCommand<T> {
    CommandResult<T> execute(Game game);

    String describe();
}
