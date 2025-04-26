package game.model.traps;

/**
 * Represents a trap in a hidden state, not visible to the player.
 * Part of the State pattern implementation.
 */
public class HiddenTrapState implements TrapState {
    private Trap trap;
    
    /**
     * Creates a new hidden trap state for the given trap.
     * 
     * @param trap The trap
     */
    public HiddenTrapState(Trap trap) {
        this.trap = trap;
    }
    
    @Override
    public int trigger() {
        // Hidden traps deal full damage when triggered
        trap.setActive(); // Becomes active after triggering
        return trap.getDamage();
    }
    
    @Override
    public boolean disarm() {
        // Can't disarm what you can't see
        return false;
    }
    
    @Override
    public String getDescription() {
        return "The trap is hidden and cannot be seen.";
    }
}
