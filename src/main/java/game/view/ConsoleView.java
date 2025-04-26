package game.view;

import game.model.Labyrinth;
import game.model.Player;
import game.model.Position;
import game.model.rooms.Room;

import java.util.Scanner;

/**
 * Console-based implementation of the game view.
 */
public class ConsoleView implements GameView {
    private Labyrinth labyrinth;
    private Player player;
    private Scanner scanner;
    
    /**
     * Creates a new console view.
     * 
     * @param labyrinth The labyrinth model
     * @param player    The player model
     */
    public ConsoleView(Labyrinth labyrinth, Player player) {
        this.labyrinth = labyrinth;
        this.player = player;
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    public void update() {
        // Display simple character stats
        System.out.println("\n" + player.getName() + " | HP: " + player.getHealth() + "/" + player.getMaxHealth() + 
                          " | Level: " + player.getLevel());
        
        // Display simple map
        displaySimpleMap();
    }
    
    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }
    
    @Override
    public String getPlayerInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    /**
     * Displays a simple ASCII map of the nearby rooms.
     */
    private void displaySimpleMap() {
        Position playerPos = player.getPosition();
        int playerX = playerPos.getX();
        int playerY = playerPos.getY();
        
        // Display a 5x5 grid centered on the player
        System.out.println("Map (? = unexplored, # = wall, @ = player):");
        
        for (int y = playerY - 2; y <= playerY + 2; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = playerX - 2; x <= playerX + 2; x++) {
                if (x == playerX && y == playerY) {
                    row.append("@"); // Player
                } else {
                    Room room = labyrinth.getRoomAt(new Position(x, y));
                    if (room == null) {
                        row.append("#"); // Wall
                    } else if (!room.isVisited()) {
                        row.append("?"); // Unexplored
                    } else {
                        // Display a character based on room type
                        switch (room.getType()) {
                            case REGULAR:
                                row.append(".");
                                break;
                            case PUZZLE:
                                row.append("P");
                                break;
                            case TREASURE:
                                row.append("T");
                                break;
                            case MONSTER:
                                row.append("M");
                                break;
                            case TRAP:
                                row.append("!");
                                break;
                            case EXIT:
                                row.append("E");
                                break;
                            default:
                                row.append(".");
                        }
                    }
                }
            }
            System.out.println(row);
        }
        
        // Map legend
        System.out.println("Legend: P=Puzzle, T=Treasure, M=Monster, !=Trap, E=Exit");
    }
}
