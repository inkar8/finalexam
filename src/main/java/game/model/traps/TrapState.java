package game.model.traps;

/**
 * Interface for the State pattern, representing different trap states.
 */
public interface TrapState {
    
    /**
     * Called when the trap is triggered.
     * 
     * @return The damage dealt, or 0 if no damage
     */
    int trigger();
    
    /**
     * Called when a player attempts to disarm the trap.
     * 
     * @return true if disarmed successfully, false otherwise
     */
    boolean disarm();
    
    /**
     * Gets a description of the current state.
     * 
     * @return The state description
     */
    String getDescription();
}
