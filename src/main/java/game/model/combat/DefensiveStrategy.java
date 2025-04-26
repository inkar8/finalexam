package game.model.combat;

import game.model.Player;
import game.model.entities.Entity;

import java.util.Random;

/**
 * A combat strategy focusing on maximizing defense at the expense of attack.
 * Part of the Strategy pattern implementation.
 */
public class DefensiveStrategy implements CombatStrategy {
    private Random random = new Random();
    
    @Override
    public int calculateAttack(Player player) {
        // Defensive strategy: 80% normal attack
        int baseAttack = player.getAttack();
        
        // Random factor between 70% and 90% of the base attack
        double randomFactor = 0.7 + (random.nextDouble() * 0.2);
        
        return (int)(baseAttack * randomFactor);
    }
    
    @Override
    public int calculateDefense(Player player) {
        // Defensive strategy: 175% normal defense
        int baseDefense = player.getDefense();
        int bonusDefense = (int)(baseDefense * 0.75); // 75% bonus
        
        // Random factor between 90% and 110% of the total
        double randomFactor = 0.9 + (random.nextDouble() * 0.2);
        
        return (int)((baseDefense + bonusDefense) * randomFactor);
    }
    
    @Override
    public int calculateAttack(Entity entity) {
        // Defensive strategy: 80% normal attack
        int baseAttack = entity.getAttack();
        
        // Random factor between 70% and 90% of the base attack
        double randomFactor = 0.7 + (random.nextDouble() * 0.2);
        
        return (int)(baseAttack * randomFactor);
    }
    
    @Override
    public int calculateDefense(Entity entity) {
        // Defensive strategy: 175% normal defense
        int baseDefense = entity.getDefense();
        int bonusDefense = (int)(baseDefense * 0.75); // 75% bonus
        
        // Random factor between 90% and 110% of the total
        double randomFactor = 0.9 + (random.nextDouble() * 0.2);
        
        return (int)((baseDefense + bonusDefense) * randomFactor);
    }
}
