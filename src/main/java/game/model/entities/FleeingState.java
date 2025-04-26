package game.model.entities;

/**
 * Represents an enemy in a fleeing state, trying to escape from the player.
 * Part of the State pattern implementation.
 */
public class FleeingState implements EnemyState {
    private Enemy enemy;
    
    /**
     * Creates a new fleeing state for the given enemy.
     * 
     * @param enemy The enemy
     */
    public FleeingState(Enemy enemy) {
        this.enemy = enemy;
    }
    
    @Override
    public void onPlayerDetected() {
        // Already fleeing, so nothing changes
        System.out.println("The " + enemy.getName() + " continues trying to escape!");
    }
    
    @Override
    public void onPlayerAttack() {
        // If attacked while fleeing, has a chance to fight back out of desperation
        if (Math.random() < 0.3) {
            enemy.setState(new HuntingState(enemy));
            System.out.println("The " + enemy.getName() + " turns to fight in desperation!");
        } else {
            System.out.println("The " + enemy.getName() + " desperately tries to avoid your attack!");
        }
    }
    
    @Override
    public void onLowHealth() {
        // Already fleeing due to low health, so nothing changes
    }
    
    @Override
    public String getDescription() {
        return "wounded and trying to escape";
    }
    
    @Override
    public boolean canAvoid() {
        return true; // Fleeing enemies can be avoided
    }
}
