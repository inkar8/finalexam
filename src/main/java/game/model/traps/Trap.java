package game.model.traps;

/**
 * Represents a trap that can harm the player.
 * Implements the State pattern for trap behavior.
 */
public class Trap {
    private String name;
    private String description;
    private int damage;
    private TrapState state;
    
    /**
     * Creates a new trap.
     * 
     * @param name        The trap's name
     * @param description The trap's description
     * @param damage      The damage the trap deals
     */
    public Trap(String name, String description, int damage) {
        this.name = name;
        this.description = description;
        this.damage = damage;
        // Default state is set by setter methods
    }
    
    /**
     * Sets the trap to the hidden state.
     */
    public void setHidden() {
        this.state = new HiddenTrapState(this);
    }
    
    /**
     * Sets the trap to the active state.
     */
    public void setActive() {
        this.state = new ActiveTrapState(this);
    }
    
    /**
     * Sets the trap to the disabled state.
     */
    public void disable() {
        this.state = new DisabledTrapState(this);
    }
    
    /**
     * Reveals a hidden trap.
     */
    public void reveal() {
        if (state instanceof HiddenTrapState) {
            setActive();
        }
    }
    
    /**
     * Checks if the trap is in the hidden state.
     * 
     * @return true if hidden, false otherwise
     */
    public boolean isHidden() {
        return state instanceof HiddenTrapState;
    }
    
    /**
     * Checks if the trap is in the active state.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return state instanceof ActiveTrapState;
    }
    
    /**
     * Checks if the trap is in the disabled state.
     * 
     * @return true if disabled, false otherwise
     */
    public boolean isDisabled() {
        return state instanceof DisabledTrapState;
    }
    
    /**
     * Gets the trap's state.
     * 
     * @return The current state
     */
    public TrapState getState() {
        return state;
    }
    
    /**
     * Gets the trap's name.
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the trap's description.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the damage the trap deals.
     * 
     * @return The damage value
     */
    public int getDamage() {
        return damage;
    }
}
