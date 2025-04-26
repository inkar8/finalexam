package game.model.combat;

import game.model.Player;
import game.model.entities.Entity;

/**
 * Interface for the Strategy pattern, representing different combat strategies.
 */
public interface CombatStrategy {
    
    /**
     * Calculates attack damage for a player.
     * 
     * @param player The player
     * @return The calculated attack damage
     */
    int calculateAttack(Player player);
    
    /**
     * Calculates defense value for a player.
     * 
     * @param player The player
     * @return The calculated defense value
     */
    int calculateDefense(Player player);
    
    /**
     * Calculates attack damage for an entity (like an enemy).
     * 
     * @param entity The entity
     * @return The calculated attack damage
     */
    int calculateAttack(Entity entity);
    
    /**
     * Calculates defense value for an entity (like an enemy).
     * 
     * @param entity The entity
     * @return The calculated defense value
     */
    int calculateDefense(Entity entity);
}
