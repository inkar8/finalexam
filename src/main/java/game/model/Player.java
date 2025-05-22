package game.model;

import game.model.combat.AggressiveStrategy;
import game.model.combat.CombatStrategy;
import game.model.puzzle.PuzzleObserver;
import game.model.puzzle.PuzzleSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player character in the game.
 */
public class Player implements PuzzleObserver {
    private String name;
    private int maxHealth;
    private int health;
    private int attack;
    private int defense;
    private int level;
    private int experience;
    private Position position;
    private List<Artifact> inventory;
    private CombatStrategy combatStrategy;
    private int remainingMoves = 30;
    private boolean isTrapped = false;
    private String trapRiddle;
    private String trapAnswer;



    /**
     * Creates a new player.
     * 
     * @param name      The player's name
     * @param health    Starting health
     * @param attack    Base attack value
     * @param defense   Base defense value
     */
    public Player(String name, int health, int attack, int defense) {

        this.name = name;
        this.maxHealth = health;
        this.health = health;
        this.attack = attack;
        this.defense = defense;
        this.level = 1;
        this.experience = 0;
        this.position = new Position(0, 0); // Start at (0,0)
        this.inventory = new ArrayList<>();
        this.combatStrategy = new AggressiveStrategy(); // Default strategy
    }
    public void setTrapPuzzle(String riddle, String answer) {
        this.trapRiddle = riddle;
        this.trapAnswer = answer;
    }

    public String getTrapRiddle() {
        return trapRiddle;
    }

    public boolean solveTrap(String answer) {
        if (trapAnswer != null && trapAnswer.equalsIgnoreCase(answer.trim())) {
            isTrapped = false;
            trapRiddle = null;
            trapAnswer = null;
            return true;
        }
        return false;
    }

    public int getRemainingMoves() {
        return remainingMoves;
    }

    public void decrementMoves() {
        if (remainingMoves > 0) {
            remainingMoves--;
        }
    }

    public boolean hasMovesLeft() {
        return remainingMoves > 0;
    }

    public boolean isTrapped() {
        return isTrapped;
    }

    public void setTrapped(boolean trapped) {
        this.isTrapped = trapped;
    }

    /**
     * Moves the player in the given direction.
     * 
     * @param direction The direction to move
     */
    public void move(Direction direction) {
        switch (direction) {
            case NORTH:
                position.setY(position.getY() - 1);
                break;
            case SOUTH:
                position.setY(position.getY() + 1);
                break;
            case EAST:
                position.setX(position.getX() + 1);
                break;
            case WEST:
                position.setX(position.getX() - 1);
                break;
        }
    }
    
    /**
     * Attack calculation using the current combat strategy.
     * 
     * @return The attack damage value
     */
    public int attack() {
        return combatStrategy.calculateAttack(this);
    }
    
    /**
     * Defense calculation using the current combat strategy.
     * 
     * @return The defense value
     */
    public int defend() {
        return combatStrategy.calculateDefense(this);
    }
    
    /**
     * Takes damage, reducing health.
     * 
     * @param damage Amount of damage to take
     * @return true if player is still alive, false if dead
     */
    public boolean takeDamage(int damage) {
        int actualDamage = Math.max(1, damage - defense / 2); // Defense reduces damage
        health -= actualDamage;
        
        System.out.println(name + " takes " + actualDamage + " damage!");
        
        if (health <= 0) {
            health = 0;
            System.out.println(name + " has been defeated!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Heals the player for the specified amount.
     * 
     * @param amount Amount to heal
     */
    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
        System.out.println(name + " heals for " + amount + " health!");
    }
    
    /**
     * Adds experience to the player and levels up if necessary.
     * 
     * @param exp Amount of experience to add
     */
    public void gainExperience(int exp) {
        experience += exp;
        System.out.println(name + " gains " + exp + " experience!");
        
        // Check for level up (simple formula: 100 * current level)
        if (experience >= level * 100) {
            levelUp();
        }
    }
    
    /**
     * Level up the player, increasing stats.
     */
    private void levelUp() {
        level++;
        maxHealth += 10;
        health = maxHealth; // Fully heal on level up
        attack += 2;
        defense += 1;
        
        System.out.println(name + " leveled up to level " + level + "!");
        System.out.println("Health increased to " + maxHealth);
        System.out.println("Attack increased to " + attack);
        System.out.println("Defense increased to " + defense);
    }
    
    /**
     * Adds an artifact to the player's inventory.
     * 
     * @param artifact The artifact to add
     */
    public void addArtifact(Artifact artifact) {
        inventory.add(artifact);
        System.out.println(name + " obtained: " + artifact.getName());
        
        // Apply artifact effects
        maxHealth += artifact.getHealthBonus();
        health += artifact.getHealthBonus();
        attack += artifact.getAttackBonus();
        defense += artifact.getDefenseBonus();
    }
    
    /**
     * Checks if player has an artifact with the given name.
     * 
     * @param artifactName Name of the artifact
     * @return true if player has the artifact, false otherwise
     */
    public boolean hasArtifact(String artifactName) {
        return inventory.stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(artifactName));
    }
    
    /**
     * Sets the combat strategy to use.
     * 
     * @param strategy The strategy to use
     */
    public void setCombatStrategy(CombatStrategy strategy) {
        this.combatStrategy = strategy;
        System.out.println(name + " changes combat style to " + strategy.getClass().getSimpleName());
    }
    
    @Override
    public void update(PuzzleSubject subject, String message) {
        System.out.println("[Notification] " + message);
    }
    
    // Getters and setters
    
    public String getName() {
        return name;
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

    public int getLevel() {
        return level;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public List<Artifact> getInventory() {
        return inventory;
    }
    
    public CombatStrategy getCombatStrategy() {
        return combatStrategy;
    }
    
    @Override
    public String toString() {
        return name + " (Lvl " + level + ") HP: " + health + "/" + maxHealth + 
               " ATK: " + attack + " DEF: " + defense;
    }
}
