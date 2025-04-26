package game.model.rooms;

import game.model.Artifact;
import game.model.Player;
import game.model.Position;
import game.model.puzzle.Puzzle;
import game.model.puzzle.PuzzleSubject;
import game.model.puzzle.RiddlePuzzle;

import java.util.ArrayList;
import java.util.List;

/**
 * A room containing a puzzle that must be solved.
 */
public class PuzzleRoom extends Room implements PuzzleSubject {
    private Puzzle puzzle;
    private boolean solved;
    private List<Player> observers;
    private Artifact reward;
    
    /**
     * Creates a new puzzle room.
     * 
     * @param position The room's position
     */
    public PuzzleRoom(Position position) {
        super(RoomType.PUZZLE, position);
        this.description = "A mysterious room with strange markings on the walls and floor.";
        this.puzzle = createRandomPuzzle();
        this.solved = false;
        this.observers = new ArrayList<>();
        this.reward = createRandomReward();
    }
    
    /**
     * Creates a random puzzle.
     * 
     * @return A random puzzle
     */
    private Puzzle createRandomPuzzle() {
        String[] riddles = {
            "I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?:echo",
            "The more you take, the more you leave behind. What am I?:footsteps",
            "What has keys but no locks, space but no room, and you can enter but not go in?:keyboard",
            "What gets wetter as it dries?:towel",
            "I'm light as a feather, yet the strongest person can't hold me for more than a few minutes. What am I?:breath"
        };
        
        int index = (int) (Math.random() * riddles.length);
        String[] parts = riddles[index].split(":");
        
        return new RiddlePuzzle(parts[0], parts[1]);
    }
    
    /**
     * Creates a random reward for solving the puzzle.
     * 
     * @return A random artifact
     */
    private Artifact createRandomReward() {
        String[] artifacts = {
            "Ancient Medallion:A medallion with strange symbols:0:5:0",
            "Mystic Crystal:A glowing crystal that hums with power:10:0:0",
            "Enchanted Ring:A ring that protects its wearer:0:0:5",
            "Magical Amulet:An amulet that enhances abilities:5:3:2",
            "Arcane Scepter:A powerful scepter crackling with energy:0:10:0"
        };
        
        int index = (int) (Math.random() * artifacts.length);
        String[] parts = artifacts[index].split(":");
        
        return new Artifact(
            parts[0], 
            parts[1], 
            Integer.parseInt(parts[2]), 
            Integer.parseInt(parts[3]), 
            Integer.parseInt(parts[4])
        );
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        addObserver(player);
        
        if (solved) {
            return "You enter a puzzle room that you've already solved. " + description;
        } else {
            return "You enter a room with a puzzle. " + description + "\n" + puzzle.getDescription();
        }
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (solved) {
            return "The puzzle in this room has already been solved.";
        }
        
        if (action.equalsIgnoreCase("solve") || action.startsWith("answer ")) {
            String answer = action.startsWith("answer ") ? action.substring(7) : "";
            
            if (puzzle.attemptSolution(answer)) {
                solved = true;
                player.addArtifact(reward);
                
                // Notify observers
                notifyObservers("The puzzle in the room has been solved! A hidden compartment opens revealing: " + reward.getName());
                
                return "Correct! " + puzzle.getSuccessMessage() + "\nYou found: " + reward;
            } else {
                return "That's not right. " + puzzle.getFailureMessage();
            }
        } else if (action.equalsIgnoreCase("hint")) {
            return puzzle.getHint();
        } else if (action.equalsIgnoreCase("examine") || action.equalsIgnoreCase("look")) {
            return "You examine the puzzle more closely. " + puzzle.getDescription();
        }
        
        return "Try 'solve', 'answer [your answer]', or 'hint' to interact with the puzzle.";
    }
    
    /**
     * Checks if the puzzle has been solved.
     * 
     * @return true if solved, false otherwise
     */
    public boolean isSolved() {
        return solved;
    }
    
    @Override
    public void addObserver(Player observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(Player observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message) {
        for (Player observer : observers) {
            observer.update(this, message);
        }
    }
}
