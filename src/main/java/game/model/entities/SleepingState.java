package game.model.entities;

/**
 * Represents an enemy in a sleeping state.
 * Part of the State pattern implementation.
 */
public class SleepingState implements EnemyState {
    private Enemy enemy;
    
    /**
     * Creates a new sleeping state for the given enemy.
     * 
     * @param enemy The enemy
     */
    public SleepingState(Enemy enemy) {
        this.enemy = enemy;
    }
    
    @Override
    public void onPlayerDetected() {
        // Probability of waking up when player is detected
        if (Math.random() < 0.7) {
            enemy.setState(new HuntingState(enemy));
            System.out.println("The " + enemy.getName() + " wakes up and sees you!");
        } else {
            System.out.println("The " + enemy.getName() + " stirs but remains asleep.");
        }
    }
    
    @Override
    public void onPlayerAttack() {
        // Always wake up if attacked
        enemy.setState(new HuntingState(enemy));
        System.out.println("The " + enemy.getName() + " roars in pain and anger as it wakes!");
    }
    
    @Override
    public void onLowHealth() {
        // Not relevant for sleeping enemies
    }
    
    @Override
    public String getDescription() {
        return "sleeping soundly, unaware of your presence";
    }
    
    @Override
    public boolean canAvoid() {
        return true; // Sleeping enemies can be avoided
    }
}
