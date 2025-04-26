package game.model.traps;

/**
 * Represents a trap in a disabled state, harmless to the player.
 * Part of the State pattern implementation.
 */
public class DisabledTrapState implements TrapState {
    private Trap trap;
    
    /**
     * Creates a new disabled trap state for the given trap.
     * 
     * @param trap The trap
     */
    public DisabledTrapState(Trap trap) {
        this.trap = trap;
    }
    
    @Override
    public int trigger() {
        // Disabled traps don't deal damage
        return 0;
    }
    
    @Override
    public boolean disarm() {
        // Already disarmed
        return true;
    }
    
    @Override
    public String getDescription() {
        return "The trap has been disabled and is harmless.";
    }
}
