package game.view;

/**
 * Interface for game views, allowing different implementations (console, GUI).
 */
public interface GameView {
    
    /**
     * Updates the view with the current game state.
     */
    void update();
    
    /**
     * Displays a message to the player.
     * 
     * @param message The message to display
     */
    void displayMessage(String message);
    
    /**
     * Gets input from the player.
     * 
     * @param prompt The prompt to display
     * @return The player's input
     */
    String getPlayerInput(String prompt);
}
