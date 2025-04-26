package game.model.rooms;

import game.model.Position;

/**
 * Factory class for creating different types of rooms.
 * Implements the Factory Method pattern.
 */
public class RoomFactory {
    
    /**
     * Creates a room of the specified type at the specified position.
     * 
     * @param type     The type of room to create
     * @param position The position for the room
     * @return The created room
     */
    public Room createRoom(RoomType type, Position position) {
        switch (type) {
            case REGULAR:
                return new RegularRoom(position);
            case PUZZLE:
                return new PuzzleRoom(position);
            case TREASURE:
                return new TreasureRoom(position);
            case MONSTER:
                return new MonsterRoom(position);
            case TRAP:
                return new TrapRoom(position);
            case EXIT:
                return new ExitRoom(position);
            default:
                // Default to regular room
                return new RegularRoom(position);
        }
    }
}
