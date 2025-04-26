package game.model.puzzle;

import game.model.Player;

/**
 * Subject interface for the Observer pattern.
 * Objects implementing this interface can notify observers of puzzle-related events.
 */
public interface PuzzleSubject {
    
    /**
     * Adds an observer to this subject.
     * 
     * @param observer The observer to add
     */
    void addObserver(Player observer);
    
    /**
     * Removes an observer from this subject.
     * 
     * @param observer The observer to remove
     */
    void removeObserver(Player observer);
    
    /**
     * Notifies all observers of an event.
     * 
     * @param message A message describing the event
     */
    void notifyObservers(String message);
}
