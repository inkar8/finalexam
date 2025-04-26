package game.model.combat;

import game.model.Player;
import game.model.entities.Entity;

import java.util.Random;

/**
 * A combat strategy using magic, with high variance and special effects.
 * Part of the Strategy pattern implementation.
 */
public class MagicStrategy implements CombatStrategy {
    private Random random = new Random();
    
    @Override
    public int calculateAttack(Player player) {
        // Magic strategy: Base attack with high variance
        int baseAttack = player.getAttack();
        
        // Random factor between 50% and 200% of the base attack
        double randomFactor = 0.5 + (random.nextDouble() * 1.5);
        
        // Critical hit chance (15% chance for double damage)
        if (random.nextDouble() < 0.15) {
            randomFactor *= 2;
            System.out.println("Critical hit with magic!");
        }
        
        return (int)(baseAttack * randomFactor);
    }
    
    @Override
    public int calculateDefense(Player player) {
        // Magic strategy: Base defense with medium variance
        int baseDefense = player.getDefense();
        
        // Random factor between 80% and 130% of the base defense
        double randomFactor = 0.8 + (random.nextDouble() * 0.5);
        
        // Magic barrier chance (10% chance for double defense)
        if (random.nextDouble() < 0.1) {
            randomFactor *= 2;
            System.out.println("Magic barrier activated!");
        }
        
        return (int)(baseDefense * randomFactor);
    }
    
    @Override
    public int calculateAttack(Entity entity) {
        // Magic strategy: Base attack with high variance
        int baseAttack = entity.getAttack();
        
        // Random factor between 50% and 200% of the base attack
        double randomFactor = 0.5 + (random.nextDouble() * 1.5);
        
        // Critical hit chance (15% chance for double damage)
        if (random.nextDouble() < 0.15) {
            randomFactor *= 2;
            System.out.println("Critical hit with magic!");
        }
        
        return (int)(baseAttack * randomFactor);
    }
    
    @Override
    public int calculateDefense(Entity entity) {
        // Magic strategy: Base defense with medium variance
        int baseDefense = entity.getDefense();
        
        // Random factor between 80% and 130% of the base defense
        double randomFactor = 0.8 + (random.nextDouble() * 0.5);
        
        // Magic barrier chance (10% chance for double defense)
        if (random.nextDouble() < 0.1) {
            randomFactor *= 2;
            System.out.println("Magic barrier activated!");
        }
        
        return (int)(baseDefense * randomFactor);
    }
}
