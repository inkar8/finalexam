package game.model.rooms;

import game.model.Player;
import game.model.Position;

/**
 * Abstract base class for all room types.
 */
public abstract class Room {
    protected Position position;
    protected RoomType type;
    protected String description;
    protected boolean visited;
    
    /**
     * Creates a new room.
     * 
     * @param type      The room type
     * @param position  The room's position in the labyrinth
     */
    public Room(RoomType type, Position position) {
        this.type = type;
        this.position = position;
        this.visited = false;
    }
    
    /**
     * Called when a player enters this room.
     * 
     * @param player The player
     * @return A message describing what happens
     */
    public abstract String onEnter(Player player);
    
    /**
     * Called when a player interacts with this room.
     * 
     * @param player The player
     * @param action The action to perform
     * @return A message describing what happens
     */
    public abstract String onInteract(Player player, String action);
    
    /**
     * Marks this room as visited.
     */
    public void markVisited() {
        this.visited = true;
    }
    
    /**
     * Checks if this room has been visited.
     * 
     * @return true if visited, false otherwise
     */
    public boolean isVisited() {
        return visited;
    }
    
    /**
     * Gets the room's position.
     * 
     * @return The position
     */
    public Position getPosition() {
        return position;
    }
    
    /**
     * Gets the room's type.
     * 
     * @return The room type
     */
    public RoomType getType() {
        return type;
    }
    
    /**
     * Gets the room's description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return type.toString() + " at " + position;
    }
}
