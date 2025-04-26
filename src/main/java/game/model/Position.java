package game.model;

import java.util.Objects;

/**
 * Represents a position in the labyrinth.
 */
public class Position {
    private int x;
    private int y;
    
    /**
     * Creates a new position with the given coordinates.
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a copy of the given position.
     * 
     * @param other The position to copy
     */
    public Position(Position other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * Gets the x-coordinate.
     * 
     * @return The x-coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Sets the x-coordinate.
     * 
     * @param x The new x-coordinate
     */
    public void setX(int x) {
        this.x = x;
    }
    
    /**
     * Gets the y-coordinate.
     * 
     * @return The y-coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Sets the y-coordinate.
     * 
     * @param y The new y-coordinate
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Gets the position adjacent to this one in the given direction.
     * 
     * @param direction The direction
     * @return The adjacent position
     */
    public Position adjacent(Direction direction) {
        switch (direction) {
            case NORTH:
                return new Position(x, y - 1);
            case SOUTH:
                return new Position(x, y + 1);
            case EAST:
                return new Position(x + 1, y);
            case WEST:
                return new Position(x - 1, y);
            default:
                return new Position(this);
        }
    }
    
    /**
     * Calculates the Manhattan distance to another position.
     * 
     * @param other The other position
     * @return The distance
     */
    public int distanceTo(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
