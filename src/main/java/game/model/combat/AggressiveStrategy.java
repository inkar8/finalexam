package game.model.combat;

import game.model.Player;
import game.model.entities.Entity;

import java.util.Random;

/**
 * A combat strategy focusing on maximizing damage at the expense of defense.
 * Part of the Strategy pattern implementation.
 */
public class AggressiveStrategy implements CombatStrategy {
    private Random random = new Random();
    
    @Override
    public int calculateAttack(Player player) {
        // Aggressive strategy: 150% attack but with randomness
        int baseAttack = player.getAttack();
        int bonusAttack = (int)(baseAttack * 0.5); // 50% bonus
        
        // Random factor between 80% and 120% of the total
        double randomFactor = 0.8 + (random.nextDouble() * 0.4);
        
        return (int)((baseAttack + bonusAttack) * randomFactor);
    }
    
    @Override
    public int calculateDefense(Player player) {
        // Aggressive strategy: 75% normal defense
        int baseDefense = player.getDefense();
        
        // Random factor between 70% and 90% of the base defense
        double randomFactor = 0.7 + (random.nextDouble() * 0.2);
        
        return (int)(baseDefense * randomFactor);
    }
    
    @Override
    public int calculateAttack(Entity entity) {
        // Aggressive strategy: 150% attack but with randomness
        int baseAttack = entity.getAttack();
        int bonusAttack = (int)(baseAttack * 0.5); // 50% bonus
        
        // Random factor between 80% and 120% of the total
        double randomFactor = 0.8 + (random.nextDouble() * 0.4);
        
        return (int)((baseAttack + bonusAttack) * randomFactor);
    }
    
    @Override
    public int calculateDefense(Entity entity) {
        // Aggressive strategy: 75% normal defense
        int baseDefense = entity.getDefense();
        
        // Random factor between 70% and 90% of the base defense
        double randomFactor = 0.7 + (random.nextDouble() * 0.2);
        
        return (int)(baseDefense * randomFactor);
    }
}
