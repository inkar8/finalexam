package game.model.entities;

/**
 * Represents an enemy in a hunting state, actively pursuing the player.
 * Part of the State pattern implementation.
 */
public class HuntingState implements EnemyState {
    private Enemy enemy;
    
    /**
     * Creates a new hunting state for the given enemy.
     * 
     * @param enemy The enemy
     */
    public HuntingState(Enemy enemy) {
        this.enemy = enemy;
    }
    
    @Override
    public void onPlayerDetected() {
        // Already hunting, so nothing changes
        System.out.println("The " + enemy.getName() + " is already hunting you!");
    }
    
    @Override
    public void onPlayerAttack() {
        // If attacked while hunting and health is low, might start fleeing
        if (enemy.getHealth() < enemy.getMaxHealth() / 4) {
            enemy.setState(new FleeingState(enemy));
            System.out.println("The " + enemy.getName() + " is badly hurt and tries to flee!");
        } else {
            System.out.println("The " + enemy.getName() + " growls and fights back!");
        }
    }
    
    @Override
    public void onLowHealth() {
        // When health gets low, consider fleeing
        if (Math.random() < 0.6) {
            enemy.setState(new FleeingState(enemy));
            System.out.println("The " + enemy.getName() + " realizes it's outmatched and tries to flee!");
        }
    }
    
    @Override
    public String getDescription() {
        return "alert and aggressively hunting you";
    }
    
    @Override
    public boolean canAvoid() {
        return false; // Hunting enemies can't be avoided normally
    }
}
