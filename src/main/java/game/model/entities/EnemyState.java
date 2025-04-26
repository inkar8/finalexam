package game.model.entities;

/**
 * Interface for the State pattern, representing different enemy behaviors.
 */
public interface EnemyState {
    
    /**
     * Called when the player is detected.
     */
    void onPlayerDetected();
    
    /**
     * Called when the player attacks the enemy.
     */
    void onPlayerAttack();
    
    /**
     * Called when the enemy's health is low.
     */
    void onLowHealth();
    
    /**
     * Gets a description of the current state.
     * 
     * @return The state description
     */
    String getDescription();
    
    /**
     * Checks if the player can avoid the enemy in this state.
     * 
     * @return true if avoidable, false otherwise
     */
    boolean canAvoid();
}
