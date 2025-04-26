package game.model.puzzle;

/**
 * A puzzle based on a riddle that the player must answer correctly.
 */
public class RiddlePuzzle extends Puzzle {
    private String question;
    private String answer;
    
    /**
     * Creates a new riddle puzzle.
     * 
     * @param question The riddle question
     * @param answer   The correct answer
     */
    public RiddlePuzzle(String question, String answer) {
        super(
            "A mysterious voice poses a riddle: \"" + question + "\"",
            "The voice speaks: \"You have answered wisely.\"",
            "The voice speaks: \"That is not the answer I seek.\"",
            "Think carefully about the properties described in the riddle."
        );
        
        this.question = question;
        this.answer = answer.toLowerCase();
    }
    
    @Override
    public boolean attemptSolution(String solution) {
        if (solution == null || solution.isEmpty()) {
            return false;
        }
        
        // Check if the solution matches the answer (case-insensitive)
        return solution.toLowerCase().trim().equals(answer);
    }
    
    /**
     * Gets the riddle question.
     * 
     * @return The question
     */
    public String getQuestion() {
        return question;
    }
}
