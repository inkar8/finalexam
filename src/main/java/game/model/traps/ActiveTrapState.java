package game.model.traps;

/**
 * Represents a trap in an active state, ready to be triggered.
 * Part of the State pattern implementation.
 */
public class ActiveTrapState implements TrapState {
    private Trap trap;
    
    /**
     * Creates a new active trap state for the given trap.
     * 
     * @param trap The trap
     */
    public ActiveTrapState(Trap trap) {
        this.trap = trap;
    }
    
    @Override
    public int trigger() {
        // Active traps deal full damage when triggered
        return trap.getDamage();
    }
    
    @Override
    public boolean disarm() {
        // 70% chance to successfully disarm
        if (Math.random() < 0.7) {
            trap.disable();
            return true;
        }
        return false;
    }
    
    @Override
    public String getDescription() {
        return "The trap is active and ready to trigger.";
    }
}
