package game.model.rooms;

import game.model.Player;
import game.model.Position;

/**
 * The exit room, where the player can escape the labyrinth.
 */
public class ExitRoom extends Room {
    private boolean locked;
    private String keyRequired;
    
    /**
     * Creates a new exit room.
     * 
     * @param position The room's position
     */
    public ExitRoom(Position position) {
        super(RoomType.EXIT, position);
        this.description = "A room with a large, ornate door that appears to lead outside.";
        this.locked = Math.random() < 0.5; // 50% chance to be locked
        
        if (locked) {
            String[] keyTypes = {"golden_lock", "crystal_lock", "runic_lock"};
            this.keyRequired = keyTypes[(int) (Math.random() * keyTypes.length)];
        }
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        
        if (locked) {
            return "You've found the exit! " + description + " However, the door is locked. You'll need a key to open it.";
        } else {
            return "You've found the exit! " + description + " The door is unlocked. You can escape the labyrinth!";
        }
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (action.equalsIgnoreCase("open") || action.equalsIgnoreCase("exit") || 
            action.equalsIgnoreCase("escape") || action.equalsIgnoreCase("leave")) {
            
            if (locked) {
                // Check if player has the required key
                boolean hasKey = player.getInventory().stream()
                        .anyMatch(a -> a.isKey() && a.getKeyId().equals(keyRequired));
                
                if (hasKey) {
                    locked = false;
                    return "You use your key to unlock the exit door. You can now escape the labyrinth!";
                } else {
                    String keyName;
                    switch (keyRequired) {
                        case "golden_lock":
                            keyName = "Golden Key";
                            break;
                        case "crystal_lock":
                            keyName = "Crystal Key";
                            break;
                        case "runic_lock":
                            keyName = "Runic Key";
                            break;
                        default:
                            keyName = "special key";
                    }
                    
                    return "The exit door is locked. You need a " + keyName + " to unlock it.";
                }
            } else {
                return "VICTORY! You escape from the magical labyrinth!";
            }
        } else if (action.equalsIgnoreCase("examine") || action.equalsIgnoreCase("look")) {
            if (locked) {
                String lockType;
                switch (keyRequired) {
                    case "golden_lock":
                        lockType = "golden";
                        break;
                    case "crystal_lock":
                        lockType = "crystal";
                        break;
                    case "runic_lock":
                        lockType = "runic";
                        break;
                    default:
                        lockType = "peculiar";
                }
                
                return "You examine the exit door. It has a " + lockType + " lock. You'll need the matching key to open it.";
            } else {
                return "You examine the exit door. It's unlocked and ready to be opened.";
            }
        }
        
        return "Try 'open', 'exit', or 'examine' to interact with the exit.";
    }
    
    /**
     * Checks if the exit is locked.
     * 
     * @return true if locked, false otherwise
     */
    public boolean isLocked() {
        return locked;
    }
    
    /**
     * Gets the key required to unlock the exit.
     * 
     * @return The key ID
     */
    public String getKeyRequired() {
        return keyRequired;
    }
    
    /**
     * Unlocks the exit.
     */
    public void unlock() {
        this.locked = false;
    }
}
