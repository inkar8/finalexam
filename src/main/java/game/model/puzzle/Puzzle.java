package game.model.puzzle;

/**
 * Abstract base class for puzzles in the game.
 */
public abstract class Puzzle {
    protected String description;
    protected String successMessage;
    protected String failureMessage;
    protected String hint;
    
    /**
     * Creates a new puzzle.
     * 
     * @param description    The puzzle's description
     * @param successMessage Message to show on successful solution
     * @param failureMessage Message to show on failed solution
     * @param hint           A hint for solving the puzzle
     */
    public Puzzle(String description, String successMessage, String failureMessage, String hint) {
        this.description = description;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.hint = hint;
    }
    
    /**
     * Attempts to solve the puzzle with the given solution.
     * 
     * @param solution The proposed solution
     * @return true if correct, false otherwise
     */
    public abstract boolean attemptSolution(String solution);
    
    /**
     * Gets the puzzle's description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the success message.
     * 
     * @return The success message
     */
    public String getSuccessMessage() {
        return successMessage;
    }
    
    /**
     * Gets the failure message.
     * 
     * @return The failure message
     */
    public String getFailureMessage() {
        return failureMessage;
    }
    
    /**
     * Gets a hint for solving the puzzle.
     * 
     * @return The hint
     */
    public String getHint() {
        return hint;
    }
}
