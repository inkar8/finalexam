package game;

import game.controller.GameController;
import game.model.Labyrinth;
import game.model.Player;
import game.view.ConsoleView;
import game.view.GameView;
import game.view.SwingView;

/**
 * Main class that starts the Magical Labyrinth game.
 */
public class MagicalLabyrinth {
    private static final boolean USE_SWING = true; // Set to false to use console UI on Replit

    public static void main(String[] args) {
        System.out.println("Magical Labyrinth: Escape from the Dungeon");

        // Create the model
        Player player = new Player("Adventurer", 100, 10, 5);
        Labyrinth labyrinth = new Labyrinth(10, 10); // Create a 10x10 labyrinth

        // Create the view
        GameView view;
        if (USE_SWING) {
            view = new SwingView(labyrinth, player);
        } else {
            view = new ConsoleView(labyrinth, player);
        }

        // Create the controller
        GameController controller = new GameController(labyrinth, player, view);

        // Start the game
        controller.startGame();
    }
}