package ija.observer;

/**
 * Basic observable API for the game model.
 */
public interface Observable {
    /**
     * Add observer to notification list.
     */
    void addObserver(GameObserver observer);

    /**
     * Remove observer from notification list.
     */
    void removeObserver(GameObserver observer);
}
