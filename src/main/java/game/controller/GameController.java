package game.controller;

import game.model.*;
import game.model.combat.AggressiveStrategy;
import game.model.combat.DefensiveStrategy;
import game.model.combat.MagicStrategy;
import game.model.rooms.Room;
import game.view.GameView;

import java.util.Scanner;

/**
 * Controller class handling game logic and user input.
 */
public class GameController {
    private Labyrinth labyrinth;
    private Player player;
    private GameView view;
    private boolean gameRunning;
    private Scanner scanner;
    
    /**
     * Creates a new game controller.
     * 
     * @param labyrinth The labyrinth model
     * @param player    The player model
     * @param view      The game view
     */
    public GameController(Labyrinth labyrinth, Player player, GameView view) {
        this.labyrinth = labyrinth;
        this.player = player;
        this.view = view;
        this.gameRunning = false;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Starts the game loop.
     */
    public void startGame() {
        gameRunning = true;
        
        // Display welcome message
        view.displayMessage("Welcome to the Magical Labyrinth!");
        view.displayMessage("You find yourself trapped in a mysterious dungeon filled with danger and treasure.");
        view.displayMessage("Find the exit to escape, but beware of monsters and traps!");
        view.displayMessage("Type 'help' for a list of commands.\n");
        
        // Put player in the starting room (0,0)
        player.setPosition(new Position(0, 0));
        Room currentRoom = labyrinth.getRoomAt(player.getPosition());
        
        if (currentRoom != null) {
            view.displayMessage(currentRoom.onEnter(player));
        } else {
            view.displayMessage("ERROR: Starting room is null!");
            return;
        }
        
        // Main game loop
        while (gameRunning) {
            // Update view
            view.update();
            
            // Get and process player command
            String command = view.getPlayerInput("What will you do? ");
            processCommand(command);
            
            // Check if player is still alive
            if (player.getHealth() <= 0) {
                view.displayMessage("GAME OVER! You have died in the labyrinth.");
                gameRunning = false;
            }
            
            // Check if player has reached the exit
            if (labyrinth.isAtExit(player.getPosition())) {
                Room exitRoom = labyrinth.getRoomAt(player.getPosition());
                String message = exitRoom.onInteract(player, "open");
                
                if (message.startsWith("VICTORY")) {
                    view.displayMessage(message);
                    view.displayMessage("Congratulations! You have escaped the magical labyrinth!");
                    gameRunning = false;
                }
            }
        }
        
        // Game ended
        view.displayMessage("Thanks for playing Magical Labyrinth!");
        scanner.close();
    }
    
    /**
     * Processes a player command.
     * 
     * @param command The command to process
     */
    private void processCommand(String command) {
        if (command == null || command.isEmpty()) {
            view.displayMessage("Please enter a command.");
            return;
        }
        
        command = command.toLowerCase().trim();
        
        if (command.equals("help") || command.equals("?")) {
            displayHelp();
        } else if (command.equals("look") || command.equals("examine room")) {
            Room currentRoom = labyrinth.getRoomAt(player.getPosition());
            view.displayMessage(currentRoom.onEnter(player));
        } else if (command.equals("inventory") || command.equals("items")) {
            displayInventory();
        } else if (command.equals("status") || command.equals("stats")) {
            displayPlayerStatus();
        } else if (command.startsWith("move ") || command.equals("north") || command.equals("south") || 
                   command.equals("east") || command.equals("west") || command.equals("n") || 
                   command.equals("s") || command.equals("e") || command.equals("w")) {
            movePlayer(command);
        } else if (command.equals("aggressive") || command.equals("attack mode")) {
            player.setCombatStrategy(new AggressiveStrategy());
            view.displayMessage("You switch to an aggressive combat stance.");
        } else if (command.equals("defensive") || command.equals("defense mode")) {
            player.setCombatStrategy(new DefensiveStrategy());
            view.displayMessage("You switch to a defensive combat stance.");
        } else if (command.equals("magic") || command.equals("magic mode")) {
            player.setCombatStrategy(new MagicStrategy());
            view.displayMessage("You prepare to use magical combat techniques.");
        } else if (command.equals("quit") || command.equals("exit game")) {
            view.displayMessage("Are you sure you want to quit? (y/n)");
            String confirm = view.getPlayerInput("").toLowerCase();
            if (confirm.startsWith("y")) {
                gameRunning = false;
            }
        } else {
            // Pass other commands to the current room for interaction
            Room currentRoom = labyrinth.getRoomAt(player.getPosition());
            String result = currentRoom.onInteract(player, command);
            view.displayMessage(result);
        }
    }
    
    /**
     * Displays help information to the player.
     */
    private void displayHelp() {
        view.displayMessage("Available commands:");
        view.displayMessage("- move (north/south/east/west) or n/s/e/w: Move in a direction");
        view.displayMessage("- look/examine room: Look around the current room");
        view.displayMessage("- inventory/items: Check your inventory");
        view.displayMessage("- status/stats: Display your character stats");
        view.displayMessage("- aggressive/attack mode: Switch to aggressive combat strategy");
        view.displayMessage("- defensive/defense mode: Switch to defensive combat strategy");
        view.displayMessage("- magic/magic mode: Switch to magic combat strategy");
        view.displayMessage("- examine [object]: Examine something in the room");
        view.displayMessage("- attack/fight: Fight an enemy if present");
        view.displayMessage("- take/loot: Collect items");
        view.displayMessage("- solve/answer [solution]: Solve a puzzle");
        view.displayMessage("- open/exit: Interact with exits");
        view.displayMessage("- quit/exit game: Quit the game");
    }
    
    /**
     * Displays the player's inventory.
     */
    private void displayInventory() {
        if (player.getInventory().isEmpty()) {
            view.displayMessage("Your inventory is empty.");
        } else {
            view.displayMessage("Inventory:");
            for (Artifact artifact : player.getInventory()) {
                view.displayMessage("- " + artifact.toString());
            }
        }
    }
    
    /**
     * Displays the player's status and stats.
     */
    private void displayPlayerStatus() {
        view.displayMessage(player.toString());
        view.displayMessage("Position: " + player.getPosition());
        view.displayMessage("Combat Style: " + player.getCombatStrategy().getClass().getSimpleName());
    }
    
    /**
     * Moves the player in the specified direction.
     * 
     * @param command The movement command
     */
    private void movePlayer(String command) {
        Direction direction = null;
        
        if (command.equals("north") || command.equals("n") || command.equals("move north")) {
            direction = Direction.NORTH;
        } else if (command.equals("south") || command.equals("s") || command.equals("move south")) {
            direction = Direction.SOUTH;
        } else if (command.equals("east") || command.equals("e") || command.equals("move east")) {
            direction = Direction.EAST;
        } else if (command.equals("west") || command.equals("w") || command.equals("move west")) {
            direction = Direction.WEST;
        } else if (command.startsWith("move ")) {
            String dir = command.substring(5).trim();
            if (dir.equals("north") || dir.equals("n")) {
                direction = Direction.NORTH;
            } else if (dir.equals("south") || dir.equals("s")) {
                direction = Direction.SOUTH;
            } else if (dir.equals("east") || dir.equals("e")) {
                direction = Direction.EAST;
            } else if (dir.equals("west") || dir.equals("w")) {
                direction = Direction.WEST;
            }
        }
        
        if (direction != null) {
            // Check if move is valid
            Position newPosition = player.getPosition().adjacent(direction);
            if (labyrinth.isValidMove(newPosition)) {
                // Update player position
                player.setPosition(newPosition);
                
                // Get and display information about the new room
                Room newRoom = labyrinth.getRoomAt(newPosition);
                view.displayMessage(newRoom.onEnter(player));
            } else {
                view.displayMessage("You can't go that way. There's a wall or the edge of the labyrinth.");
            }
        } else {
            view.displayMessage("Invalid direction. Use north, south, east, or west.");
        }
    }
}
