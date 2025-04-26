package game.model.rooms;

import game.model.Player;
import game.model.Position;

/**
 * A regular room with no special features.
 */
public class RegularRoom extends Room {
    
    /**
     * Creates a new regular room.
     * 
     * @param position The room's position
     */
    public RegularRoom(Position position) {
        super(RoomType.REGULAR, position);
        this.description = "A simple stone room with flickering torches on the walls.";
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        return "You enter " + description;
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (action.equalsIgnoreCase("look") || action.equalsIgnoreCase("examine")) {
            return "You examine the room more closely. " + description + " There's nothing particularly interesting here.";
        } else if (action.equalsIgnoreCase("search")) {
            return "You search the room carefully, but find nothing of value.";
        }
        
        return "There's nothing to " + action + " here.";
    }
}
