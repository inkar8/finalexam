package game.model.entities;

import game.model.combat.CombatStrategy;

/**
 * Represents an enemy that the player can fight.
 * Implements the State pattern for enemy behavior.
 */
public class Enemy extends Entity {
    private EnemyState state;
    private CombatStrategy combatStrategy;
    private int experienceValue;
    
    /**
     * Creates a new enemy.
     * 
     * @param name           The enemy's name
     * @param description    The enemy's description
     * @param health         The enemy's health points
     * @param attack         The enemy's attack value
     * @param defense        The enemy's defense value
     * @param speed          The enemy's speed value
     * @param experienceValue Experience granted when defeated
     */
    public Enemy(String name, String description, int health, int attack, int defense, int speed, int experienceValue) {
        super(name, description, health, attack, defense, speed);
        this.experienceValue = experienceValue;
        // Default state is set by the room that creates the enemy
    }
    
    /**
     * Called when the enemy detects the player.
     */
    public void playerDetected() {
        state.onPlayerDetected();
    }
    
    /**
     * Called when the enemy is hit by the player.
     */
    public void playerAttacked() {
        state.onPlayerAttack();
    }
    
    /**
     * Sets the enemy's state.
     * 
     * @param state The new state
     */
    public void setState(EnemyState state) {
        this.state = state;
    }
    
    /**
     * Gets the enemy's current state.
     * 
     * @return The current state
     */
    public EnemyState getState() {
        return state;
    }
    
    /**
     * Sets the enemy's combat strategy.
     * 
     * @param strategy The strategy to use
     */
    public void setCombatStrategy(CombatStrategy strategy) {
        this.combatStrategy = strategy;
    }
    
    /**
     * Gets the experience value for defeating this enemy.
     * 
     * @return The experience value
     */
    public int getExperienceValue() {
        return experienceValue;
    }
    
    @Override
    public int attack() {
        return combatStrategy.calculateAttack(this);
    }
    
    @Override
    public boolean takeDamage(int damage) {
        boolean alive = super.takeDamage(damage);
        
        if (alive && health < maxHealth / 3) {
            // If health gets low, might change behavior
            state.onLowHealth();
        }
        
        return alive;
    }
}
