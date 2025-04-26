package game.model;

/**
 * Represents a magical artifact that the player can collect.
 */
public class Artifact {
    private String name;
    private String description;
    private int healthBonus;
    private int attackBonus;
    private int defenseBonus;
    private boolean isKey; // If this artifact can unlock something
    private String keyId; // What this key unlocks, if it's a key
    
    /**
     * Creates a new artifact.
     * 
     * @param name        The artifact's name
     * @param description Description of the artifact
     * @param healthBonus Health bonus provided
     * @param attackBonus Attack bonus provided
     * @param defenseBonus Defense bonus provided
     */
    public Artifact(String name, String description, int healthBonus, int attackBonus, int defenseBonus) {
        this.name = name;
        this.description = description;
        this.healthBonus = healthBonus;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.isKey = false;
        this.keyId = null;
    }
    
    /**
     * Creates a new key artifact.
     * 
     * @param name        The key's name
     * @param description Description of the key
     * @param keyId       Identifier for what this key unlocks
     */
    public Artifact(String name, String description, String keyId) {
        this.name = name;
        this.description = description;
        this.healthBonus = 0;
        this.attackBonus = 0;
        this.defenseBonus = 0;
        this.isKey = true;
        this.keyId = keyId;
    }
    
    /**
     * Checks if this artifact is a key.
     * 
     * @return true if it's a key, false otherwise
     */
    public boolean isKey() {
        return isKey;
    }
    
    /**
     * Gets the key ID if this is a key.
     * 
     * @return The key ID, or null if not a key
     */
    public String getKeyId() {
        return keyId;
    }
    
    // Getters
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getHealthBonus() {
        return healthBonus;
    }
    
    public int getAttackBonus() {
        return attackBonus;
    }
    
    public int getDefenseBonus() {
        return defenseBonus;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ").append(description);
        
        if (isKey) {
            sb.append(" (Key)");
        } else {
            if (healthBonus != 0) sb.append(" HP+").append(healthBonus);
            if (attackBonus != 0) sb.append(" ATK+").append(attackBonus);
            if (defenseBonus != 0) sb.append(" DEF+").append(defenseBonus);
        }
        
        return sb.toString();
    }
}
