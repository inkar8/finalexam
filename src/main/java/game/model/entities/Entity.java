package game.model.entities;

/**
 * Base class for all entities in the game, like enemies and NPCs.
 */
public abstract class Entity {
    protected String name;
    protected String description;
    protected int health;
    protected int maxHealth;
    protected int attack;
    protected int defense;
    protected int speed;
    
    /**
     * Creates a new entity.
     * 
     * @param name        The entity's name
     * @param description The entity's description
     * @param health      The entity's health points
     * @param attack      The entity's attack value
     * @param defense     The entity's defense value
     * @param speed       The entity's speed value
     */
    public Entity(String name, String description, int health, int attack, int defense, int speed) {
        this.name = name;
        this.description = description;
        this.maxHealth = health;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }
    
    /**
     * Performs an attack.
     * 
     * @return The damage value
     */
    public abstract int attack();
    
    /**
     * Takes damage, reducing health.
     * 
     * @param damage Amount of damage to take
     * @return true if entity is still alive, false if dead
     */
    public boolean takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense / 3);
        health -= actualDamage;
        
        if (health <= 0) {
            health = 0;
            return false; // Entity died
        }
        
        return true; // Entity still alive
    }
    
    /**
     * Heals the entity for the specified amount.
     * 
     * @param amount Amount to heal
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    // Getters and setters
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public int getAttack() {
        return attack;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    @Override
    public String toString() {
        return name + " (HP: " + health + "/" + maxHealth + ")";
    }
}
