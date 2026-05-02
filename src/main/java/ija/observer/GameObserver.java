package ija.observer;

import ija.common.GameEvent;

/**
 * Observer callback used by Game.
 */
public interface GameObserver {
    /**
     * Called when Game emits new event.
     */
    void update(GameEvent event);
}
