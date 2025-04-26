package game.model.puzzle;

/**
 * Observer interface for the Observer pattern.
 * Objects implementing this interface can receive updates when puzzles are solved.
 */
public interface PuzzleObserver {
    
    /**
     * Called when a puzzle subject has an update.
     * 
     * @param subject The puzzle subject that was updated
     * @param message A message describing the update
     */
    void update(PuzzleSubject subject, String message);
}
