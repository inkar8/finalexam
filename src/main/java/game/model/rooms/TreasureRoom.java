package game.model.rooms;

import game.model.Artifact;
import game.model.Player;
import game.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A room containing treasures and artifacts.
 */
public class TreasureRoom extends Room {
    private List<Artifact> artifacts;
    private boolean looted;
    
    /**
     * Creates a new treasure room.
     * 
     * @param position The room's position
     */
    public TreasureRoom(Position position) {
        super(RoomType.TREASURE, position);
        this.description = "A room filled with glittering treasures and ancient artifacts.";
        this.artifacts = generateArtifacts();
        this.looted = false;
    }
    
    /**
     * Generates random artifacts for this room.
     * 
     * @return List of generated artifacts
     */
    private List<Artifact> generateArtifacts() {
        List<Artifact> result = new ArrayList<>();
        Random random = new Random();
        
        // Common artifacts
        String[][] commonArtifacts = {
            {"Health Potion", "A red potion that restores health", "20", "0", "0"},
            {"Strength Elixir", "A potion that temporarily increases strength", "0", "5", "0"},
            {"Shield Charm", "A charm that enhances defensive capabilities", "0", "0", "3"},
            {"Minor Healing Scroll", "A scroll that heals minor wounds", "10", "0", "0"}
        };
        
        // Rare artifacts
        String[][] rareArtifacts = {
            {"Dragonscale Amulet", "An amulet made from dragon scales", "15", "5", "10"},
            {"Enchanted Sword", "A sword imbued with magical energy", "0", "15", "0"},
            {"Mage's Staff", "A staff that enhances magical abilities", "10", "10", "5"},
            {"Blessed Shield", "A shield blessed by the gods", "0", "0", "20"}
        };
        
        // Key artifacts (for puzzle rooms or locked doors)
        String[][] keyArtifacts = {
            {"Golden Key", "A key that seems important", "golden_lock"},
            {"Crystal Key", "A key made of pure crystal", "crystal_lock"},
            {"Runic Key", "A key with strange runes inscribed", "runic_lock"}
        };
        
        // Add 1-3 common artifacts
        int commonCount = random.nextInt(3) + 1;
        for (int i = 0; i < commonCount; i++) {
            String[] artifact = commonArtifacts[random.nextInt(commonArtifacts.length)];
            result.add(new Artifact(
                artifact[0],
                artifact[1],
                Integer.parseInt(artifact[2]),
                Integer.parseInt(artifact[3]),
                Integer.parseInt(artifact[4])
            ));
        }
        
        // Small chance for a rare artifact
        if (random.nextDouble() < 0.3) {
            String[] artifact = rareArtifacts[random.nextInt(rareArtifacts.length)];
            result.add(new Artifact(
                artifact[0],
                artifact[1],
                Integer.parseInt(artifact[2]),
                Integer.parseInt(artifact[3]),
                Integer.parseInt(artifact[4])
            ));
        }
        
        // Small chance for a key
        if (random.nextDouble() < 0.2) {
            String[] key = keyArtifacts[random.nextInt(keyArtifacts.length)];
            result.add(new Artifact(key[0], key[1], key[2]));
        }
        
        return result;
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        
        if (looted) {
            return "You enter a treasure room that has already been looted. " + description + " But all treasures have been taken.";
        } else {
            return "You enter a treasure room! " + description + " You can see " + artifacts.size() + " valuable items.";
        }
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (looted) {
            return "This room has already been looted.";
        }
        
        if (action.equalsIgnoreCase("loot") || action.equalsIgnoreCase("take treasure") || 
            action.equalsIgnoreCase("collect") || action.equalsIgnoreCase("gather")) {
            
            StringBuilder result = new StringBuilder("You collect the following items:\n");
            
            for (Artifact artifact : artifacts) {
                player.addArtifact(artifact);
                result.append("- ").append(artifact).append("\n");
            }
            
            looted = true;
            return result.toString().trim();
        } else if (action.equalsIgnoreCase("examine") || action.equalsIgnoreCase("look")) {
            StringBuilder result = new StringBuilder("You see the following treasures:\n");
            
            for (Artifact artifact : artifacts) {
                result.append("- ").append(artifact).append("\n");
            }
            
            return result.toString().trim();
        }
        
        return "Try 'loot', 'take treasure', or 'examine' to interact with the treasure.";
    }
    
    /**
     * Checks if the room has been looted.
     * 
     * @return true if looted, false otherwise
     */
    public boolean isLooted() {
        return looted;
    }
    
    /**
     * Gets the artifacts in this room.
     * 
     * @return List of artifacts
     */
    public List<Artifact> getArtifacts() {
        return new ArrayList<>(artifacts);
    }
}
