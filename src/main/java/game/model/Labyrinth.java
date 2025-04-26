package game.model;

import game.model.entities.Enemy;
import game.model.rooms.*;

import java.util.*;

/**
 * Represents the labyrinth, containing a grid of rooms.
 */
public class Labyrinth {
    private int width;
    private int height;
    private Room[][] rooms;
    private RoomFactory roomFactory;
    private Position exitPosition;
    private Random random;
    
    /**
     * Creates a new labyrinth with the specified dimensions.
     * 
     * @param width  Width of the labyrinth
     * @param height Height of the labyrinth
     */
    public Labyrinth(int width, int height) {
        this.width = width;
        this.height = height;
        this.rooms = new Room[height][width];
        this.roomFactory = new RoomFactory();
        this.random = new Random();
        
        generateLabyrinth();
    }
    
    /**
     * Generates the labyrinth layout with different room types.
     */
    private void generateLabyrinth() {
        // First, create a maze structure to ensure all rooms are reachable
        createMazeStructure();
        
        // Then, assign room types based on the structure
        assignRoomTypes();
        
        // Place exit room
        placeExitRoom();
    }
    
    /**
     * Creates a maze structure using a simple randomized DFS algorithm.
     */
    private void createMazeStructure() {
        // Initialize all rooms as null (walls)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rooms[y][x] = null;
            }
        }
        
        // Start from a random position
        int startX = random.nextInt(width);
        int startY = random.nextInt(height);
        Position start = new Position(startX, startY);
        
        // Create a set to track visited positions
        Set<Position> visited = new HashSet<>();
        
        // Create a stack for DFS
        Stack<Position> stack = new Stack<>();
        
        // Add the starting position
        visited.add(start);
        stack.push(start);
        
        // Create the starting room
        rooms[startY][startX] = roomFactory.createRoom(RoomType.REGULAR, new Position(startX, startY));
        
        // DFS to create passages
        while (!stack.isEmpty()) {
            Position current = stack.peek();
            
            // Get unvisited neighbors
            List<Position> neighbors = getUnvisitedNeighbors(current, visited);
            
            if (!neighbors.isEmpty()) {
                // Choose a random neighbor
                Position next = neighbors.get(random.nextInt(neighbors.size()));
                
                // Mark as visited
                visited.add(next);
                
                // Create a room at this position
                rooms[next.getY()][next.getX()] = roomFactory.createRoom(RoomType.REGULAR, next);
                
                // Push to stack
                stack.push(next);
            } else {
                // Backtrack
                stack.pop();
            }
        }
    }
    
    /**
     * Gets the unvisited neighbors of a position.
     */
    private List<Position> getUnvisitedNeighbors(Position pos, Set<Position> visited) {
        List<Position> neighbors = new ArrayList<>();
        
        // Check all four directions
        int[] dx = {0, 1, 0, -1}; // North, East, South, West
        int[] dy = {-1, 0, 1, 0};
        
        for (int i = 0; i < 4; i++) {
            int nx = pos.getX() + dx[i];
            int ny = pos.getY() + dy[i];
            
            // Check if the position is valid and not visited
            if (nx >= 0 && nx < width && ny >= 0 && ny < height && 
                !visited.contains(new Position(nx, ny))) {
                neighbors.add(new Position(nx, ny));
            }
        }
        
        return neighbors;
    }
    
    /**
     * Assigns room types based on the maze structure.
     */
    private void assignRoomTypes() {
        // Count the number of rooms
        int roomCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (rooms[y][x] != null) {
                    roomCount++;
                }
            }
        }
        
        // Determine counts for each room type
        int puzzleCount = roomCount / 10;
        int treasureCount = roomCount / 8;
        int monsterCount = roomCount / 6;
        int trapCount = roomCount / 8;
        
        // Place puzzle rooms
        placeRooms(RoomType.PUZZLE, puzzleCount);
        
        // Place treasure rooms
        placeRooms(RoomType.TREASURE, treasureCount);
        
        // Place monster rooms
        placeRooms(RoomType.MONSTER, monsterCount);
        
        // Place trap rooms
        placeRooms(RoomType.TRAP, trapCount);
    }
    
    /**
     * Places a specific number of rooms of a certain type.
     */
    private void placeRooms(RoomType type, int count) {
        for (int i = 0; i < count; i++) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                
                // Skip if position is a wall or entry position
                if (rooms[y][x] == null || (x == 0 && y == 0)) {
                    continue;
                }
                
                // Skip if already a special room
                if (rooms[y][x].getType() != RoomType.REGULAR) {
                    continue;
                }
                
                // Replace the room
                Position pos = new Position(x, y);
                rooms[y][x] = roomFactory.createRoom(type, pos);
                placed = true;
            }
        }
    }
    
    /**
     * Places the exit room at a random location far from the start.
     */
    private void placeExitRoom() {
        // Find the position farthest from start (0,0)
        int maxDistance = 0;
        Position farthestPos = null;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (rooms[y][x] != null && rooms[y][x].getType() == RoomType.REGULAR) {
                    int distance = x + y; // Manhattan distance
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        farthestPos = new Position(x, y);
                    }
                }
            }
        }
        
        // Place exit room
        if (farthestPos != null) {
            exitPosition = farthestPos;
            rooms[farthestPos.getY()][farthestPos.getX()] = 
                roomFactory.createRoom(RoomType.EXIT, farthestPos);
        } else {
            // Fallback if no suitable position found
            int x = width - 1;
            int y = height - 1;
            exitPosition = new Position(x, y);
            rooms[y][x] = roomFactory.createRoom(RoomType.EXIT, exitPosition);
        }
    }
    
    /**
     * Gets the room at the specified position.
     * 
     * @param position The position
     * @return The room, or null if out of bounds or a wall
     */
    public Room getRoomAt(Position position) {
        int x = position.getX();
        int y = position.getY();
        
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        
        return rooms[y][x];
    }
    
    /**
     * Checks if a move to the given position is valid.
     * 
     * @param position The position to check
     * @return true if valid, false otherwise
     */
    public boolean isValidMove(Position position) {
        Room room = getRoomAt(position);
        return room != null; // Valid if not a wall or out of bounds
    }
    
    /**
     * Gets all enemies in the labyrinth.
     * 
     * @return List of all enemies
     */
    public List<Enemy> getAllEnemies() {
        List<Enemy> enemies = new ArrayList<>();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (rooms[y][x] instanceof MonsterRoom) {
                    MonsterRoom monsterRoom = (MonsterRoom) rooms[y][x];
                    enemies.add(monsterRoom.getEnemy());
                }
            }
        }
        
        return enemies;
    }
    
    /**
     * Checks if the player has reached the exit room.
     * 
     * @param playerPosition The player's position
     * @return true if at exit, false otherwise
     */
    public boolean isAtExit(Position playerPosition) {
        return playerPosition.equals(exitPosition);
    }
    
    // Getters
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Room[][] getRooms() {
        return rooms;
    }
    
    public Position getExitPosition() {
        return exitPosition;
    }
}
