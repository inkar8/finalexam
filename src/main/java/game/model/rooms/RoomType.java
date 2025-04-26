package game.model.rooms;

/**
 * Enum representing the different types of rooms.
 */
public enum RoomType {
    REGULAR("Regular Room"),
    PUZZLE("Puzzle Room"),
    TREASURE("Treasure Room"),
    MONSTER("Monster Room"),
    TRAP("Trap Room"),
    EXIT("Exit Room");
    
    private final String displayName;
    
    RoomType(String displayName) {
        this.displayName = displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
